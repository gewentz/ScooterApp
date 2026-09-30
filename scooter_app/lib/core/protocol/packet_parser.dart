import 'dart:typed_data';
import 'scooter_constants.dart';
import 'packet_builder.dart';
import '../models/scooter_enums.dart';
import '../models/scooter_telemetry.dart';

/// Analisador e decodificador de pacotes binários recebidos via BLE do M0Robot
class PacketParser {
  /// Buffer interno para acumular fluxos de bytes fragmentados
  final List<int> _buffer = [];

  /// Limpa o buffer de dados
  void clear() => _buffer.clear();

  /// Processa um bloco de bytes brutos recebido via notificação BLE
  /// Retorna uma lista de pacotes de telemetria decodificados (se houver frames completos)
  List<ScooterTelemetry> processIncomingBytes(List<int> incoming, {ScooterTelemetry? current}) {
    _buffer.addAll(incoming);
    final List<ScooterTelemetry> telemetries = [];

    while (_buffer.length >= 6) {
      // 1. Procurar pelo início do cabeçalho (0x55 0xAA ou 0x5A 0xA5)
      int headerIndex = -1;
      for (int i = 0; i < _buffer.length - 1; i++) {
        final b0 = _buffer[i];
        final b1 = _buffer[i + 1];
        if ((b0 == ScooterConstants.headerByte0 && b1 == ScooterConstants.headerByte1) ||
            (b0 == ScooterConstants.altHeaderByte0 && b1 == ScooterConstants.altHeaderByte1)) {
          headerIndex = i;
          break;
        }
      }

      // Se nenhum cabeçalho for encontrado, descartar dados espúrios e sair
      if (headerIndex == -1) {
        if (_buffer.length > 1) {
          _buffer.removeRange(0, _buffer.length - 1);
        }
        break;
      }

      // Descartar bytes residuais antes do cabeçalho
      if (headerIndex > 0) {
        _buffer.removeRange(0, headerIndex);
      }

      // Agora o buffer começa exatamente no cabeçalho
      if (_buffer.length < 3) break; // Esperar pelo byte de comprimento (Len)

      final int payloadLen = _buffer[2];
      // Tamanho total do pacote:
      // Header(2) + Len(1) + Destino(1) + Len (Cmd+Reg+Payload) + Checksum(2)
      final int totalFrameSize = 2 + 1 + 1 + payloadLen + 2;

      // Se o frame ainda não chegou por completo, aguardar próximas notificações BLE
      if (_buffer.length < totalFrameSize) {
        break;
      }

      // Extrair o frame completo
      final Uint8List frame = Uint8List.fromList(_buffer.sublist(0, totalFrameSize));

      // 2. Validar o Checksum
      final int checksumStart = 2;
      final int checksumLength = 1 + 1 + payloadLen;
      final int calculatedChecksum =
          PacketBuilder.calculateChecksum(frame, checksumStart, checksumLength);

      final int receivedChecksum =
          frame[totalFrameSize - 2] | (frame[totalFrameSize - 1] << 8);

      if (calculatedChecksum == receivedChecksum) {
        // Frame válido! Decodificar
        final ScooterTelemetry? telemetry = _decodeTelemetryFrame(frame, current: current);
        if (telemetry != null) {
          telemetries.add(telemetry);
        }
        // Remover o frame processado do buffer
        _buffer.removeRange(0, totalFrameSize);
      } else {
        // Checksum inválido: descarta apenas o primeiro byte do cabeçalho e tenta sincronizar de novo
        _buffer.removeAt(0);
      }
    }

    return telemetries;
  }

