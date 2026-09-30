import 'dart:async';
import 'package:flutter/foundation.dart';
import 'package:flutter_blue_plus/flutter_blue_plus.dart';
import 'package:shared_preferences/shared_preferences.dart';
import '../protocol/scooter_protocol.dart';
import '../models/ble_traffic_log.dart';
import 'ble_permission_helper.dart';
import 'flutter_blue_transport.dart';
import 'scooter_device.dart';

/// Interface abstrata para envio e recebimento de dados BLE em baixo nível
abstract class BleTransport {
  Stream<List<int>> get onDataReceived;
  Future<void> sendData(Uint8List data);
  Future<void> disconnect();
}

/// Serviço central de comunicação Bluetooth Low Energy para o patinete M0Robot
class ScooterBleService {
  final ScooterProtocol _protocol = ScooterProtocol();

  // Controladores de Fluxo (Streams)
  final StreamController<BleConnectionStatus> _statusController =
      StreamController<BleConnectionStatus>.broadcast();
  final StreamController<ScooterTelemetry> _telemetryController =
      StreamController<ScooterTelemetry>.broadcast();
  final StreamController<List<ScooterDevice>> _scanResultsController =
      StreamController<List<ScooterDevice>>.broadcast();

  // Estados atuais
  BleConnectionStatus _currentStatus = BleConnectionStatus.disconnected;
  ScooterTelemetry _currentTelemetry = ScooterTelemetry.initial();
  ScooterDevice? _connectedDevice;
  final List<ScooterDevice> _discoveredDevices = [];

  // Temporizador para polling de telemetria contínua (keep-alive)
  Timer? _telemetryPollTimer;

  // Subscrições
  StreamSubscription<List<int>>? _rxSubscription;
  StreamSubscription<List<ScanResult>>? _scanSubscription;
  BleTransport? _transport;

  // Modos de operação
  bool _isAutoReconnectEnabled = true;

  ScooterBleService() {
    _statusController.add(_currentStatus);
    _telemetryController.add(_currentTelemetry);
  }

  // --------------------------------------------------------------------------
  // GETTERS PÚBLICOS
  // --------------------------------------------------------------------------

  Stream<BleConnectionStatus> get statusStream => _statusController.stream;
  BleConnectionStatus get currentStatus => _currentStatus;

  Stream<ScooterTelemetry> get telemetryStream => _telemetryController.stream;
  ScooterTelemetry get currentTelemetry => _currentTelemetry;

  Stream<List<ScooterDevice>> get scanResultsStream => _scanResultsController.stream;
  List<ScooterDevice> get discoveredDevices => List.unmodifiable(_discoveredDevices);

  ScooterDevice? get connectedDevice => _connectedDevice;
  bool get isConnected => _currentStatus == BleConnectionStatus.ready;

  List<BluetoothService> _discoveredServices = [];
  final List<BluetoothCharacteristic> _writableCharacteristics = [];
  final List<BluetoothCharacteristic> _notifyCharacteristics = [];

  List<BluetoothService> get discoveredServices => List.unmodifiable(_discoveredServices);
  List<BluetoothCharacteristic> get writableCharacteristics => List.unmodifiable(_writableCharacteristics);
  List<BluetoothCharacteristic> get notifyCharacteristics => List.unmodifiable(_notifyCharacteristics);

  BluetoothCharacteristic? get txCharacteristic =>
      _transport is FlutterBlueTransport ? (_transport as FlutterBlueTransport).txCharacteristic : null;

  BluetoothCharacteristic? get rxCharacteristic =>
      _transport is FlutterBlueTransport ? (_transport as FlutterBlueTransport).rxCharacteristic : null;

  Stream<BleTrafficLog> get trafficLogStream =>
      _transport is FlutterBlueTransport
          ? (_transport as FlutterBlueTransport).trafficLogStream
          : const Stream.empty();

  List<BleTrafficLog> get trafficHistory =>
      _transport is FlutterBlueTransport
          ? (_transport as FlutterBlueTransport).trafficHistory
          : const [];

  void clearTrafficLogs() {
    if (_transport is FlutterBlueTransport) {
      (_transport as FlutterBlueTransport).clearTrafficHistory();
    }
  }

  bool get isTelemetryPollingActive => _telemetryPollTimer != null;

