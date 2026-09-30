import 'dart:async';
import 'package:flutter/foundation.dart';
import 'package:flutter_blue_plus/flutter_blue_plus.dart';
import '../models/ble_traffic_log.dart';
import 'scooter_ble_service.dart';

/// Implementação do transporte BLE nativo utilizando o plugin FlutterBluePlus
/// Esta classe gerencia a comunicação serial de baixo nível com a controladora do patinete
class FlutterBlueTransport implements BleTransport {
  final BluetoothDevice device;
  final BluetoothCharacteristic txCharacteristic;
  final BluetoothCharacteristic rxCharacteristic;
  final List<BluetoothCharacteristic> additionalRxCharacteristics;
  final VoidCallback? onDisconnected;

  final StreamController<List<int>> _rxController = StreamController<List<int>>.broadcast();
  final StreamController<BleTrafficLog> _trafficController = StreamController<BleTrafficLog>.broadcast();
  final List<StreamSubscription> _subscriptions = [];
  StreamSubscription<BluetoothConnectionState>? _deviceConnSubscription;

  FlutterBlueTransport._({
    required this.device,
    required this.txCharacteristic,
    required this.rxCharacteristic,
    this.additionalRxCharacteristics = const [],
    this.onDisconnected,
  });

  /// Construtor assíncrono para garantir que as notificações CCCD estejam ativas
  /// antes de qualquer comando de escrita ser transmitido
  static Future<FlutterBlueTransport> create({
    required BluetoothDevice device,
    required BluetoothCharacteristic txCharacteristic,
    required BluetoothCharacteristic rxCharacteristic,
    List<BluetoothCharacteristic> additionalRxCharacteristics = const [],
    VoidCallback? onDisconnected,
  }) async {
    final transport = FlutterBlueTransport._(
      device: device,
      txCharacteristic: txCharacteristic,
      rxCharacteristic: rxCharacteristic,
      additionalRxCharacteristics: additionalRxCharacteristics,
      onDisconnected: onDisconnected,
    );

    transport._listenConnectionState();
    await transport._initNotify();
    return transport;
  }

  void _listenConnectionState() {
    _deviceConnSubscription = device.connectionState.listen((state) {
      if (state == BluetoothConnectionState.disconnected) {
        onDisconnected?.call();
      }
    });
  }

  final List<BleTrafficLog> _trafficHistory = [];

  void _recordTraffic(BleTrafficLog log) {
    if (_trafficHistory.length >= 400) {
      _trafficHistory.removeAt(0);
    }
    _trafficHistory.add(log);
    if (!_trafficController.isClosed) {
      _trafficController.add(log);
    }
  }

  Future<void> _initNotify() async {
    try {
      debugPrint('[BLE Transport] Configurando escuta na característica RX: ${rxCharacteristic.uuid}');

      // Todas as características que devem ser escutadas
      final allNotifyChars = {rxCharacteristic, ...additionalRxCharacteristics};

      for (final char in allNotifyChars) {
        // 1. Ouvir o fluxo onValueReceived ANTES de ativar o descriptor
        final sub = char.onValueReceived.listen((data) {
          if (data.isNotEmpty) {
            debugPrint('[BLE RX raw (${char.uuid})]: ${_formatHex(data)}');
            _rxController.add(data);
            _recordTraffic(
              BleTrafficLog(
                timestamp: DateTime.now(),
                direction: TrafficDirection.rx,
                bytes: data,
                characteristicUuid: char.uuid.toString(),
              ),
            );
          }
        });
        _subscriptions.add(sub);

        // 2. Ouvir também lastValueStream como garantia adicional
        final lastSub = char.lastValueStream.listen((data) {
          if (data.isNotEmpty) {
            _rxController.add(data);
          }
        });
        _subscriptions.add(lastSub);

        // 3. Ativar notificações CCCD
        try {
          await char.setNotifyValue(true);
          debugPrint('[BLE Transport] Notificações ativadas na char: ${char.uuid}');
        } catch (e) {
          debugPrint('[BLE Transport] Erro ao ativar notificação em ${char.uuid}: $e');
        }
      }

      // Se a característica TX for diferente da RX e também suportar notificação, escutar também
      if (!allNotifyChars.contains(txCharacteristic) &&
          (txCharacteristic.properties.notify || txCharacteristic.properties.indicate)) {
        final txSub = txCharacteristic.onValueReceived.listen((data) {
          if (data.isNotEmpty) {
            debugPrint('[BLE TX-RX fallback raw]: ${_formatHex(data)}');
            _rxController.add(data);
            _recordTraffic(
              BleTrafficLog(
                timestamp: DateTime.now(),
                direction: TrafficDirection.rx,
                bytes: data,
                characteristicUuid: txCharacteristic.uuid.toString(),
              ),
            );
          }
        });
        _subscriptions.add(txSub);
        try {
          await txCharacteristic.setNotifyValue(true);
        } catch (_) {}
      }

      // 4. Pausa de acomodação para o descritor GATT ser gravado na pilha BLE
      await Future.delayed(const Duration(milliseconds: 150));
    } catch (e) {
      debugPrint('Erro ao inicializar notificações BLE: $e');
    }
  }