  /// Decodifica o payload de um frame válido
  ScooterTelemetry? _decodeTelemetryFrame(Uint8List frame, {ScooterTelemetry? current}) {
    try {
      // Offset de início dos dados do payload
      // frame[0..1]: Header (0x55 0xAA)
      // frame[2]: Len
      // frame[3]: Origem/Destino (0x23, 0x20, 0x21)
      // frame[4]: Comando (0x01, 0x02, 0x04)
      // frame[5]: Registrador base (ex: 0xB0)
      // frame[6..N]: Dados úteis
      final int register = frame[5];
      final int dataOffset = 6;
      final int dataLength = frame.length - 8; // Desconta Header(2)+Len(1)+Dest(1)+Cmd(1)+Reg(1)+Chk(2)

      if (dataLength <= 0) return null;

      final ByteData byteData = ByteData.sublistView(frame, dataOffset, dataOffset + dataLength);

      // Roteamento específico para cada registrador da controladora M0Robot
      switch (register) {
        case ScooterConstants.regTelemetryC2:
          return parseTelemetryC2Block(byteData, current: current);

        case ScooterConstants.regStatus1A:
          return parseStatus1ABlock(byteData, current: current);

        case ScooterConstants.regConfig6C:
          return parseConfig6CBlock(byteData, current: current);

        case ScooterConstants.regSecondaryEA:
          return parseSecondaryEABlock(byteData, current: current);

        case ScooterConstants.regTelemetryGeneral:
          return parseGeneralTelemetryBlock(byteData, current: current);

        default:
          if (dataLength >= 40) {
            // Bloco longo desconhecido: tenta decodificar como C2 se o registrador for compatível
            return parseTelemetryC2Block(byteData, current: current);
          }
          return parseSingleRegisterResponse(register, byteData, current: current);
      }
    } catch (_) {
      return null;
    }
  }