  void setTelemetryPolling(bool active) {
    if (active) {
      _startTelemetryPolling();
    } else {
      _stopTelemetryPolling();
    }
  }

  Future<bool> sendRawBytes(
    List<int> bytes, {
    bool? withoutResponse,
    BluetoothCharacteristic? customChar,
  }) async {
    if (_transport is FlutterBlueTransport) {
      final fbt = _transport as FlutterBlueTransport;
      final target = customChar ?? fbt.txCharacteristic;
      final noResp = withoutResponse ?? target.properties.writeWithoutResponse;
      await fbt.sendDataWithMode(
        Uint8List.fromList(bytes),
        withoutResponse: noResp,
        customChar: customChar,
      );
      return true;
    } else if (_transport != null) {
      await _transport!.sendData(Uint8List.fromList(bytes));
      return true;
    }
    return false;
  }

  Future<bool> sendRawHex(
    String hexString, {
    bool? withoutResponse,
    BluetoothCharacteristic? customChar,
  }) async {
    final clean = hexString.replaceAll(RegExp(r'[^0-9a-fA-F]'), '');
    if (clean.length % 2 != 0 || clean.isEmpty) return false;
    final List<int> bytes = [];
    for (int i = 0; i < clean.length; i += 2) {
      bytes.add(int.parse(clean.substring(i, i + 2), radix: 16));
    }
    return await sendRawBytes(bytes, withoutResponse: withoutResponse, customChar: customChar);
  }

  // --------------------------------------------------------------------------
  // VARREDURA (SCAN)
  // --------------------------------------------------------------------------

  /// Inicia a busca real por patinetes elétricos compatíveis via FlutterBluePlus
  Future<void> startScan({
    Duration timeout = const Duration(seconds: 15),
    bool filterOnlyCompatible = false,
  }) async {
    try {
      if (await FlutterBluePlus.isSupported == false) {
        debugPrint('BLE não suportado no aparelho');
        return;
      }

      // 1. Validar e solicitar permissões de tempo de execução (Android 12+ e Android <= 11)
      await BlePermissionHelper.checkAndRequestPermissions();

      // 2. Verificar se o Bluetooth está ativo
      final adapterState = await FlutterBluePlus.adapterState.first;
      if (adapterState != BluetoothAdapterState.on) {
        try {
          await FlutterBluePlus.turnOn();
          await FlutterBluePlus.adapterState
              .where((s) => s == BluetoothAdapterState.on)
              .first
              .timeout(const Duration(seconds: 3));
        } catch (_) {}
      }

      // Parar qualquer busca anterior que ainda possa estar rodando
      if (FlutterBluePlus.isScanningNow) {
        await FlutterBluePlus.stopScan();
      }

      _discoveredDevices.clear();
      if (!_scanResultsController.isClosed) {
        _scanResultsController.add(_discoveredDevices);
      }
      _updateStatus(BleConnectionStatus.scanning);

      // 3. Obter dispositivos já pareados no Android (Bonded Devices)
      try {
        final bonded = await FlutterBluePlus.bondedDevices;
        for (final b in bonded) {
          final name = b.platformName.isNotEmpty ? b.platformName : 'Dispositivo Pareado';
          final device = ScooterDevice(
            name: name,
            id: b.remoteId.str,
            rssi: -55,
            nativeDevice: b,
          );
          onDeviceDiscovered(device, filterOnlyCompatible: filterOnlyCompatible);
        }
      } catch (e) {
        debugPrint('Aviso ao consultar bondedDevices: $e');
      }

      // 4. Obter dispositivos já conectados ao sistema operacional por qualquer app
      try {
        final systemDevs = await FlutterBluePlus.systemDevices([]);
        for (final s in systemDevs) {
          final name = s.platformName.isNotEmpty ? s.platformName : 'Dispositivo Conectado ao Sistema';
          final device = ScooterDevice(
            name: name,
            id: s.remoteId.str,
            rssi: -45,
            nativeDevice: s,
          );
          onDeviceDiscovered(device, filterOnlyCompatible: filterOnlyCompatible);
        }
      } catch (e) {
        debugPrint('Aviso ao consultar systemDevices: $e');
      }

      await _scanSubscription?.cancel();
      _scanSubscription = FlutterBluePlus.scanResults.listen(
        (results) {
          for (final r in results) {
            String name = r.device.platformName.isNotEmpty
                ? r.device.platformName
                : (r.advertisementData.advName.isNotEmpty
                    ? r.advertisementData.advName
                    : '');

            // Verificar se os serviços anunciados batem com os UUIDs conhecidos da controladora
            final bool hasScooterService = r.advertisementData.serviceUuids.any((u) {
              final s = u.str.toLowerCase();
              return s.contains('ffe0') ||
                  s.contains('fff0') ||
                  s.contains('6e400001') ||
                  s.contains('ae00');
            });

            // Se o nome estiver vazio no pacote primário de anúncio
            if (name.isEmpty) {
              final idStr = r.device.remoteId.str;
              final shortId = idStr.length > 5 ? idStr.substring(idStr.length - 5) : idStr;
              if (hasScooterService) {
                name = 'Patinete ($shortId)';
              } else {
                name = 'Dispositivo BLE ($shortId)';
              }
            }

            final device = ScooterDevice(
              name: name,
              id: r.device.remoteId.str,
              rssi: r.rssi,
              nativeDevice: r.device,
              hasKnownService: hasScooterService,
            );
            onDeviceDiscovered(device, filterOnlyCompatible: filterOnlyCompatible);
          }
        },
        onError: (err) {
          debugPrint('Erro na busca BLE: $err');
          _updateStatus(BleConnectionStatus.disconnected);
        },
      );

      await FlutterBluePlus.startScan(timeout: timeout);

      // Ao terminar o tempo limite da busca nativa
      FlutterBluePlus.isScanning.where((scanning) => !scanning).first.then((_) {
        if (_currentStatus == BleConnectionStatus.scanning) {
          _updateStatus(BleConnectionStatus.disconnected);
        }
      });
    } catch (e) {
      debugPrint('Exceção ao iniciar busca BLE: $e');
      _updateStatus(BleConnectionStatus.disconnected);
    }
  }

