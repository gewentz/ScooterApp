import '../protocol/scooter_constants.dart';

/// Representação de um patinete elétrico detectado na varredura BLE
class ScooterDevice {
  /// Nome de transmissão Bluetooth (ex: "M0RobotDFE54C32BA")
  final String name;

  /// Endereço MAC ou UUID do dispositivo BLE
  final String id;

  /// Intensidade do sinal em dBm (RSSI)
  final int rssi;

  /// Referência original da plataforma (BluetoothDevice)
  final dynamic nativeDevice;

  /// Indica se o dispositivo anuncia explicitamente um serviço UART compatível (HM-10, Nordic, E9/MAX)
  final bool hasKnownService;

  const ScooterDevice({
    required this.name,
    required this.id,
    required this.rssi,
    this.nativeDevice,
    this.hasKnownService = false,
  });

  /// Verifica se é o dispositivo alvo oficial detectado
  bool get isOfficialTarget =>
      name.toUpperCase().startsWith('M0ROBOT') || name.toUpperCase().startsWith('M0');

  /// Verifica se possui prefixo compatível conhecido ou anuncia serviço correspondente
  bool get isCompatible =>
      hasKnownService ||
      isOfficialTarget ||
      ScooterConstants.supportedPrefixes.any((prefix) => name.toUpperCase().startsWith(prefix.toUpperCase()));

  @override
  String toString() => 'ScooterDevice(name: $name, id: $id, rssi: $rssi dBm)';

  @override
  bool operator ==(Object other) =>
      identical(this, other) ||
      other is ScooterDevice && runtimeType == other.runtimeType && id == other.id;

  @override
  int get hashCode => id.hashCode;
}
