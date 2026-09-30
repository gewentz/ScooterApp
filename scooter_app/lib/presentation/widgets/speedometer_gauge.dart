import 'dart:math' as math;
import 'package:flutter/material.dart';
import '../../core/models/scooter_enums.dart';
import '../theme/app_theme.dart';

/// Widget de velocímetro em arco fluido renderizado via CustomPainter a 60/120fps
class SpeedometerGauge extends StatelessWidget {
  final double currentSpeed;
  final double maxSpeed;
  final SpeedMode mode;
  final double currentPowerWatts;
  final bool isLocked;

  const SpeedometerGauge({
    super.key,
    required this.currentSpeed,
    this.maxSpeed = 35.0,
    required this.mode,
    required this.currentPowerWatts,
    required this.isLocked,
  });

  Color _getModeColor() {
    if (isLocked) return AppTheme.lockCrimson;
    switch (mode) {
      case SpeedMode.eco:
        return AppTheme.neonGreen;
      case SpeedMode.drive:
        return AppTheme.electricCyan;
      case SpeedMode.sport:
        return AppTheme.sportOrange;
    }
  }

  @override
  Widget build(BuildContext context) {
    final themeColor = _getModeColor();

    return LayoutBuilder(
      builder: (context, constraints) {
        final size = math.min(constraints.maxWidth, 300.0);

        return Center(
          child: SizedBox(
            width: size,
            height: size,
            child: Stack(
              alignment: Alignment.center,
              children: [
                // 1. Arco e mostrador desenhados com CustomPaint
                TweenAnimationBuilder<double>(
                  tween: Tween<double>(begin: 0.0, end: currentSpeed),
                  duration: const Duration(milliseconds: 250),
                  curve: Curves.easeOutCubic,
                  builder: (context, animatedSpeed, child) {
                    return CustomPaint(
                      size: Size(size, size),
                      painter: _SpeedometerPainter(
                        speed: animatedSpeed,
                        maxSpeed: maxSpeed,
                        activeColor: themeColor,
                      ),
                    );
                  },
                ),

                // 2. Núcleo central com velocímetro digital
                Column(
                  mainAxisSize: MainAxisSize.min,
                  children: [
                    // Badge do Modo Ativo ou Trava
                    Container(
                      padding: const EdgeInsets.symmetric(horizontal: 14, vertical: 4),
                      decoration: BoxDecoration(
                        color: themeColor.withValues(alpha: 0.15),
                        borderRadius: BorderRadius.circular(20),
                        border: Border.all(
                          color: themeColor.withValues(alpha: 0.4),
                          width: 1.2,
                        ),
                      ),
                      child: Text(
                        isLocked ? 'MOTOR BLOQUEADO' : mode.label.toUpperCase(),
                        style: TextStyle(
                          color: themeColor,
                          fontSize: 11,
                          fontWeight: FontWeight.w800,
                          letterSpacing: 1.2,
                        ),
                      ),
                    ),
                    const SizedBox(height: 8),

                    // Valor da Velocidade Grande
                    Row(
                      mainAxisSize: MainAxisSize.min,
                      crossAxisAlignment: CrossAxisAlignment.baseline,
                      textBaseline: TextBaseline.alphabetic,
                      children: [
                        Text(
                          isLocked ? '--' : currentSpeed.toStringAsFixed(1),
                          style: TextStyle(
                            color: isLocked ? AppTheme.lockCrimson : AppTheme.textPrimary,
                            fontSize: size * 0.24,
                            fontWeight: FontWeight.w900,
                            letterSpacing: -1.0,
                            fontFeatures: const [FontFeature.tabularFigures()],
                          ),
                        ),
                        const SizedBox(width: 4),
                        Text(
                          'km/h',
                          style: TextStyle(
                            color: AppTheme.textSecondary,
                            fontSize: size * 0.065,
                            fontWeight: FontWeight.w600,
                          ),
                        ),
                      ],
                    ),

                    const SizedBox(height: 6),

                    // Potência instantânea em Watts
                    Row(
                      mainAxisSize: MainAxisSize.min,
                      children: [
                        Icon(
                          Icons.bolt_rounded,
                          size: 16,
                          color: themeColor,
                        ),
                        const SizedBox(width: 2),
                        Text(
                          '${currentPowerWatts.toStringAsFixed(0)} W',
                          style: TextStyle(
                            color: AppTheme.textSecondary,
                            fontSize: 13,
                            fontWeight: FontWeight.w600,
                            letterSpacing: 0.5,
                          ),
                        ),
                      ],
                    ),
                  ],
                ),
              ],
            ),
          ),
        );
      },
    );
  }
}