  /// Registra um dispositivo encontrado na varredura com ordenação inteligente
  void onDeviceDiscovered(ScooterDevice device, {bool filterOnlyCompatible = false}) {
    if (filterOnlyCompatible && !device.isCompatible) return;

    final existingIndex = _discoveredDevices.indexWhere((d) => d.id == device.id);
    if (existingIndex >= 0) {
      _discoveredDevices[existingIndex] = device;
    } else {
      _discoveredDevices.add(device);
    }

    // Ordenar: Alvo Oficial (M0Robot) primeiro, depois compatíveis conhecidos, depois por RSSI
    _discoveredDevices.sort((a, b) {
      if (a.isOfficialTarget && !b.isOfficialTarget) return -1;
      if (!a.isOfficialTarget && b.isOfficialTarget) return 1;
      if (a.isCompatible && !b.isCompatible) return -1;
      if (!a.isCompatible && b.isCompatible) return 1;
      return b.rssi.compareTo(a.rssi);
    });

    if (!_scanResultsController.isClosed) {
      _scanResultsController.add(List.from(_discoveredDevices));
    }
  }

  /// Cancela a varredura ativa
  Future<void> stopScan() async {
    try {
      await FlutterBluePlus.stopScan();
    } catch (_) {}
    await _scanSubscription?.cancel();
    _scanSubscription = null;
    if (_currentStatus == BleConnectionStatus.scanning) {
      _updateStatus(BleConnectionStatus.disconnected);
    }
  }

  // --------------------------------------------------------------------------
  // CONEXÃO E CICLO DE VIDA
  // --------------------------------------------------------------------------

