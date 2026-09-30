import 'dart:async';
import 'dart:math';
import 'dart:typed_data';
import 'scooter_ble_service.dart';
import '../protocol/scooter_constants.dart';
import '../protocol/packet_builder.dart';

/// Simulador de hardware de patinete M0Robot para testes e desenvolvimento offline
class MockScooterBleTransport implements BleTransport {
  final StreamController<List<int>> _rxController = StreamController<List<int>>.broadcast();

  bool _isHeadlightOn = false;
  bool _isLocked = false;
  bool _isCruiseEnabled = false;
  int _speedMode = 1; // Drive
  int _batteryPercent = 88;
  double _voltage = 39.2;
  double _speedKmh = 0.0;
  double _tripKm = 1.4;
  double _totalKm = 125.6;

  Timer? _simulationTimer;
  double _targetSpeed = 0.0;
  final Random _random = Random();

  MockScooterBleTransport() {
    // Iniciar loop de física simulada (aceleração suave / variação de telemetria)
    _simulationTimer = Timer.periodic(const Duration(milliseconds: 200), (_) {
      _updatePhysics();
    });
  }

  void _updatePhysics() {
    if (_isLocked) {
      _speedKmh = 0.0;
      _targetSpeed = 0.0;
      return;
    }

    // Variação suave de velocidade para simular movimento
    if ((_targetSpeed - _speedKmh).abs() > 0.5) {
      _speedKmh += (_targetSpeed - _speedKmh) * 0.15;
    } else {
      _speedKmh = _targetSpeed;
      // Escolher nova velocidade aleatória a cada momento
      if (_random.nextDouble() < 0.05) {
        final maxModeSpeed = [15.0, 20.0, 28.0][_speedMode];
        _targetSpeed = _random.nextDouble() * maxModeSpeed;
      }
    }

    if (_speedKmh > 0) {
      _tripKm += (_speedKmh / 3600.0) * 0.2;
      _totalKm += (_speedKmh / 3600.0) * 0.2;
    }
  }

  @override
  Stream<List<int>> get onDataReceived => _rxController.stream;

  @override
  Future<void> sendData(Uint8List data) async {
    if (data.length < 6) return;

    final int command = data[4];
    final int register = data[5];

    // Tratar comandos de escrita
    if (command == ScooterConstants.cmdWrite && data.length >= 8) {
      final int value = data[6];

      switch (register) {
        case ScooterConstants.regHeadlight:
          _isHeadlightOn = value == 1;
          break;
        case ScooterConstants.regLock:
          _isLocked = value == 1;
          break;
        case ScooterConstants.regSpeedMode:
          _speedMode = value.clamp(0, 2);
          break;
        case ScooterConstants.regCruiseControl:
          _isCruiseEnabled = value == 1;
          break;
      }
    }

    // Quando o app solicita telemetria, responder com pacote binário real
    _emitSimulatedTelemetry();
  }

  void _emitSimulatedTelemetry() {
    // Montar payload simulado no formato padrão LebiTEC/M0
    final ByteData payload = ByteData(21);
    int p = 0;

    // Status Word
    int status = 0;
    if (_isHeadlightOn) status |= 0x0001;
    if (_isLocked) status |= 0x0002;
    if (_isCruiseEnabled) status |= 0x0004;
    status |= (_speedMode << 3);

    payload.setUint16(p, status, Endian.little); p += 2;
    payload.setUint16(p, 0x0000, Endian.little); p += 2; // Error

    // Velocidade em unidades de 0.001 km/h
    final int rawSpeed = (_speedKmh * 1000).toInt();
    payload.setInt16(p, rawSpeed, Endian.little); p += 2;

    // Bateria %
    payload.setUint8(p, _batteryPercent); p += 1;

    // Tensão em mV
    final int rawVoltage = (_voltage * 1000).toInt();
    payload.setUint16(p, rawVoltage, Endian.little); p += 2;

    // Corrente em 0.01A (positiva ao acelerar)
    final int rawCurrent = _speedKmh > 0 ? ((_speedKmh / 3.0) * 100).toInt() : 0;
    payload.setInt16(p, rawCurrent, Endian.little); p += 2;

    // Temperatura da controladora (30.5 °C -> 305)
    payload.setInt16(p, 305, Endian.little); p += 2;

    // Odômetro parcial em metros
    payload.setUint16(p, (_tripKm * 1000).toInt(), Endian.little); p += 2;

    // Odômetro total em metros
    payload.setUint32(p, (_totalKm * 1000).toInt(), Endian.little); p += 4;

    // Gerar o frame binário oficial de resposta
    final frame = PacketBuilder.buildFrame(
      destination: ScooterConstants.addrBle,
      command: ScooterConstants.cmdTelemetryResponse,
      register: ScooterConstants.regTelemetryGeneral,
      payload: payload.buffer.asUint8List(),
    );

    _rxController.add(frame);
  }

  @override
  Future<void> disconnect() async {
    _simulationTimer?.cancel();
    _simulationTimer = null;
    await _rxController.close();
  }
}
