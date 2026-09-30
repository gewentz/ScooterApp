enum TrafficDirection { tx, rx }

/// Registro de tráfego BLE binário para interceptação e análise
class BleTrafficLog {
  final DateTime timestamp;
  final TrafficDirection direction;
  final List<int> bytes;
  final String? note;
  final String? characteristicUuid;
  final String? taggedFunction;

  BleTrafficLog({
    required this.timestamp,
    required this.direction,
    required this.bytes,
    this.note,
    this.characteristicUuid,
    this.taggedFunction,
  });

  /// Representação dos bytes em formato HEX (ex: "55 AA 04 06 03 72 01 00 7F FF")
  String get hexString =>
      bytes.map((b) => b.toRadixString(16).padLeft(2, '0').toUpperCase()).join(' ');

  /// Representação ASCII dos bytes imprimíveis
  String get asciiString =>
      bytes.map((b) => (b >= 32 && b <= 126) ? String.fromCharCode(b) : '.').join('');

  /// Retorna o nome da direção amigável
  String get directionLabel =>
      direction == TrafficDirection.tx ? 'TX (App -> Scooter)' : 'RX (Scooter -> App)';

  /// Verifica se o pacote possui cabeçalho válido do padrão MiniRobot / M365 (0x55 0xAA ou 0x5A 0xA5)
  bool get hasValidHeader =>
      bytes.length >= 2 &&
      ((bytes[0] == 0x55 && bytes[1] == 0xAA) || (bytes[0] == 0x5A && bytes[1] == 0xA5));

  /// Resumo amigável decodificado do frame
  String get parsedSummary {
    if (!hasValidHeader) {
      return 'Raw Bytes (${bytes.length} bytes)';
    }
    if (bytes.length < 6) {
      return 'Frame Incompleto (${bytes.length} bytes)';
    }

    final len = bytes[2];
    final dest = bytes[3];
    final cmd = bytes[4];
    final reg = bytes[5];

    final destName = switch (dest) {
      0x06 => 'MiniRobot',
      0x20 => 'ESC (Motor)',
      0x21 => 'BMS (Bateria)',
      0x22 => 'Dashboard',
      0x23 => 'App / Ble',
      _ => 'Addr 0x${dest.toRadixString(16).padLeft(2, '0').toUpperCase()}',
    };

    final cmdName = switch (cmd) {
      0x01 => 'Read',
      0x02 => 'ReadResp',
      0x03 => 'Write',
      0x04 => 'WriteAck',
      _ => 'Cmd 0x${cmd.toRadixString(16).padLeft(2, '0').toUpperCase()}',
    };

    final regName = switch (reg) {
      0xB0 => 'Telemetria Geral (0xB0)',
      0x70 => 'Travar Motor (0x70)',
      0x71 => 'Destravar Motor (0x71)',
      0x72 => 'Farol / Luz (0x72)',
      0x73 => 'Modo Velocidade (0x73)',
      0x75 => 'Piloto Automático (0x75)',
      0x76 => 'Modo Partida (0x76)',
      0x7B => 'Limite Velocidade (0x7B)',
      0x1A => 'Versão Firmware (0x1A)',
      0x10 => 'Serial Number (0x10)',
      _ => 'Reg 0x${reg.toRadixString(16).padLeft(2, '0').toUpperCase()}',
    };

    return 'Dest: $destName | $cmdName | $regName (Len: $len)';
  }

  BleTrafficLog copyWith({
    DateTime? timestamp,
    TrafficDirection? direction,
    List<int>? bytes,
    String? note,
    String? characteristicUuid,
    String? taggedFunction,
  }) {
    return BleTrafficLog(
      timestamp: timestamp ?? this.timestamp,
      direction: direction ?? this.direction,
      bytes: bytes ?? this.bytes,
      note: note ?? this.note,
      characteristicUuid: characteristicUuid ?? this.characteristicUuid,
      taggedFunction: taggedFunction ?? this.taggedFunction,
    );
  }
}
