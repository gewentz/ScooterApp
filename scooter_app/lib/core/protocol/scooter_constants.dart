/// Constantes do protocolo BLE e registradores da controladora M0 / MiniRobot
class ScooterConstants {
  ScooterConstants._();

  // --------------------------------------------------------------------------
  // IDENTIFICADORES BLUETOOTH (BLE)
  // --------------------------------------------------------------------------

  /// Prefixo oficial detectado no dispositivo do usuário
  static const String targetDevicePrefix = 'M0Robot';

  /// Prefixos adicionais de patinetes compatíveis com a mesma controladora
  static const List<String> supportedPrefixes = [
    'M0Robot',
    'M0',
    'MiniRobot',
    'E9PRO',
    'E9G',
    'E9',
    'MAX',
    'E12',
    'E-WHEELS',
    'TECAR',
    'XRIDER',
    'EO STREET',
    'i10',
    'NEXRIDE',
    'XT-',
    'SPARK',
    'KING',
    'SFSO',
    'Lebi',
    'Scooter',
    'X-Bot',
    'HM-',
    'BLE',
  ];

  /// UUID do Serviço Serial HM-10 (comum em placas M0Robot)
  static const String serviceSerialStandard = '0000ffe0-0000-1000-8000-00805f9b34fb';

  /// UUID da Característica Serial TX/RX (Write + Notify)
  static const String charSerialStandard = '0000ffe1-0000-1000-8000-00805f9b34fb';

  /// UUID do Serviço Nordic UART (alternativa comum em placas nRF)
  static const String serviceNordicUart = '6e400001-b5a3-f393-e0a9-e50e24dcca9e';

  /// UUID de Escrita no Nordic UART (App -> Scooter)
  static const String charNordicUartTx = '6e400002-b5a3-f393-e0a9-e50e24dcca9e';

  /// UUID de Notificação no Nordic UART (Scooter -> App)
  static const String charNordicUartRx = '6e400003-b5a3-f393-e0a9-e50e24dcca9e';

  // --------------------------------------------------------------------------
  // CABEÇALHOS DO FRAME DE PROTOCOLO (NINEBOT / LEBITEC)
  // --------------------------------------------------------------------------

  /// Primeiro byte de cabeçalho padrão
  static const int headerByte0 = 0x55;

  /// Segundo byte de cabeçalho padrão
  static const int headerByte1 = 0xAA;

  /// Primeiro byte de cabeçalho alternativo
  static const int altHeaderByte0 = 0x5A;

  /// Segundo byte de cabeçalho alternativo
  static const int altHeaderByte1 = 0xA5;

  // --------------------------------------------------------------------------
  // ENDEREÇOS DE BARRAMENTO (DESTINO / ORIGEM)
  // --------------------------------------------------------------------------

  /// Endereço padrão da controladora de patinete MiniRobot / LebiTEC (Scooter ESC)
  /// Mapeado via descompilação do ARM64 de SendWriteCmd2 e SendReadCmdWithAddr2 (0x0A)
  static const int addrMiniRobotEsc = 0x0A;
  static const int addrEsc = 0x0A;
  static const int addrMiniRobot = 0x0A;
  static const int addrEscNinebot = 0x20;
  static const int addrMiniRobotAlt = 0x06;

  /// Painel / Módulo Bluetooth (Dashboard)
  static const int addrBle = 0x21;
  static const int addrDashboard = 0x21;

  /// Sistema de Gerenciamento da Bateria (BMS)
  static const int addrBms = 0x22;

  /// Identificador do Aplicativo (Master / Host)
  /// Identificador do Aplicativo (Master / Host receptor - 0x23)
  static const int addrApp = 0x23;

  // --------------------------------------------------------------------------
  // TIPOS DE COMANDO (NINEBOT / MINIROBOT UART)
  // --------------------------------------------------------------------------

  /// Leitura de registradores (Master -> ESC)
  static const int cmdRead = 0x01;

  /// Resposta de leitura de registradores (ESC -> Master)
  static const int cmdReadResponse = 0x01;

  /// Escrita de parâmetros em registradores (Master -> ESC)
  static const int cmdWrite = 0x03;

  /// Confirmação de escrita (ESC -> Master ACK)
  static const int cmdWriteResponse = 0x04;

  /// Resposta de dados / telemetria periódica
  static const int cmdTelemetryQuery = 0x64;

  /// Resposta do comando de telemetria
  static const int cmdTelemetryResponse = 0x65;

  // --------------------------------------------------------------------------
  // REGISTRADORES OFICIAIS (REVERSA SENCOR / M0ROBOT NATIVE C++)
  // --------------------------------------------------------------------------

  /// Farol Principal: Registrador 0xD3 (Bit 7: 0x80 = Ligado, 0x00 = Desligado)
  static const int regHeadlight = 0xD3;
  static const int regHeadlightAlt = 0xF2; // Registrador secundário em modelos E9/HB

  /// Trava do Motor: Registrador 0x70 (Valor: 0x0001 = Travado, 0x0000 = Destravado)
  static const int regLock = 0x70;

  /// Modo de Condução: Registrador 0x7E (0x0000 = Eco, 0x0001 = Drive, 0x0002 = Sport)
  static const int regSpeedMode = 0x7E;

  /// Piloto Automático (Cruise Control / DSXH): Registrador 0x7C (0x0001 = Ligado, 0x0000 = Desligado)
  static const int regCruiseControl = 0x7C;

  /// Modo de Partida (Zero-Start / LQD): Registrador 0x7D (0x0001 = Zero Start, 0x0000 = Kick-to-start)
  static const int regStartMode = 0x7D;

  /// Ajuste do limite máximo de velocidade em km/h
  static const int regSpeedLimit = 0x72;

  /// Telemetria Principal M0Robot (Registrador 0xC2, 44 bytes de dados = 22 words)
  /// Contém: Velocidade, Tensão da Bateria (0.1V), Corrente, Status do Farol
  static const int regTelemetryC2 = 0xC2;
  static const int telemetryC2Length = 0x2C; // 44 bytes

  /// Bloco de Status e Odometria (Registrador 0x1A, 52 bytes de dados = 26 words)
  /// Contém: Modos, Odômetro total e parcial, Flags de segurança
  static const int regStatus1A = 0x1A;
  static const int status1ALength = 0x34; // 52 bytes

  /// Telemetria Secundária e Limites de Velocidade (Registrador 0xEA, 16 a 32 bytes)
  static const int regSecondaryEA = 0xEA;
  static const int secondaryEALength = 0x10; // 16 bytes

  /// Bloco de Configurações e Estados (Registrador 0x6C, 52 bytes de dados = 26 words)
  /// Contém: Trava (0x70), Cruise (0x7C), Zero-Start (0x7D), Modo de Condução (0x7E)
  static const int regConfig6C = 0x6C;
  static const int config6CLength = 0x34; // 52 bytes

  /// Identificação do Painel / Dashboard (Registrador 0x14 do destino 0x21, 16 bytes)
  /// Contém MAC address invertido e firmware do painel
  static const int regDash14 = 0x14;
  static const int dash14Length = 0x10;

  /// Registrador de início da leitura de telemetria geral legada (0xB0)
  static const int regTelemetryGeneral = 0xB0;
  static const int telemetryGeneralLength = 0x1C; // 28 bytes

  /// Versão do firmware (handshake inicial)
  static const int regVersion = 0x1A;

  /// Número de série do veículo
  static const int regSerial = 0x10;
}
