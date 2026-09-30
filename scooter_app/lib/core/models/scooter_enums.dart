/// Modos de velocidade suportados pela controladora M0 / MiniRobot
enum SpeedMode {
  eco(0, 'Eco (15 km/h)', 15),
  drive(1, 'Drive / Conforto (20 km/h)', 20),
  sport(2, 'Sport (25-30 km/h)', 30);

  final int value;
  final String label;
  final int maxSpeedKmh;

  const SpeedMode(this.value, this.label, this.maxSpeedKmh);

  static SpeedMode fromValue(int val) {
    return SpeedMode.values.firstWhere(
      (m) => m.value == val,
      orElse: () => SpeedMode.drive,
    );
  }
}

/// Modo de partida do motor
enum StartMode {
  /// Exige impulso com o pé para armar o motor (mais seguro)
  kickToStart(0, 'Kick-to-Start (com impulso)'),

  /// Acelera parado desde 0 km/h
  zeroStart(1, 'Zero-Start (partida direta)');

  final int value;
  final String label;

  const StartMode(this.value, this.label);

  static StartMode fromValue(int val) {
    return StartMode.values.firstWhere(
      (m) => m.value == val,
      orElse: () => StartMode.kickToStart,
    );
  }
}

/// Estado do farol / lanterna
enum HeadlightState {
  off(0, 'Desligado'),
  on(1, 'Ligado');

  final int value;
  final String label;

  const HeadlightState(this.value, this.label);

  static HeadlightState fromValue(int val) =>
      val == 1 ? HeadlightState.on : HeadlightState.off;
}

/// Estado da trava eletrônica do motor
enum LockState {
  unlocked(0, 'Destravado'),
  locked(1, 'Travado (Anti-furto)');

  final int value;
  final String label;

  const LockState(this.value, this.label);

  static LockState fromValue(int val) =>
      val == 1 ? LockState.locked : LockState.unlocked;
}

/// Estado da conexão BLE
enum BleConnectionStatus {
  disconnected,
  scanning,
  connecting,
  connected,
  ready,
  error;
}
