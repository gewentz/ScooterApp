import 'dart:typed_data';
import 'scooter_constants.dart';
import '../models/scooter_enums.dart';

/// Construtor de pacotes binários para comunicação BLE com a controladora M0Robot
class PacketBuilder {
  PacketBuilder._();

  /// Calcula a soma de verificação de 16 bits invertida (Padrão Ninebot/LebiTEC)
  ///
  /// O checksum soma todos os bytes a partir do comprimento (Len) até o final dos dados
  /// e aplica XOR 0xFFFF:
  ///   checksum = (sum & 0xFFFF) ^ 0xFFFF
  static int calculateChecksum(List<int> bytes, int startIndex, int length) {
    int sum = 0;
    for (int i = startIndex; i < startIndex + length; i++) {
      sum += bytes[i];
    }
    return (sum & 0xFFFF) ^ 0xFFFF;
  }

  /// Monta um frame genérico de comando no padrão:
  /// [0x55] [0xAA] [Len] [Destino] [Comando] [Registrador] [Dados...] [Checksum_L] [Checksum_H]
  static Uint8List buildFrame({
    int header0 = ScooterConstants.headerByte0,
    int header1 = ScooterConstants.headerByte1,
    required int destination,
    required int command,
    required int register,
    required List<int> payload,
  }) {
    // Len representa o tamanho de: Comando (1B) + Registrador (1B) + Payload (NB)
    // Em alguns firmwares X-Bot, Len = payload.length + 2.
    final int payloadLength = payload.length + 2;

    // Tamanho total do pacote:
    // Header (2B) + Len (1B) + Destino (1B) + Cmd (1B) + Reg (1B) + Payload (NB) + Checksum (2B)
    final int totalSize = 2 + 1 + 1 + 1 + 1 + payload.length + 2;
    final Uint8List frame = Uint8List(totalSize);

    // 1. Cabeçalho (2 bytes)
    frame[0] = header0;
    frame[1] = header1;

    // 2. Comprimento (1 byte)
    frame[2] = payloadLength;

    // 3. Endereço do Destino (1 byte)
    frame[3] = destination;

    // 4. Código do Comando (1 byte)
    frame[4] = command;

    // 5. Endereço do Registrador alvo (1 byte)
    frame[5] = register;

    // 6. Dados do Payload
    for (int i = 0; i < payload.length; i++) {
      frame[6 + i] = payload[i];
    }

    // 7. Cálculo do Checksum sobre: [Len, Destino, Cmd, Reg, Payload...]
    final int checksumStart = 2;
    final int checksumLength = 1 + 1 + 1 + 1 + payload.length;
    final int checksum = calculateChecksum(frame, checksumStart, checksumLength);

    // 8. Anexar Checksum (Little-Endian: Byte Baixo primeiro, Byte Alto depois)
    final int checksumOffset = 6 + payload.length;
    frame[checksumOffset] = checksum & 0xFF;
    frame[checksumOffset + 1] = (checksum >> 8) & 0xFF;

    return frame;
  }

  // --------------------------------------------------------------------------
  // COMANDOS DE ALTO NÍVEL DO PATINETE (REVERSA OFICIAL SENCOR / M0ROBOT)
  // --------------------------------------------------------------------------

  /// Constrói comando genérico de leitura
  static Uint8List buildReadCommand({
    required int destination,
    required int register,
    required int length,
  }) {
    return buildFrame(
      destination: destination,
      command: ScooterConstants.cmdRead,
      register: register,
      payload: [length],
    );
  }

  /// Constrói comando genérico de escrita de 16 bits
  static Uint8List buildWriteCommand({
    required int destination,
    required int register,
    required int value16,
  }) {
    return buildFrame(
      destination: destination,
      command: ScooterConstants.cmdWrite,
      register: register,
      payload: [value16 & 0xFF, (value16 >> 8) & 0xFF],
    );
  }

  /// Comando para ligar ou desligar o farol
  /// Registrador oficial 0xD3: 0x0001 = Ligado, 0x0000 = Desligado (Bit 0, conforme SendWriteCmd2 em 0x4f0a54)
  static Uint8List setHeadlight(bool turnOn, {int destination = ScooterConstants.addrEsc}) {
    return buildWriteCommand(
      destination: destination,
      register: ScooterConstants.regHeadlight,
      value16: turnOn ? 0x0001 : 0x0000,
    );
  }

  /// Farol via Bit 7 (0x80) do registrador 0xD3 (conforme SendWriteCmd2 em 0x4f0cc0)
  static Uint8List setHeadlightBit7(bool turnOn, {int destination = ScooterConstants.addrEsc}) {
    return buildWriteCommand(
      destination: destination,
      register: ScooterConstants.regHeadlight,
      value16: turnOn ? 0x0080 : 0x0000,
    );
  }

  /// Farol com ambos os bits ativados (0x81 = Bit 0 + Bit 7)
  static Uint8List setHeadlightCombined(bool turnOn, {int destination = ScooterConstants.addrEsc}) {
    return buildWriteCommand(
      destination: destination,
      register: ScooterConstants.regHeadlight,
      value16: turnOn ? 0x0081 : 0x0000,
    );
  }