  @override
  Stream<List<int>> get onDataReceived => _rxController.stream;

  Stream<BleTrafficLog> get trafficLogStream => _trafficController.stream;

  List<BleTrafficLog> get trafficHistory => List.unmodifiable(_trafficHistory);

  void clearTrafficHistory() {
    _trafficHistory.clear();
  }

  @override
  Future<void> sendData(Uint8List data) async {
    // O app oficial Android usa WRITE_TYPE_DEFAULT (com resposta / withoutResponse = false).
    // Firmware de patinetes muitas vezes ignora WRITE_CMD (sem resposta).
    // Se a característica suportar escrita padrão (write), priorizamos com resposta.
    final bool hasWrite = txCharacteristic.properties.write;
    final bool withoutResponse = hasWrite ? false : txCharacteristic.properties.writeWithoutResponse;
    await sendDataWithMode(data, withoutResponse: withoutResponse);
  }

  /// Permite envio explicitamente configurado com ou sem resposta para testes de protocolo
  Future<void> sendDataWithMode(
    Uint8List data, {
    required bool withoutResponse,
    BluetoothCharacteristic? customChar,
  }) async {
    final target = customChar ?? txCharacteristic;
    try {
      debugPrint('[BLE TX raw (char=${target.uuid}, withoutResponse=$withoutResponse)]: ${_formatHex(data)}');
      _recordTraffic(
        BleTrafficLog(
          timestamp: DateTime.now(),
          direction: TrafficDirection.tx,
          bytes: data,
          note: withoutResponse ? 'NoResponse' : 'WithResponse',
          characteristicUuid: target.uuid.toString(),
        ),
      );
      await target.write(data, withoutResponse: withoutResponse);
    } catch (e) {
      debugPrint('Erro ao enviar dados BLE via TX (withoutResponse=$withoutResponse): $e');
      // Tentativa de fallback alternando o modo de escrita
      try {
        await target.write(data, withoutResponse: !withoutResponse);
      } catch (e2) {
        debugPrint('Fallback de envio BLE também falhou: $e2');
      }
    }
  }

  String _formatHex(List<int> bytes) {
    return bytes.map((b) => b.toRadixString(16).padLeft(2, '0').toUpperCase()).join(' ');
  }

  @override
  Future<void> disconnect() async {
    try {
      for (final s in _subscriptions) {
        await s.cancel();
      }
      _subscriptions.clear();
      await _deviceConnSubscription?.cancel();
      _deviceConnSubscription = null;
      if (!_rxController.isClosed) {
        await _rxController.close();
      }
      if (!_trafficController.isClosed) {
        await _trafficController.close();
      }
      await device.disconnect();
    } catch (e) {
      debugPrint('Erro ao desconectar dispositivo BLE: $e');
    }
  }
}
