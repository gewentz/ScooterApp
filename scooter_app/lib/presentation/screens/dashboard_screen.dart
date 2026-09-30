import 'package:flutter/material.dart';
import '../../core/ble/scooter_ble_service.dart';
import '../../core/models/scooter_enums.dart';
import '../../core/models/scooter_telemetry.dart';
import '../theme/app_theme.dart';
import '../widgets/battery_indicator.dart';
import '../widgets/quick_control_bar.dart';
import '../widgets/speedometer_gauge.dart';
import '../widgets/telemetry_card.dart';
import 'scan_screen.dart';
import 'settings_screen.dart';
import 'interceptor_screen.dart';

/// Tela Principal (Cockpit / Dashboard) de controle do patinete elétrico
class DashboardScreen extends StatelessWidget {
  final ScooterBleService bleService;

  const DashboardScreen({super.key, required this.bleService});

  void _executeWithSafety(
    BuildContext context,
    bool isConnected,
    VoidCallback action,
  ) {
    if (!isConnected) {
      ScaffoldMessenger.of(context).showSnackBar(
        const SnackBar(
          content: Text('Conecte ao patinete para enviar comandos!'),
          backgroundColor: AppTheme.lockCrimson,
          duration: Duration(seconds: 2),
        ),
      );
      return;
    }
    action();
  }