  /// Decodifica o bloco padrão de telemetria contínua das controladoras M0/MiniRobot
  /// Mapeamento de registradores conforme engenharia reversa do MiniRobot / LebiTEC:
  /// - Word 0 (0xB0, offset 0..1): Código de erro primário
  /// - Word 1 (0xB1, offset 2..3): Código de erro secundário
  /// - Word 2 (0xB2, offset 4..5): Flags de status (Luz, Trava, Modo, Partida, Piloto)
  /// - Word 3 (0xB3, offset 6..7): Temperatura / Corrente
  /// - Word 4 (0xB4, offset 8..9): Nível da Bateria em porcentagem (0-100%)
  /// - Word 5 (0xB5, offset 10..11): Velocidade instantânea em km/h (bruto / 1000.0)
  /// - Word 6 (0xB6, offset 12..13): Velocidade média
  /// - Word 7 (0xB7, offset 14..15): Tensão da bateria (bruto / 100.0 V)
  /// - Words 8-9 (0xB8-0xB9, offset 16..19): Odômetro parcial (viagem atual)
  /// - Words 10-11 (0xBA-0xBB, offset 20..23): Odômetro total em metros
  static ScooterTelemetry parseGeneralTelemetryBlock(ByteData data, {ScooterTelemetry? current}) {
    final base = current ?? ScooterTelemetry.initial();

    int errorCode = base.errorCode;
    bool isHeadlightOn = base.isHeadlightOn;
    bool isLocked = base.isLocked;
    bool isCruiseControlEnabled = base.isCruiseControlEnabled;
    SpeedMode speedMode = base.speedMode;
    StartMode startMode = base.startMode;
    double temperature = base.temperatureCelsius;
    double currentAmps = base.currentAmperes;
    int batteryPercent = base.batteryPercent;
    double speedKmh = base.speedKmh;
    double batteryVoltage = base.batteryVoltageVolts;
    double tripDistanceKm = base.tripDistanceKm;
    double totalDistanceKm = base.totalDistanceKm;

    // 1. Código de erro (0xB0 e 0xB1)
    if (data.lengthInBytes >= 2) {
      final int err0 = data.getUint16(0, Endian.little);
      if (err0 != 0) errorCode = err0;
    }
    if (data.lengthInBytes >= 4) {
      final int err1 = data.getUint16(2, Endian.little);
      if (err1 != 0 && errorCode == 0) errorCode = err1;
    }

    // 2. Flags de Status (0xB2 - offset 4..5)
    if (data.lengthInBytes >= 6) {
      final int statusFlags = data.getUint16(4, Endian.little);
      isHeadlightOn = (statusFlags & 0x0001) != 0;
      isLocked = (statusFlags & 0x0002) != 0;
      isCruiseControlEnabled = (statusFlags & 0x0004) != 0;
      final int modeVal = (statusFlags >> 3) & 0x03;
      speedMode = SpeedMode.fromValue(modeVal);
      startMode = ((statusFlags & 0x0020) != 0) ? StartMode.zeroStart : StartMode.kickToStart;
    }

    // 3. Temperatura e Corrente (0xB3 - offset 6..7)
    if (data.lengthInBytes >= 8) {
      final int rawTemp = data.getInt16(6, Endian.little);
      if (rawTemp > 0 && rawTemp < 1500) {
        temperature = rawTemp > 150 ? rawTemp / 10.0 : rawTemp.toDouble();
      }
    }

    // 4. Bateria (0xB4 - offset 8..9 ou byte 8)
    if (data.lengthInBytes >= 9) {
      final int bVal8 = data.getUint8(8);
      if (bVal8 > 0 && bVal8 <= 100) {
        batteryPercent = bVal8;
      } else if (data.lengthInBytes >= 10) {
        final int bVal16 = data.getUint16(8, Endian.little);
        if (bVal16 > 0 && bVal16 <= 100) {
          batteryPercent = bVal16;
        }
      }
    }

    // 5. Velocidade Instantânea (0xB5 - offset 10..11)
    if (data.lengthInBytes >= 12) {
      final int rawSpeed = data.getInt16(10, Endian.little);
      if (rawSpeed >= 0) {
        speedKmh = rawSpeed > 1000 ? rawSpeed / 1000.0 : rawSpeed / 100.0;
      }
    }

    // 6. Tensão da Bateria (0xB7 - offset 14..15)
    if (data.lengthInBytes >= 16) {
      final int rawVolt = data.getUint16(14, Endian.little);
      if (rawVolt > 2000 && rawVolt < 7000) {
        batteryVoltage = rawVolt / 100.0;
      } else if (rawVolt > 200 && rawVolt < 700) {
        batteryVoltage = rawVolt / 10.0;
      }
    }

    // 7. Odômetro Parcial (0xB8-0xB9 - offset 16..19)
    if (data.lengthInBytes >= 20) {
      final int rawTrip = data.getUint32(16, Endian.little);
      tripDistanceKm = rawTrip / 1000.0;
    } else if (data.lengthInBytes >= 18) {
      final int rawTrip = data.getUint16(16, Endian.little);
      tripDistanceKm = rawTrip / 1000.0;
    }

    // 8. Odômetro Total (0xBA-0xBB - offset 20..23)
    if (data.lengthInBytes >= 24) {
      final int rawTotal = data.getUint32(20, Endian.little);
      totalDistanceKm = rawTotal / 1000.0;
    }

    // Fallback: se a bateria ainda for 0 e data tiver byte 6 ou 7 com porcentagem
    if (batteryPercent == 0 && data.lengthInBytes >= 7) {
      final fallbackB = data.getUint8(6);
      if (fallbackB > 0 && fallbackB <= 100) {
        batteryPercent = fallbackB;
      }
    }

    return ScooterTelemetry(
      speedKmh: speedKmh,
      batteryPercent: batteryPercent,
      batteryVoltageVolts: batteryVoltage,
      currentAmperes: currentAmps,
      temperatureCelsius: temperature,
      tripDistanceKm: tripDistanceKm,
      totalDistanceKm: totalDistanceKm,
      isHeadlightOn: isHeadlightOn,
      isLocked: isLocked,
      isCruiseControlEnabled: isCruiseControlEnabled,
      speedMode: speedMode,
      startMode: startMode,
      errorCode: errorCode,
      timestamp: DateTime.now(),
    );
  }

