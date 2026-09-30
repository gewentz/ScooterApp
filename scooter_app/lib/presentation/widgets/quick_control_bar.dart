import 'package:flutter/material.dart';
import '../../core/models/scooter_enums.dart';
import '../theme/app_theme.dart';

/// Barra de ações e controles rápidos do patinete
class QuickControlBar extends StatelessWidget {
  final bool isHeadlightOn;
  final bool isLocked;
  final SpeedMode speedMode;
  final bool isCruiseControlEnabled;
  final bool isSafeToLock;

  final ValueChanged<bool> onHeadlightToggle;
  final ValueChanged<bool> onLockToggle;
  final ValueChanged<SpeedMode> onSpeedModeChanged;
  final ValueChanged<bool> onCruiseToggle;

  const QuickControlBar({
    super.key,
    required this.isHeadlightOn,
    required this.isLocked,
    required this.speedMode,
    required this.isCruiseControlEnabled,
    required this.isSafeToLock,
    required this.onHeadlightToggle,
    required this.onLockToggle,
    required this.onSpeedModeChanged,
    required this.onCruiseToggle,
  });

  void _cycleSpeedMode() {
    switch (speedMode) {
      case SpeedMode.eco:
        onSpeedModeChanged(SpeedMode.drive);
        break;
      case SpeedMode.drive:
        onSpeedModeChanged(SpeedMode.sport);
        break;
      case SpeedMode.sport:
        onSpeedModeChanged(SpeedMode.eco);
        break;
    }
  }

  @override
  Widget build(BuildContext context) {
    return Row(
      children: [
        // 1. Botão Farol
        Expanded(
          child: _ControlButton(
            label: 'Farol',
            isActive: isHeadlightOn,
            activeColor: AppTheme.electricCyan,
            icon: isHeadlightOn ? Icons.light_mode_rounded : Icons.light_mode_outlined,
            onTap: () => onHeadlightToggle(!isHeadlightOn),
          ),
        ),
        const SizedBox(width: 10),

        // 2. Botão Modo de Velocidade
        Expanded(
          child: _ControlButton(
            label: speedMode.name.toUpperCase(),
            isActive: true,
            activeColor: speedMode == SpeedMode.sport
                ? AppTheme.sportOrange
                : speedMode == SpeedMode.eco
                    ? AppTheme.neonGreen
                    : AppTheme.electricCyan,
            icon: Icons.speed_rounded,
            onTap: _cycleSpeedMode,
          ),
        ),
        const SizedBox(width: 10),

        // 3. Botão Piloto Automático (Cruise)
        Expanded(
          child: _ControlButton(
            label: 'Piloto',
            isActive: isCruiseControlEnabled,
            activeColor: AppTheme.neonGreen,
            icon: Icons.navigation_rounded,
            onTap: () => onCruiseToggle(!isCruiseControlEnabled),
          ),
        ),
        const SizedBox(width: 10),

        // 4. Botão Trava do Motor
        Expanded(
          child: _ControlButton(
            label: isLocked ? 'Travado' : 'Trava',
            isActive: isLocked,
            activeColor: AppTheme.lockCrimson,
            icon: isLocked ? Icons.lock_rounded : Icons.lock_open_rounded,
            onTap: () {
              if (!isLocked && !isSafeToLock) {
                ScaffoldMessenger.of(context).showSnackBar(
                  const SnackBar(
                    content: Text('Por segurança, pare o patinete antes de travar o motor!'),
                    backgroundColor: AppTheme.lockCrimson,
                    duration: Duration(seconds: 2),
                  ),
                );
                return;
              }
              onLockToggle(!isLocked);
            },
          ),
        ),
      ],
    );
  }
}

class _ControlButton extends StatelessWidget {
  final String label;
  final bool isActive;
  final Color activeColor;
  final IconData icon;
  final VoidCallback onTap;

  const _ControlButton({
    required this.label,
    required this.isActive,
    required this.activeColor,
    required this.icon,
    required this.onTap,
  });

  @override
  Widget build(BuildContext context) {
    return Material(
      color: Colors.transparent,
      child: InkWell(
        onTap: onTap,
        borderRadius: BorderRadius.circular(16),
        child: AnimatedContainer(
          duration: const Duration(milliseconds: 200),
          padding: const EdgeInsets.symmetric(vertical: 14),
          decoration: BoxDecoration(
            color: isActive ? activeColor.withValues(alpha: 0.15) : AppTheme.surface,
            borderRadius: BorderRadius.circular(16),
            border: Border.all(
              color: isActive ? activeColor.withValues(alpha: 0.6) : AppTheme.cardBorder,
              width: 1.2,
            ),
            boxShadow: isActive
                ? [
                    BoxShadow(
                      color: activeColor.withValues(alpha: 0.2),
                      blurRadius: 10,
                      spreadRadius: 1,
                    )
                  ]
                : [],
          ),
          child: Column(
            mainAxisSize: MainAxisSize.min,
            children: [
              Icon(
                icon,
                color: isActive ? activeColor : AppTheme.textSecondary,
                size: 24,
              ),
              const SizedBox(height: 6),
              Text(
                label,
                style: TextStyle(
                  color: isActive ? activeColor : AppTheme.textSecondary,
                  fontSize: 11,
                  fontWeight: FontWeight.w700,
                  letterSpacing: 0.5,
                ),
              ),
            ],
          ),
        ),
      ),
    );
  }
}
