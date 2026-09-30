import 'scooter_enums.dart';

/// Modelo de telemetria em tempo real recebida do patinete elétrico M0Robot
class ScooterTelemetry {
  /// Velocidade atual instantânea em km/h
  final double speedKmh;

  /// Nível de carga da bateria (0 a 100%)
  final int batteryPercent;

  /// Tensão instantânea da bateria em Volts (ex: 41.5 V)
  final double batteryVoltageVolts;

  /// Corrente consumida pelo motor em Amperes (positiva = acelerando, negativa = regeneração)
  final double currentAmperes;

  /// Temperatura interna da placa controladora em °C
  final double temperatureCelsius;

  /// Distância percorrida na viagem atual (odômetro parcial) em km
  final double tripDistanceKm;

  /// Quilometragem total acumulada pelo patinete (odômetro total) em km
  final double totalDistanceKm;

  /// Estado do farol dianteiro / lanterna
  final bool isHeadlightOn;

  /// Se a trava eletrônica antifurto está acionada
  final bool isLocked;

  /// Se o piloto automático (Cruise Control) está ativado
  final bool isCruiseControlEnabled;

  /// Modo de velocidade selecionado (Eco, Drive, Sport)
  final SpeedMode speedMode;

  /// Modo de partida configurado (Zero-Start ou Kick-to-Start)
  final StartMode startMode;

  /// Código de erro reportado pelo veículo (0 = sem erros)
  final int errorCode;

  /// Momento exato da leitura
  final DateTime timestamp;

  const ScooterTelemetry({
    required this.speedKmh,
    required this.batteryPercent,
    required this.batteryVoltageVolts,
    required this.currentAmperes,
    required this.temperatureCelsius,
    required this.tripDistanceKm,
    required this.totalDistanceKm,
    required this.isHeadlightOn,
    required this.isLocked,
    required this.isCruiseControlEnabled,
    required this.speedMode,
    required this.startMode,
    required this.errorCode,
    required this.timestamp,
  });

  /// Estado inicial limpo (desconectado ou zerado)
  factory ScooterTelemetry.initial() {
    return ScooterTelemetry(
      speedKmh: 0.0,
      batteryPercent: 0,
      batteryVoltageVolts: 0.0,
      currentAmperes: 0.0,
      temperatureCelsius: 0.0,
      tripDistanceKm: 0.0,
      totalDistanceKm: 0.0,
      isHeadlightOn: false,
      isLocked: false,
      isCruiseControlEnabled: false,
      speedMode: SpeedMode.drive,
      startMode: StartMode.kickToStart,
      errorCode: 0,
      timestamp: DateTime.now(),
    );
  }

  /// Potência instantânea em Watts (P = V * I)
  double get powerWatts => batteryVoltageVolts * currentAmperes;

  /// Indica se o patinete está em movimento
  bool get isMoving => speedKmh > 0.5;

  /// Regra de segurança: só permite travar o motor se o patinete estiver 100% parado
  bool get isSafeToLock => speedKmh < 0.2;

  /// Cria cópia com valores alterados
  ScooterTelemetry copyWith({
    double? speedKmh,
    int? batteryPercent,
    double? batteryVoltageVolts,
    double? currentAmperes,
    double? temperatureCelsius,
    double? tripDistanceKm,
    double? totalDistanceKm,
    bool? isHeadlightOn,
    bool? isLocked,
    bool? isCruiseControlEnabled,
    SpeedMode? speedMode,
    StartMode? startMode,
    int? errorCode,
    DateTime? timestamp,
  }) {
    return ScooterTelemetry(
      speedKmh: speedKmh ?? this.speedKmh,
      batteryPercent: batteryPercent ?? this.batteryPercent,
      batteryVoltageVolts: batteryVoltageVolts ?? this.batteryVoltageVolts,
      currentAmperes: currentAmperes ?? this.currentAmperes,
      temperatureCelsius: temperatureCelsius ?? this.temperatureCelsius,
      tripDistanceKm: tripDistanceKm ?? this.tripDistanceKm,
      totalDistanceKm: totalDistanceKm ?? this.totalDistanceKm,
      isHeadlightOn: isHeadlightOn ?? this.isHeadlightOn,
      isLocked: isLocked ?? this.isLocked,
      isCruiseControlEnabled: isCruiseControlEnabled ?? this.isCruiseControlEnabled,
      speedMode: speedMode ?? this.speedMode,
      startMode: startMode ?? this.startMode,
      errorCode: errorCode ?? this.errorCode,
      timestamp: timestamp ?? this.timestamp,
    );
  }

  @override
  String toString() {
    return 'ScooterTelemetry(speed: ${speedKmh.toStringAsFixed(1)} km/h, '
        'battery: $batteryPercent% (${batteryVoltageVolts.toStringAsFixed(1)}V), '
        'power: ${powerWatts.toStringAsFixed(0)}W, '
        'mode: ${speedMode.label}, '
        'headlight: ${isHeadlightOn ? "ON" : "OFF"}, '
        'locked: ${isLocked ? "YES" : "NO"})';
  }
}