/// Painter responsável por desenhar a pista circular e o progresso iluminado
class _SpeedometerPainter extends CustomPainter {
  final double speed;
  final double maxSpeed;
  final Color activeColor;

  _SpeedometerPainter({
    required this.speed,
    required this.maxSpeed,
    required this.activeColor,
  });

  @override
  void paint(Canvas canvas, Size size) {
    final center = Offset(size.width / 2, size.height / 2);
    final radius = (size.width / 2) - 18.0;

    // Ângulo inicial: 135° (2.356 rad) | Sweep total: 270° (4.712 rad)
    const startAngle = 135.0 * (math.pi / 180.0);
    const totalSweep = 270.0 * (math.pi / 180.0);

    // 1. Trilho de fundo inativo
    final bgPaint = Paint()
      ..color = const Color(0xFF1B2335)
      ..style = PaintingStyle.stroke
      ..strokeWidth = 14.0
      ..strokeCap = StrokeCap.round;

    canvas.drawArc(
      Rect.fromCircle(center: center, radius: radius),
      startAngle,
      totalSweep,
      false,
      bgPaint,
    );

    // 2. Arco ativo com gradiente e efeito de brilho
    final clampedSpeed = speed.clamp(0.0, maxSpeed);
    final progress = clampedSpeed / maxSpeed;
    final currentSweep = totalSweep * progress;

    if (currentSweep > 0.01) {
      // Brilho difuso (Glow)
      final glowPaint = Paint()
        ..color = activeColor.withValues(alpha: 0.35)
        ..style = PaintingStyle.stroke
        ..strokeWidth = 20.0
        ..maskFilter = const MaskFilter.blur(BlurStyle.normal, 10.0)
        ..strokeCap = StrokeCap.round;

      canvas.drawArc(
        Rect.fromCircle(center: center, radius: radius),
        startAngle,
        currentSweep,
        false,
        glowPaint,
      );

      // Arco frontal nítido
      final activePaint = Paint()
        ..shader = SweepGradient(
          startAngle: startAngle,
          endAngle: startAngle + totalSweep,
          colors: [
            activeColor.withValues(alpha: 0.6),
            activeColor,
          ],
        ).createShader(Rect.fromCircle(center: center, radius: radius))
        ..style = PaintingStyle.stroke
        ..strokeWidth = 14.0
        ..strokeCap = StrokeCap.round;

      canvas.drawArc(
        Rect.fromCircle(center: center, radius: radius),
        startAngle,
        currentSweep,
        false,
        activePaint,
      );
    }

    // 3. Marcadores graduais (Ticks)
    final tickPaint = Paint()
      ..color = const Color(0xFF334155)
      ..strokeWidth = 1.5;

    for (int i = 0; i <= 10; i++) {
      final angle = startAngle + (totalSweep * (i / 10.0));
      final innerRadius = radius - 16.0;
      final outerRadius = radius - 10.0;

      final p1 = Offset(
        center.dx + innerRadius * math.cos(angle),
        center.dy + innerRadius * math.sin(angle),
      );
      final p2 = Offset(
        center.dx + outerRadius * math.cos(angle),
        center.dy + outerRadius * math.sin(angle),
      );

      canvas.drawLine(p1, p2, tickPaint);
    }
  }

  @override
  bool shouldRepaint(covariant _SpeedometerPainter oldDelegate) {
    return oldDelegate.speed != speed ||
        oldDelegate.activeColor != activeColor ||
        oldDelegate.maxSpeed != maxSpeed;
  }
}
