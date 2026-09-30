import 'package:flutter/material.dart';
import '../theme/app_theme.dart';

/// Card de monitoramento de bateria, tensão e autonomia estimada
class BatteryIndicator extends StatelessWidget {
  final int batteryPercent;
  final double voltage;
  final double currentAmperes;

  const BatteryIndicator({
    super.key,
    required this.batteryPercent,
    required this.voltage,
    required this.currentAmperes,
  });

  Color _getBatteryColor() {
    if (batteryPercent > 50) return AppTheme.neonGreen;
    if (batteryPercent > 20) return AppTheme.warningAmber;
    return AppTheme.lockCrimson;
  }

  /// Estimativa aproximada de autonomia restante (baseada em ~25km para 100% de bateria)
  double get _estimatedRangeKm => (batteryPercent / 100.0) * 25.0;

  @override
  Widget build(BuildContext context) {
    final color = _getBatteryColor();
    final isRegenerating = currentAmperes < -0.2; // Frenagem regenerativa recarregando

    return Container(
      padding: const EdgeInsets.all(16),
      decoration: BoxDecoration(
        color: AppTheme.surface,
        borderRadius: BorderRadius.circular(20),
        border: Border.all(color: AppTheme.cardBorder, width: 1),
      ),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Row(
            mainAxisAlignment: MainAxisAlignment.spaceBetween,
            children: [
              Row(
                children: [
                  Icon(
                    isRegenerating ? Icons.battery_charging_full_rounded : Icons.battery_std_rounded,
                    color: color,
                    size: 20,
                  ),
                  const SizedBox(width: 8),
                  const Text(
                    'BATERIA',
                    style: TextStyle(
                      color: AppTheme.textSecondary,
                      fontSize: 12,
                      fontWeight: FontWeight.w700,
                      letterSpacing: 1.0,
                    ),
                  ),
                ],
              ),
              // Tensão em Volts
              Container(
                padding: const EdgeInsets.symmetric(horizontal: 8, vertical: 3),
                decoration: BoxDecoration(
                  color: AppTheme.surfaceLight,
                  borderRadius: BorderRadius.circular(8),
                ),
                child: Text(
                  '${voltage.toStringAsFixed(1)} V',
                  style: const TextStyle(
                    color: AppTheme.textPrimary,
                    fontSize: 12,
                    fontWeight: FontWeight.w700,
                  ),
                ),
              ),
            ],
          ),
          const SizedBox(height: 12),

          // Barra de Progresso Segmentada
          Stack(
            children: [
              // Fundo
              Container(
                height: 10,
                decoration: BoxDecoration(
                  color: const Color(0xFF1E283D),
                  borderRadius: BorderRadius.circular(5),
                ),
              ),
              // Barra Preenchida
              FractionallySizedBox(
                widthFactor: (batteryPercent / 100.0).clamp(0.0, 1.0),
                child: Container(
                  height: 10,
                  decoration: BoxDecoration(
                    color: color,
                    borderRadius: BorderRadius.circular(5),
                    boxShadow: [
                      BoxShadow(
                        color: color.withValues(alpha: 0.4),
                        blurRadius: 8,
                        spreadRadius: 1,
                      ),
                    ],
                  ),
                ),
              ),
            ],
          ),
          const SizedBox(height: 12),

          // Informações de Porcentagem e Autonomia
          Row(
            mainAxisAlignment: MainAxisAlignment.spaceBetween,
            children: [
              RichText(
                text: TextSpan(
                  children: [
                    TextSpan(
                      text: '$batteryPercent',
                      style: TextStyle(
                        color: color,
                        fontSize: 22,
                        fontWeight: FontWeight.w900,
                      ),
                    ),
                    const TextSpan(
                      text: '%',
                      style: TextStyle(
                        color: AppTheme.textSecondary,
                        fontSize: 14,
                        fontWeight: FontWeight.w700,
                      ),
                    ),
                  ],
                ),
              ),
              Text(
                '~${_estimatedRangeKm.toStringAsFixed(1)} km restantes',
                style: const TextStyle(
                  color: AppTheme.textSecondary,
                  fontSize: 12,
                  fontWeight: FontWeight.w500,
                ),
              ),
            ],
          ),
        ],
      ),
    );
  }
}
