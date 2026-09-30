export '../models/scooter_enums.dart';
export '../models/scooter_telemetry.dart';
export 'scooter_constants.dart';
export 'packet_builder.dart';
export 'packet_parser.dart';

import 'dart:typed_data';
import '../models/scooter_enums.dart';
import '../models/scooter_telemetry.dart';
import 'scooter_constants.dart';
import 'packet_builder.dart';
import 'packet_parser.dart';

/// Fachada unificada para o protocolo de comunicação do patinete elétrico M0Robot
class ScooterProtocol {
  final PacketParser _parser = PacketParser();

  /// Processa os bytes recebidos da característica BLE RX/Notify
  /// e retorna as telemetrias decodificadas preservando o estado existente
  List<ScooterTelemetry> parseIncomingBytes(List<int> bytes, {ScooterTelemetry? current}) {
    return _parser.processIncomingBytes(bytes, current: current);
  }

  /// Limpa os buffers residuais (útil ao reconectar)
  void reset() {
    _parser.clear();
  }

  // --- Geradores de Comandos ---

  /// Ligar / Desligar Farol (Registrador 0xD3)
  Uint8List buildHeadlightCommand(bool on, {int destination = ScooterConstants.addrEsc}) =>
      PacketBuilder.setHeadlight(on, destination: destination);

  /// Travar / Destravar Motor (Registrador 0x70)
  Uint8List buildLockCommand(bool locked, {int destination = ScooterConstants.addrEsc}) =>
      PacketBuilder.setMotorLock(locked, destination: destination);

  /// Selecionar Modo de Velocidade (Eco, Drive, Sport - Registrador 0x7E)
  Uint8List buildSpeedModeCommand(SpeedMode mode, {int destination = ScooterConstants.addrEsc}) =>
      PacketBuilder.setSpeedMode(mode, destination: destination);

  /// Ligar / Desligar Piloto Automático (Registrador 0x7C)
  Uint8List buildCruiseControlCommand(bool enabled, {int destination = ScooterConstants.addrEsc}) =>
      PacketBuilder.setCruiseControl(enabled, destination: destination);

  /// Definir Modo de Partida (Zero-start vs Kick-start - Registrador 0x7D)
  Uint8List buildStartModeCommand(StartMode mode, {int destination = ScooterConstants.addrEsc}) =>
      PacketBuilder.setStartMode(mode, destination: destination);

  /// Definir Limite de Velocidade Máxima (Registrador 0x72)
  Uint8List buildSpeedLimitCommand(int maxKmh, {int destination = ScooterConstants.addrEsc}) =>
      PacketBuilder.setSpeedLimit(maxKmh, destination: destination);

  /// Pacote de consulta de telemetria geral (Registrador 0xB0, 28 bytes)
  Uint8List buildTelemetryQueryCommand({int destination = ScooterConstants.addrEsc}) =>
      PacketBuilder.queryTelemetry(destination: destination);

  /// Pacote de handshake/versão (Registrador 0x1A)
  Uint8List buildVersionQueryCommand({int destination = ScooterConstants.addrEsc}) =>
      PacketBuilder.queryVersion(destination: destination);
}