  /// Conecta a um patinete específico (físico ou simulador)
  Future<bool> connect(ScooterDevice device, {BleTransport? transport}) async {
    await stopScan();
    _connectedDevice = device;
    _updateStatus(BleConnectionStatus.connecting);

    try {
      _protocol.reset();

      if (transport != null) {
        _transport = transport;
      } else {
        // Conexão nativa Bluetooth Low Energy
        final BluetoothDevice btDevice = device.nativeDevice is BluetoothDevice
            ? (device.nativeDevice as BluetoothDevice)
            : BluetoothDevice.fromId(device.id);

        try {
          await btDevice.disconnect();
        } catch (_) {}

        await btDevice.connect(
          timeout: const Duration(seconds: 15),
          autoConnect: false,
        );

        // Descobrir características UART (sem requestMtu para evitar desconexão pelo firmware)
        final services = await btDevice.discoverServices();
        _discoveredServices = services;
        _writableCharacteristics.clear();
        _notifyCharacteristics.clear();

        for (final s in services) {
          final sUuid = s.uuid.toString().toLowerCase();
          // Ignorar serviços genéricos do sistema Bluetooth (GAP 1800, GATT 1801)
          if (sUuid.contains('1800') || sUuid.contains('1801')) {
            debugPrint('[BLE GATT] Ignorando serviço genérico de sistema: ${s.uuid}');
            continue;
          }

          debugPrint('[BLE GATT] Serviço: ${s.uuid}');
          for (final c in s.characteristics) {
            final p = c.properties;
            debugPrint('[BLE GATT]   Char: ${c.uuid} (write=${p.write}, writeNoResp=${p.writeWithoutResponse}, notify=${p.notify}, indicate=${p.indicate})');
            if (p.write || p.writeWithoutResponse) {
              _writableCharacteristics.add(c);
            }
            if (p.notify || p.indicate) {
              _notifyCharacteristics.add(c);
            }
          }
        }

        final chars = _findScooterCharacteristics(services);
        final txChar = chars['tx'] ?? (_writableCharacteristics.isNotEmpty ? _writableCharacteristics.first : null);
        final rxChar = chars['rx'] ?? (_notifyCharacteristics.isNotEmpty ? _notifyCharacteristics.first : null);

        if (txChar == null || rxChar == null) {
          throw Exception('Características seriais de controle (TX/RX) não encontradas no dispositivo.');
        }

        final additionalNotify = _notifyCharacteristics.where((c) => c != rxChar).toList();

        _transport = await FlutterBlueTransport.create(
          device: btDevice,
          txCharacteristic: txChar,
          rxCharacteristic: rxChar,
          additionalRxCharacteristics: additionalNotify,
          onDisconnected: () => _handleDisconnect(),
        );
      }

      _rxSubscription?.cancel();
      _rxSubscription = _transport!.onDataReceived.listen(
        _handleIncomingBytes,
        onError: (_) => _handleDisconnect(),
        onDone: () => _handleDisconnect(),
      );

      _updateStatus(BleConnectionStatus.connected);

      // Salvar último dispositivo conectado
      try {
        final prefs = await SharedPreferences.getInstance();
        await prefs.setString('last_device_id', device.id);
        await prefs.setString('last_device_name', device.name);
      } catch (_) {}

      // Handshake inicial oficial: consulta de identificação e versão do painel (destino 0x21)
      try {
        await _sendPacket(PacketBuilder.queryDash14(destination: ScooterConstants.addrBle));
        await Future.delayed(const Duration(milliseconds: 150));
        await _sendPacket(PacketBuilder.queryVersion(destination: ScooterConstants.addrBle));
        await Future.delayed(const Duration(milliseconds: 150));
      } catch (e) {
        debugPrint('Aviso no handshake inicial: $e');
      }

      // Iniciar subscrição e polling de telemetria
      _startTelemetryPolling();

      _updateStatus(BleConnectionStatus.ready);
      return true;
    } catch (e) {
      debugPrint('Falha ao conectar no patinete: $e');
      _updateStatus(BleConnectionStatus.error);
      _handleDisconnect();
      return false;
    }
  }