  /// Decodifica o bloco principal de telemetria M0Robot (0xC2, 44 bytes de dados = 22 words de 16 bits)
  /// Conforme captura real do patinete e reversa do C++ da SENCOR:
  /// - Word 0 (0xC2, offset 0..1): Corrente do motor (em 0.01A)
  /// - Word 2 (0xC4, offset 4..5): Bateria em % (ex: 45%)
  /// - Word 4 (0xC6, offset 8..9): Tensão da bateria em 0.1V (ex: 351 -> 35.1V)
  /// - Word 11 (0xCD, offset 22..23): Temperatura em °C (ex: 17°C)
  /// - Word 17 (0xD3, offset 34..35): Farol e lanterna ((val & 0x80) != 0 || (val & 0x01) != 0)
  /// - Word 21 (0xD7, offset 42..43): Velocidade limite (ex: 30000 -> 30 km/h)
  static ScooterTelemetry parseTelemetryC2Block(ByteData data, {ScooterTelemetry? current}) {
    final base = current ?? ScooterTelemetry.initial();

    double currentAmps = base.currentAmperes;
    int batteryPercent = base.batteryPercent;
    double batteryVoltage = base.batteryVoltageVolts;
    double temperature = base.temperatureCelsius;
    double speedKmh = base.speedKmh;
    bool isHeadlightOn = base.isHeadlightOn;

    // Word 0 (0xC2): Corrente (0.01A)
    if (data.lengthInBytes >= 2) {
      final int rawCur = data.getInt16(0, Endian.little);
      if (rawCur.abs() < 10000) {
        currentAmps = rawCur / 100.0;
      }
    }

    // Word 2 (0xC4): Nível da Bateria em %
    if (data.lengthInBytes >= 6) {
      final int bVal = data.getUint16(4, Endian.little);
      if (bVal >= 0 && bVal <= 100) {
        batteryPercent = bVal;
      }
    }

    // Word 4 (0xC6): Tensão da Bateria (0.1V)
    if (data.lengthInBytes >= 10) {
      final int vVal = data.getUint16(8, Endian.little);
      if (vVal > 200 && vVal < 700) {
        batteryVoltage = vVal / 10.0; // Ex: 351 -> 35.1 V
      } else if (vVal > 2000 && vVal < 7000) {
        batteryVoltage = vVal / 100.0;
      }
    }

    // Word 11 (0xCD): Temperatura em °C
    if (data.lengthInBytes >= 24) {
      final int tempVal = data.getInt16(22, Endian.little);
      if (tempVal > -20 && tempVal < 120) {
        temperature = tempVal.toDouble();
      }
    }

    // Word 17 (0xD3): Farol
    if (data.lengthInBytes >= 36) {
      final int lightVal = data.getUint16(34, Endian.little);
      isHeadlightOn = (lightVal & 0x80) != 0 || (lightVal & 0x01) != 0;
    }

    return base.copyWith(
      currentAmperes: currentAmps,
      batteryPercent: batteryPercent,
      batteryVoltageVolts: batteryVoltage,
      temperatureCelsius: temperature,
      isHeadlightOn: isHeadlightOn,
      speedKmh: speedKmh,
    );
  }