  @override
  Widget build(BuildContext context) {
    return StreamBuilder<BleConnectionStatus>(
      stream: bleService.statusStream,
      initialData: bleService.currentStatus,
      builder: (context, statusSnapshot) {
        final status = statusSnapshot.data ?? BleConnectionStatus.disconnected;
        final isConnected = status == BleConnectionStatus.ready;

        return StreamBuilder<ScooterTelemetry>(
          stream: bleService.telemetryStream,
          initialData: bleService.currentTelemetry,
          builder: (context, telemetrySnapshot) {
            final telemetry = telemetrySnapshot.data ?? ScooterTelemetry.initial();

            return Scaffold(
              appBar: AppBar(
                title: Row(
                  mainAxisSize: MainAxisSize.min,
                  children: [
                    // Indicador de status de conexão (Led pulsante)
                    Container(
                      width: 8,
                      height: 8,
                      decoration: BoxDecoration(
                        shape: BoxShape.circle,
                        color: isConnected ? AppTheme.neonGreen : AppTheme.lockCrimson,
                        boxShadow: [
                          BoxShadow(
                            color: (isConnected ? AppTheme.neonGreen : AppTheme.lockCrimson)
                                .withValues(alpha: 0.6),
                            blurRadius: 6,
                            spreadRadius: 2,
                          ),
                        ],
                      ),
                    ),
                    const SizedBox(width: 10),
                    Column(
                      crossAxisAlignment: CrossAxisAlignment.start,
                      mainAxisSize: MainAxisSize.min,
                      children: [
                        const Text(
                          'Gabriel Wentz',
                          style: TextStyle(
                            fontSize: 16,
                            fontWeight: FontWeight.w800,
                            letterSpacing: 0.5,
                          ),
                        ),
                        Text(
                          isConnected
                              ? '${bleService.connectedDevice?.name ?? "M0Robot"} Conectado'
                              : 'Patinete Desconectado',
                          style: TextStyle(
                            fontSize: 11,
                            color: isConnected ? AppTheme.neonGreen : AppTheme.textMuted,
                            fontWeight: FontWeight.w600,
                          ),
                        ),
                      ],
                    ),
                  ],
                ),
                actions: [
                  // Botão de Scanner Bluetooth
                  IconButton(
                    icon: const Icon(Icons.bluetooth_searching_rounded),
                    color: AppTheme.electricCyan,
                    tooltip: 'Buscar Dispositivos',
                    onPressed: () {
                      Navigator.push(
                        context,
                        MaterialPageRoute(
                          builder: (_) => ScanScreen(bleService: bleService),
                        ),
                      );
                    },
                  ),
                  // Botão de Interceptador & Diagnóstico
                  IconButton(
                    icon: const Icon(Icons.radar_rounded),
                    color: AppTheme.warningAmber,
                    tooltip: 'Interceptador & Mapeamento 1-a-1',
                    onPressed: () {
                      Navigator.push(
                        context,
                        MaterialPageRoute(
                          builder: (_) => InterceptorScreen(bleService: bleService),
                        ),
                      );
                    },
                  ),
                  // Botão de Configurações
                  IconButton(
                    icon: const Icon(Icons.tune_rounded),
                    color: AppTheme.textSecondary,
                    tooltip: 'Ajustes do Patinete',
                    onPressed: () {
                      Navigator.push(
                        context,
                        MaterialPageRoute(
                          builder: (_) => SettingsScreen(bleService: bleService),
                        ),
                      );
                    },
                  ),
                ],
              ),
              body: SafeArea(
                child: SingleChildScrollView(
                  physics: const BouncingScrollPhysics(),
                  padding: const EdgeInsets.symmetric(horizontal: 20, vertical: 12),
                  child: Column(
                    children: [
                      // Banner do Interceptador (quando conectado)
                      if (isConnected) ...[
                        InkWell(
                          onTap: () {
                            Navigator.push(
                              context,
                              MaterialPageRoute(
                                builder: (_) => InterceptorScreen(bleService: bleService),
                              ),
                            );
                          },
                          borderRadius: BorderRadius.circular(12),
                          child: Container(
                            margin: const EdgeInsets.only(bottom: 12),
                            padding: const EdgeInsets.symmetric(horizontal: 14, vertical: 9),
                            decoration: BoxDecoration(
                              color: AppTheme.warningAmber.withValues(alpha: 0.12),
                              borderRadius: BorderRadius.circular(12),
                              border: Border.all(
                                color: AppTheme.warningAmber.withValues(alpha: 0.5),
                              ),
                            ),
                            child: Row(
                              children: [
                                const Icon(Icons.radar_rounded, size: 18, color: AppTheme.warningAmber),
                                const SizedBox(width: 10),
                                const Expanded(
                                  child: Text(
                                    'Interceptador Ativo: Testar & Mapear Funções 1 a 1',
                                    style: TextStyle(
                                      fontSize: 12,
                                      fontWeight: FontWeight.bold,
                                      color: AppTheme.warningAmber,
                                    ),
                                  ),
                                ),
                                const Icon(Icons.chevron_right_rounded, size: 18, color: AppTheme.warningAmber),
                              ],
                            ),
                          ),
                        ),
                      ],
                      // Banner de Conexão Rápida (quando desconectado)
                      if (!isConnected) ...[
                        InkWell(
                          onTap: () {
                            Navigator.push(
                              context,
                              MaterialPageRoute(
                                builder: (_) => ScanScreen(bleService: bleService),
                              ),
                            );
                          },
                          borderRadius: BorderRadius.circular(16),
                          child: Container(
                            width: double.infinity,
                            padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 14),
                            decoration: BoxDecoration(
                              color: AppTheme.electricCyan.withValues(alpha: 0.12),
                              borderRadius: BorderRadius.circular(16),
                              border: Border.all(
                                color: AppTheme.electricCyan.withValues(alpha: 0.5),
                              ),
                            ),
                            child: Row(
                              children: [
                                Container(
                                  padding: const EdgeInsets.all(8),
                                  decoration: BoxDecoration(
                                    shape: BoxShape.circle,
                                    color: AppTheme.electricCyan.withValues(alpha: 0.2),
                                  ),
                                  child: const Icon(
                                    Icons.bluetooth_searching_rounded,
                                    color: AppTheme.electricCyan,
                                    size: 22,
                                  ),
                                ),
                                const SizedBox(width: 14),
                                Expanded(
                                  child: Column(
                                    crossAxisAlignment: CrossAxisAlignment.start,
                                    children: const [
                                      Text(
                                        'Nenhum patinete conectado',
                                        style: TextStyle(
                                          color: AppTheme.textPrimary,
                                          fontSize: 14,
                                          fontWeight: FontWeight.w800,
                                        ),
                                      ),
                                      SizedBox(height: 2),
                                      Text(
                                        'Toque aqui para localizar e parear seu veículo',
                                        style: TextStyle(
                                          color: AppTheme.electricCyan,
                                          fontSize: 12,
                                          fontWeight: FontWeight.w600,
                                        ),
                                      ),
                                    ],
                                  ),
                                ),
                                const Icon(
                                  Icons.chevron_right_rounded,
                                  color: AppTheme.electricCyan,
                                  size: 24,
                                ),
                              ],
                            ),
                          ),
                        ),
                        const SizedBox(height: 16),
                      ],

                      // 1. Mostrador do Velocímetro Central
                      SpeedometerGauge(
                        currentSpeed: isConnected ? telemetry.speedKmh : 0.0,
                        maxSpeed: 35.0,
                        mode: telemetry.speedMode,
                        currentPowerWatts: isConnected ? telemetry.powerWatts : 0.0,
                        isLocked: isConnected ? telemetry.isLocked : false,
                      ),

                      const SizedBox(height: 16),

                      // 2. Barra de Ações Rápidas (Farol, Modo, Cruise, Trava)
                      QuickControlBar(
                        isHeadlightOn: isConnected && telemetry.isHeadlightOn,
                        isLocked: isConnected && telemetry.isLocked,
                        speedMode: telemetry.speedMode,
                        isCruiseControlEnabled: isConnected && telemetry.isCruiseControlEnabled,
                        isSafeToLock: telemetry.isSafeToLock,
                        onHeadlightToggle: (val) => _executeWithSafety(
                          context,
                          isConnected,
                          () => bleService.setHeadlight(val),
                        ),
                        onLockToggle: (val) => _executeWithSafety(
                          context,
                          isConnected,
                          () => bleService.setMotorLock(val),
                        ),
                        onSpeedModeChanged: (mode) => _executeWithSafety(
                          context,
                          isConnected,
                          () => bleService.setSpeedMode(mode),
                        ),
                        onCruiseToggle: (val) => _executeWithSafety(
                          context,
                          isConnected,
                          () => bleService.setCruiseControl(val),
                        ),
                      ),

                      const SizedBox(height: 16),

                      // 3. Indicador de Bateria e Tensão
                      BatteryIndicator(
                        batteryPercent: isConnected ? telemetry.batteryPercent : 0,
                        voltage: isConnected ? telemetry.batteryVoltageVolts : 0.0,
                        currentAmperes: isConnected ? telemetry.currentAmperes : 0.0,
                      ),

                      const SizedBox(height: 16),

                      // 4. Métricas de Viagem e Temperatura
                      TelemetryCard(
                        tripDistanceKm: isConnected ? telemetry.tripDistanceKm : 0.0,
                        totalDistanceKm: isConnected ? telemetry.totalDistanceKm : 0.0,
                        temperatureCelsius: isConnected ? telemetry.temperatureCelsius : 0.0,
                        startMode: telemetry.startMode,
                      ),

                      const SizedBox(height: 20),
                    ],
                  ),
                ),
              ),
            );
          },
        );
      },
    );
  }
}