  /// Localiza as características seriais no dispositivo conectado
  static Map<String, BluetoothCharacteristic?> _findScooterCharacteristics(
      List<BluetoothService> services) {
    BluetoothCharacteristic? txChar;
    BluetoothCharacteristic? rxChar;

    String norm(dynamic uuid) => uuid.toString().toLowerCase().replaceAll('-', '');

    // 1. Procurar perfis conhecidos (HM-10, Nordic UART, E9/MAX, Custom)
    for (final s in services) {
      final sUuid = norm(s.uuid);

      // HM-10 (ffe0 -> ffe1 Write+Notify)
      if (sUuid.contains('ffe0')) {
        for (final c in s.characteristics) {
          if (norm(c.uuid).contains('ffe1')) {
            txChar = c;
            rxChar = c;
            break;
          }
        }
      }

      // Nordic UART (6e400001 -> 6e400002 TX, 6e400003 RX)
      if (sUuid.contains('6e400001')) {
        for (final c in s.characteristics) {
          final cUuid = norm(c.uuid);
          if (cUuid.contains('6e400002')) txChar = c;
          if (cUuid.contains('6e400003')) rxChar = c;
        }
        rxChar ??= txChar;
      }

      // E9 / MAX (fff0 -> fff3 TX, fff4 RX)
      if (sUuid.contains('fff0')) {
        for (final c in s.characteristics) {
          final cUuid = norm(c.uuid);
          if (cUuid.contains('fff3')) txChar = c;
          if (cUuid.contains('fff4')) rxChar = c;
        }
      }

      // Custom (ae00 -> ae01 TX, ae02 RX)
      if (sUuid.contains('ae00')) {
        for (final c in s.characteristics) {
          final cUuid = norm(c.uuid);
          if (cUuid.contains('ae01')) txChar = c;
          if (cUuid.contains('ae02')) rxChar = c;
        }
      }
    }

    // 2. Heurística de fallback: procurar característica com suporte a escrita e notificação
    if (txChar == null || rxChar == null) {
      for (final s in services) {
        for (final c in s.characteristics) {
          final p = c.properties;
          final canWrite = p.write || p.writeWithoutResponse;
          final canNotify = p.notify || p.indicate;

          if (canWrite && canNotify && txChar == null && rxChar == null) {
            txChar = c;
            rxChar = c;
            break;
          }
          if (canWrite && txChar == null) {
            txChar = c;
          }
          if (canNotify && rxChar == null) {
            rxChar = c;
          }
        }
        if (txChar != null && rxChar != null) break;
      }
    }

    return {'tx': txChar, 'rx': rxChar};
  }

  /// Desconecta intencionalmente do patinete e zera a telemetria
  Future<void> disconnect() async {
    _isAutoReconnectEnabled = false;
    _stopTelemetryPolling();
    await _rxSubscription?.cancel();
    _rxSubscription = null;

    if (_transport != null) {
      await _transport!.disconnect();
      _transport = null;
    }

    _connectedDevice = null;
    _currentTelemetry = ScooterTelemetry.initial();
    if (!_telemetryController.isClosed) {
      _telemetryController.add(_currentTelemetry);
    }
    _updateStatus(BleConnectionStatus.disconnected);
  }

  /// Tratamento imediato de desconexão ou queda de link
  void _handleDisconnect() {
    _stopTelemetryPolling();
    _currentTelemetry = ScooterTelemetry.initial();
    if (!_telemetryController.isClosed) {
      _telemetryController.add(_currentTelemetry);
    }
    _updateStatus(BleConnectionStatus.disconnected);

    if (_isAutoReconnectEnabled && _connectedDevice != null && _transport is! FlutterBlueTransport) {
      // Reconexão silenciosa opcional
    }
  }

  // --------------------------------------------------------------------------
  // ENVIO DE COMANDOS DE CONTROLE
  // --------------------------------------------------------------------------

  Future<bool> _sendPacket(Uint8List packet) async {
    if (!isConnected || _transport == null) return false;
    try {
      await _transport!.sendData(packet);
      return true;
    } catch (_) {
      return false;
    }
  }

  Future<bool> setHeadlight(bool on) async {
    // Registrador oficial 0xD3 (bit 7: 0x80)
    final pD3 = _protocol.buildHeadlightCommand(on, destination: ScooterConstants.addrEsc);
    final success = await _sendPacket(pD3);
    if (success) {
      _currentTelemetry = _currentTelemetry.copyWith(isHeadlightOn: on);
      _telemetryController.add(_currentTelemetry);
    }
    return success;
  }

  Future<bool> setMotorLock(bool locked) async {
    if (locked && !_currentTelemetry.isSafeToLock) {
      return false;
    }

    // Registrador oficial 0x70: 0x0001 (travar), 0x0000 (destravar)
    final p20 = _protocol.buildLockCommand(locked, destination: ScooterConstants.addrEsc);
    final success = await _sendPacket(p20);
    if (success) {
      _currentTelemetry = _currentTelemetry.copyWith(isLocked: locked);
      _telemetryController.add(_currentTelemetry);
    }
    return success;
  }