  /// Decodifica o bloco de status e odometria M0Robot (0x1A, 52 bytes de dados = 26 words)
  /// Conforme firmware oficial SENCOR / M0 e dump capturado:
  /// - Word 1 (0x1B, offset 2..3): Odômetro parcial (trip em 0.1 km)
  /// - Word 2 (0x1C, offset 4..5): Odômetro total em 0.1 km
  /// - Word 7 (0x21, offset 14..15): Modo de velocidade
  /// - Word 8 (0x22, offset 16..17): Nível de Bateria em porcentagem (0-100%) [Ex: 54 = 54%]
  /// - Word 12 (0x26, offset 24..25): Velocidade instantânea em km/h [RefreshMainPage4]
  /// - Word 24 (0x32, offset 48..49): Odômetro acumulado secundário
  static ScooterTelemetry parseStatus1ABlock(ByteData data, {ScooterTelemetry? current}) {
    final base = current ?? ScooterTelemetry.initial();

    SpeedMode speedMode = base.speedMode;
    int batteryPercent = base.batteryPercent;
    double speedKmh = base.speedKmh;
    double tripDistanceKm = base.tripDistanceKm;
    double totalDistanceKm = base.totalDistanceKm;

    // Word 1 (0x1B): Odômetro parcial
    if (data.lengthInBytes >= 4) {
      final int tripRaw = data.getUint16(2, Endian.little);
      tripDistanceKm = tripRaw / 10.0;
    }

    // Word 2 (0x1C): Odômetro total
    if (data.lengthInBytes >= 6) {
      final int odoRaw = data.getUint16(4, Endian.little);
      totalDistanceKm = odoRaw / 10.0;
    }

    // Word 7 (0x21): Modo de condução
    if (data.lengthInBytes >= 16) {
      final int mVal = data.getUint16(14, Endian.little);
      if (mVal >= 0 && mVal <= 3) {
        speedMode = SpeedMode.fromValue(mVal);
      }
    }

    // Word 8 (0x22): Nível da Bateria em porcentagem (ex: 54 = 54%)
    if (data.lengthInBytes >= 18) {
      final int bVal = data.getUint16(16, Endian.little);
      if (bVal > 0 && bVal <= 100) {
        batteryPercent = bVal;
      }
    }

    // Word 11 (0x25) e Word 12 (0x26): Velocidade Instantânea em km/h (C++ RefreshMainPage4 / MapData 0x25 e 0x26)
    if (data.lengthInBytes >= 24) {
      final int rawSpeed11 = data.getInt16(22, Endian.little);
      if (rawSpeed11 > 0 && rawSpeed11 < 100000) {
        speedKmh = rawSpeed11 > 1000 ? rawSpeed11 / 1000.0 : (rawSpeed11 > 100 ? rawSpeed11 / 100.0 : rawSpeed11.toDouble());
      }
    }
    if (speedKmh == 0 && data.lengthInBytes >= 26) {
      final int rawSpeed12 = data.getInt16(24, Endian.little);
      if (rawSpeed12 > 0 && rawSpeed12 < 100000) {
        speedKmh = rawSpeed12 > 1000 ? rawSpeed12 / 1000.0 : (rawSpeed12 > 100 ? rawSpeed12 / 100.0 : rawSpeed12.toDouble());
      }
    }

    // Word 24 (0x32): Odômetro secundário se total ainda for 0
    if (totalDistanceKm == 0 && data.lengthInBytes >= 50) {
      final int odoRaw2 = data.getUint16(48, Endian.little);
      if (odoRaw2 > 0) {
        totalDistanceKm = odoRaw2 / 10.0;
      }
    }

    return base.copyWith(
      speedMode: speedMode,
      batteryPercent: batteryPercent,
      speedKmh: speedKmh,
      tripDistanceKm: tripDistanceKm,
      totalDistanceKm: totalDistanceKm,
    );
  }

  /// Decodifica o bloco de configurações e estados M0Robot (0x6C, 52 bytes = 26 words)
  /// Conforme captura real do patinete e UserInterface da SENCOR:
  /// - Word 4 (0x70, offset 8..9): Trava antifurto do motor (1 = Travado, 0 = Destravado)
  /// - Word 16 (0x7C, offset 32..33): Piloto Automático (Cruise Control, 1 = Ligado, 0 = Desligado)
  /// - Word 17 (0x7D, offset 34..35): Modo de Partida (1 = Zero Start, 0 = Kick-start)
  /// - Word 18 (0x7E, offset 36..37): Modo de Velocidade (0 = Eco, 1 = Drive, 2 = Sport)
  static ScooterTelemetry parseConfig6CBlock(ByteData data, {ScooterTelemetry? current}) {
    final base = current ?? ScooterTelemetry.initial();

    bool isLocked = base.isLocked;
    bool isCruiseControlEnabled = base.isCruiseControlEnabled;
    StartMode startMode = base.startMode;
    SpeedMode speedMode = base.speedMode;

    // Word 4 (0x70): Trava do motor
    if (data.lengthInBytes >= 10) {
      final int lockVal = data.getUint16(8, Endian.little);
      isLocked = lockVal == 1;
    }

    // Word 16 (0x7C): Piloto automático (DSXH)
    if (data.lengthInBytes >= 34) {
      final int cruiseVal = data.getUint16(32, Endian.little);
      isCruiseControlEnabled = cruiseVal == 1;
    }

    // Word 17 (0x7D): Modo de partida (LQD)
    if (data.lengthInBytes >= 36) {
      final int startVal = data.getUint16(34, Endian.little);
      startMode = (startVal == 1) ? StartMode.zeroStart : StartMode.kickToStart;
    }

    // Word 18 (0x7E): Modo de velocidade
    if (data.lengthInBytes >= 38) {
      final int modeVal = data.getUint16(36, Endian.little);
      if (modeVal >= 0 && modeVal <= 3) {
        speedMode = SpeedMode.fromValue(modeVal);
      }
    }

    return base.copyWith(
      isLocked: isLocked,
      isCruiseControlEnabled: isCruiseControlEnabled,
      startMode: startMode,
      speedMode: speedMode,
    );
  }