  /// Comando secundário de farol (compatibilidade com firmware alternativo 0xF2)
  static Uint8List setHeadlightAlt(bool turnOn, {int destination = ScooterConstants.addrEsc}) {
    return buildWriteCommand(
      destination: destination,
      register: ScooterConstants.regHeadlightAlt,
      value16: turnOn ? 0x0001 : 0x0000,
    );
  }

  /// Comando para ativar ou desativar a trava eletrônica antifurto
  /// Registrador oficial 0x70: 0x0001 = Travado, 0x0000 = Destravado
  static Uint8List setMotorLock(bool lock, {int destination = ScooterConstants.addrEsc}) {
    return buildWriteCommand(
      destination: destination,
      register: ScooterConstants.regLock,
      value16: lock ? 0x0001 : 0x0000,
    );
  }

  /// Comando para alterar o modo de velocidade
  /// Registrador oficial 0x7E: 0x0000 = Eco, 0x0001 = Drive, 0x0002 = Sport
  static Uint8List setSpeedMode(SpeedMode mode, {int destination = ScooterConstants.addrEsc}) {
    return buildWriteCommand(
      destination: destination,
      register: ScooterConstants.regSpeedMode,
      value16: mode.value,
    );
  }

  /// Comando para ativar ou desativar o piloto automático (Cruise Control)
  /// Registrador oficial 0x7C: 0x0001 = Ativado, 0x0000 = Desativado
  static Uint8List setCruiseControl(bool enabled, {int destination = ScooterConstants.addrEsc}) {
    return buildWriteCommand(
      destination: destination,
      register: ScooterConstants.regCruiseControl,
      value16: enabled ? 0x0001 : 0x0000,
    );
  }

  /// Comando para definir o modo de partida (Zero-Start vs Kick-Start)
  /// Registrador oficial 0x7D: 0x0001 = Zero Start direto, 0x0000 = Kick-start
  static Uint8List setStartMode(StartMode mode, {int destination = ScooterConstants.addrEsc}) {
    return buildWriteCommand(
      destination: destination,
      register: ScooterConstants.regStartMode,
      value16: mode == StartMode.zeroStart ? 0x0001 : 0x0000,
    );
  }

  /// Comando para ajustar a velocidade máxima limite permitida em km/h
  /// Registrador 0x72
  static Uint8List setSpeedLimit(int maxKmh, {int destination = ScooterConstants.addrEsc}) {
    final int clampedSpeed = maxKmh.clamp(5, 45);
    return buildWriteCommand(
      destination: destination,
      register: ScooterConstants.regSpeedLimit,
      value16: clampedSpeed,
    );
  }

  /// Consulta do bloco principal de telemetria M0Robot (0xC2, 44 bytes)
  /// Retorna velocidade instantânea, tensão de bateria em 0.1V, corrente e luzes
  static Uint8List queryTelemetryC2({int destination = ScooterConstants.addrEsc}) {
    return buildReadCommand(
      destination: destination,
      register: ScooterConstants.regTelemetryC2,
      length: ScooterConstants.telemetryC2Length,
    );
  }

  /// Consulta do bloco de status e odometria (0x1A, 52 bytes)
  /// Retorna odômetro parcial, total, modos e flags
  static Uint8List queryStatus1A({int destination = ScooterConstants.addrEsc}) {
    return buildReadCommand(
      destination: destination,
      register: ScooterConstants.regStatus1A,
      length: ScooterConstants.status1ALength,
    );
  }

  /// Consulta de telemetria secundária (0xEA, 16 bytes)
  static Uint8List querySecondaryEA({int destination = ScooterConstants.addrEsc}) {
    return buildReadCommand(
      destination: destination,
      register: ScooterConstants.regSecondaryEA,
      length: ScooterConstants.secondaryEALength,
    );
  }

  /// Consulta do bloco de configurações e estados (0x6C, 52 bytes)
  /// Retorna Trava (0x70), Cruise (0x7C), Zero-Start (0x7D), Modo de Condução (0x7E)
  static Uint8List queryConfig6C({int destination = ScooterConstants.addrEsc}) {
    return buildReadCommand(
      destination: destination,
      register: ScooterConstants.regConfig6C,
      length: ScooterConstants.config6CLength,
    );
  }

  /// Consulta de identificação do painel/dashboard (0x14, 16 bytes do destino 0x21)
  static Uint8List queryDash14({int destination = ScooterConstants.addrBle}) {
    return buildReadCommand(
      destination: destination,
      register: ScooterConstants.regDash14,
      length: ScooterConstants.dash14Length,
    );
  }

  /// Consulta legada de telemetria (0xB0, 28 bytes)
  static Uint8List queryTelemetry({int destination = ScooterConstants.addrMiniRobot}) {
    return buildReadCommand(
      destination: destination,
      register: ScooterConstants.regTelemetryGeneral,
      length: ScooterConstants.telemetryGeneralLength,
    );
  }

  /// Requisição de versão do firmware (Handshake e keep-alive)
  static Uint8List queryVersion({int destination = ScooterConstants.addrEsc}) {
    return buildReadCommand(
      destination: destination,
      register: ScooterConstants.regVersion,
      length: 0x02,
    );
  }

  /// Requisição do número de série do veículo
  static Uint8List querySerial({int destination = ScooterConstants.addrEsc}) {
    return buildReadCommand(
      destination: destination,
      register: ScooterConstants.regSerial,
      length: 0x0E,
    );
  }
}