  Future<bool> setSpeedMode(SpeedMode mode) async {
    // Registrador oficial 0x7E: 0=Eco, 1=Drive, 2=Sport
    final p20 = _protocol.buildSpeedModeCommand(mode, destination: ScooterConstants.addrEsc);
    final success = await _sendPacket(p20);
    if (success) {
      _currentTelemetry = _currentTelemetry.copyWith(speedMode: mode);
      _telemetryController.add(_currentTelemetry);
    }
    return success;
  }

  Future<bool> setCruiseControl(bool enabled) async {
    // Registrador oficial 0x7C: 1=Ligado, 0=Desligado
    final p20 = _protocol.buildCruiseControlCommand(enabled, destination: ScooterConstants.addrEsc);
    final success = await _sendPacket(p20);
    if (success) {
      _currentTelemetry = _currentTelemetry.copyWith(isCruiseControlEnabled: enabled);
      _telemetryController.add(_currentTelemetry);
    }
    return success;
  }

  Future<bool> setStartMode(StartMode mode) async {
    // Registrador oficial 0x7D: 1=Zero Start, 0=Kick-to-start
    final p20 = _protocol.buildStartModeCommand(mode, destination: ScooterConstants.addrEsc);
    final success = await _sendPacket(p20);
    if (success) {
      _currentTelemetry = _currentTelemetry.copyWith(startMode: mode);
      _telemetryController.add(_currentTelemetry);
    }
    return success;
  }

  Future<bool> setSpeedLimit(int maxKmh) async {
    final p20 = _protocol.buildSpeedLimitCommand(maxKmh, destination: ScooterConstants.addrEsc);
    return await _sendPacket(p20);
  }

  int _pollCounter = 0;

  Future<void> queryTelemetry() async {
    _pollCounter++;
    // Rotaciona as consultas oficiais identificadas na engenharia reversa do app SENCOR:
    // Ciclo 0: Bloco C2 (farol, tensão em 0.1V, corrente, temperatura)
    // Ciclo 1: Bloco 1A (velocidade instantânea, bateria %, odômetro parcial e total)
    // Ciclo 2: Bloco 6C (trava, piloto automático, modo de partida, modo de velocidade)
    // Ciclo 3: Bloco EA (limites e status secundário)
    final int cycle = _pollCounter % 4;
    Uint8List packet;
    if (cycle == 0) {
      packet = PacketBuilder.queryTelemetryC2(destination: ScooterConstants.addrEsc);
    } else if (cycle == 1) {
      packet = PacketBuilder.queryStatus1A(destination: ScooterConstants.addrEsc);
    } else if (cycle == 2) {
      packet = PacketBuilder.queryConfig6C(destination: ScooterConstants.addrEsc);
    } else {
      packet = PacketBuilder.querySecondaryEA(destination: ScooterConstants.addrEsc);
    }
    await _sendPacket(packet);
  }

  // --------------------------------------------------------------------------
  // RECEBIMENTO E PARSER DE TELEMETRIA
  // --------------------------------------------------------------------------

  void _handleIncomingBytes(List<int> bytes) {
    final telemetries = _protocol.parseIncomingBytes(bytes, current: _currentTelemetry);
    for (final telemetry in telemetries) {
      _currentTelemetry = telemetry;
      if (!_telemetryController.isClosed) {
        _telemetryController.add(_currentTelemetry);
      }
    }
  }

  void _startTelemetryPolling() {
    _stopTelemetryPolling();
    // Consulta periódica a cada 1000ms para manter o link estável sem sobrecarregar o barramento UART
    _telemetryPollTimer = Timer.periodic(const Duration(milliseconds: 1000), (_) {
      queryTelemetry();
    });
  }

  void _stopTelemetryPolling() {
    _telemetryPollTimer?.cancel();
    _telemetryPollTimer = null;
  }

  void _updateStatus(BleConnectionStatus status) {
    _currentStatus = status;
    if (!_statusController.isClosed) {
      _statusController.add(_currentStatus);
    }
  }

  void dispose() {
    disconnect();
    _statusController.close();
    _telemetryController.close();
    _scanResultsController.close();
  }
}