  /// Decodifica o bloco secundário EA (velocidade e limites)
  static ScooterTelemetry parseSecondaryEABlock(ByteData data, {ScooterTelemetry? current}) {
    final base = current ?? ScooterTelemetry.initial();

    double speedKmh = base.speedKmh;
    double batteryVoltage = base.batteryVoltageVolts;

    // Word 7 (0xF1, offset 14..15): Tensão
    if (data.lengthInBytes >= 16) {
      final int vVal = data.getUint16(14, Endian.little);
      if (vVal > 200 && vVal < 700) {
        batteryVoltage = vVal / 10.0;
      }
    }

    return base.copyWith(
      speedKmh: speedKmh,
      batteryVoltageVolts: batteryVoltage,
    );
  }

  /// Decodifica a resposta de leitura de um único registrador preservando o estado existente
  static ScooterTelemetry? parseSingleRegisterResponse(
    int register,
    ByteData data, {
    ScooterTelemetry? current,
  }) {
    if (data.lengthInBytes < 1) return null;

    final base = current ?? ScooterTelemetry.initial();

    switch (register) {
      case ScooterConstants.regHeadlight:
        final val = data.lengthInBytes >= 2 ? data.getUint16(0, Endian.little) : data.getUint8(0);
        return base.copyWith(isHeadlightOn: (val & 0x80) != 0 || (val & 0x01) != 0);
      case ScooterConstants.regHeadlightAlt:
        return base.copyWith(isHeadlightOn: data.getUint8(0) == 1);
      case ScooterConstants.regLock:
        final val = data.lengthInBytes >= 2 ? data.getUint16(0, Endian.little) : data.getUint8(0);
        return base.copyWith(isLocked: val == 1);
      case ScooterConstants.regSpeedMode:
        final val = data.lengthInBytes >= 2 ? data.getUint16(0, Endian.little) : data.getUint8(0);
        return base.copyWith(speedMode: SpeedMode.fromValue(val));
      case ScooterConstants.regCruiseControl:
        final val = data.lengthInBytes >= 2 ? data.getUint16(0, Endian.little) : data.getUint8(0);
        return base.copyWith(isCruiseControlEnabled: val == 1);
      case ScooterConstants.regStartMode:
        final val = data.lengthInBytes >= 2 ? data.getUint16(0, Endian.little) : data.getUint8(0);
        return base.copyWith(startMode: val == 1 ? StartMode.zeroStart : StartMode.kickToStart);
      case 0xB4: // Velocidade instantânea (C++ RefreshMainPage)
        if (data.lengthInBytes >= 2) {
          final int rawSpeed = data.getInt16(0, Endian.little);
          final double speed = rawSpeed > 1000 ? rawSpeed / 1000.0 : (rawSpeed > 100 ? rawSpeed / 100.0 : rawSpeed.toDouble());
          return base.copyWith(speedKmh: speed);
        }
        return null;
      default:
        return null;
    }
  }
}
