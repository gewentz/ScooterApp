import 'package:flutter/material.dart';
import '../../core/models/scooter_enums.dart';
import '../theme/app_theme.dart';

/// Grade de métricas complementares de telemetria
class TelemetryCard extends StatelessWidget {
  final double tripDistanceKm;
  final double totalDistanceKm;
  final double temperatureCelsius;
  final StartMode startMode;

  const TelemetryCard({
    super.key,
    required this.tripDistanceKm,
    required this.totalDistanceKm,
    required this.temperatureCelsius,
    required this.startMode,
  });

  @override
  Widget build(BuildContext context) {
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
          const Text(
            'TELEMETRIA DA VIAGEM',
            style: TextStyle(
              color: AppTheme.textSecondary,
              fontSize: 12,
              fontWeight: FontWeight.w700,
              letterSpacing: 1.0,
            ),
          ),
          const SizedBox(height: 14),
          Row(
            children: [
              Expanded(
                child: _TelemetryTile(
                  icon: Icons.map_outlined,
                  label: 'Distância Viagem',
                  value: '${tripDistanceKm.toStringAsFixed(2)} km',
                  accentColor: AppTheme.electricCyan,
                ),
              ),
              const SizedBox(width: 12),
              Expanded(
                child: _TelemetryTile(
                  icon: Icons.route_rounded,
                  label: 'Odômetro Total',
                  value: '${totalDistanceKm.toStringAsFixed(1)} km',
                  accentColor: AppTheme.neonGreen,
                ),
              ),
            ],
          ),
          const SizedBox(height: 12),
          Row(
            children: [
              Expanded(
                child: _TelemetryTile(
                  icon: Icons.thermostat_rounded,
                  label: 'Temp. Controladora',
                  value: '${temperatureCelsius.toStringAsFixed(1)} °C',
                  accentColor: temperatureCelsius > 55 ? AppTheme.lockCrimson : AppTheme.warningAmber,
                ),
              ),
              const SizedBox(width: 12),
              Expanded(
                child: _TelemetryTile(
                  icon: Icons.flash_on_rounded,
                  label: 'Modo de Partida',
                  value: startMode == StartMode.zeroStart ? 'Zero-Start' : 'Com Impulso',
                  accentColor: AppTheme.electricCyan,
                ),
              ),
            ],
          ),
        ],
      ),
    );
  }
}

class _TelemetryTile extends StatelessWidget {
  final IconData icon;
  final String label;
  final String value;
  final Color accentColor;

  const _TelemetryTile({
    required this.icon,
    required this.label,
    required this.value,
    required this.accentColor,
  });

  @override
  Widget build(BuildContext context) {
    return Container(
      padding: const EdgeInsets.all(12),
      decoration: BoxDecoration(
        color: AppTheme.surfaceLight,
        borderRadius: BorderRadius.circular(14),
      ),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Row(
            children: [
              Icon(icon, size: 16, color: accentColor),
              const SizedBox(width: 6),
              Flexible(
                child: Text(
                  label,
                  maxLines: 1,
                  overflow: TextOverflow.ellipsis,
                  style: const TextStyle(
                    color: AppTheme.textMuted,
                    fontSize: 11,
                    fontWeight: FontWeight.w600,
                  ),
                ),
              ),
            ],
          ),
          const SizedBox(height: 6),
          Text(
            value,
            style: const TextStyle(
              color: AppTheme.textPrimary,
              fontSize: 15,
              fontWeight: FontWeight.w800,
            ),
          ),
        ],
      ),
    );
  }
}
