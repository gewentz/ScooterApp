import 'package:flutter/material.dart';
import '../../core/ble/mock_scooter_transport.dart';
import '../../core/ble/scooter_ble_service.dart';
import '../../core/ble/scooter_device.dart';
import '../../core/models/scooter_enums.dart';
import '../theme/app_theme.dart';

/// Tela de configurações e ajustes avançados do patinete elétrico
class SettingsScreen extends StatefulWidget {
  final ScooterBleService bleService;

  const SettingsScreen({super.key, required this.bleService});

  @override
  State<SettingsScreen> createState() => _SettingsScreenState();
}

class _SettingsScreenState extends State<SettingsScreen> {
  double _speedLimit = 25.0;

  @override
  Widget build(BuildContext context) {
    return StreamBuilder<BleConnectionStatus>(
      stream: widget.bleService.statusStream,
      initialData: widget.bleService.currentStatus,
      builder: (context, _) {
        final telemetry = widget.bleService.currentTelemetry;
        final connectedDevice = widget.bleService.connectedDevice;
        final isConnected = widget.bleService.isConnected;
        final isMockActive = connectedDevice?.name.contains('Simulador') == true;

        return Scaffold(
          appBar: AppBar(
            title: const Text('AJUSTES DO VEÍCULO'),
          ),
          body: ListView(
            padding: const EdgeInsets.symmetric(horizontal: 20, vertical: 16),
            children: [
              // 1. Limite de Velocidade Máxima
              _buildSectionHeader('DESEMPENHO'),
              Container(
                padding: const EdgeInsets.all(16),
                decoration: BoxDecoration(
                  color: AppTheme.surface,
                  borderRadius: BorderRadius.circular(16),
                  border: Border.all(color: AppTheme.cardBorder),
                ),
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Row(
                      mainAxisAlignment: MainAxisAlignment.spaceBetween,
                      children: [
                        const Text(
                          'Velocidade Máxima Limite',
                          style: TextStyle(
                            color: AppTheme.textPrimary,
                            fontSize: 14,
                            fontWeight: FontWeight.w700,
                          ),
                        ),
                        Text(
                          '${_speedLimit.toInt()} km/h',
                          style: const TextStyle(
                            color: AppTheme.electricCyan,
                            fontSize: 16,
                            fontWeight: FontWeight.w900,
                          ),
                        ),
                      ],
                    ),
                    Slider(
                      value: _speedLimit,
                      min: 15.0,
                      max: 35.0,
                      divisions: 20,
                      activeColor: AppTheme.electricCyan,
                      inactiveColor: AppTheme.surfaceLight,
                      onChanged: (val) {
                        setState(() => _speedLimit = val);
                      },
                      onChangeEnd: (val) {
                        if (isConnected) {
                          widget.bleService.setSpeedLimit(val.toInt());
                        }
                      },
                    ),
                    const Text(
                      'Limita a aceleração do motor no modo Sport.',
                      style: TextStyle(color: AppTheme.textMuted, fontSize: 11),
                    ),
                  ],
                ),
              ),

              const SizedBox(height: 24),

              // 2. Modos de Partida e Segurança
              _buildSectionHeader('SEGURANÇA E PILOTAGEM'),
              Container(
                decoration: BoxDecoration(
                  color: AppTheme.surface,
                  borderRadius: BorderRadius.circular(16),
                  border: Border.all(color: AppTheme.cardBorder),
                ),
                child: Column(
                  children: [
                    SwitchListTile(
                      title: const Text(
                        'Partida Direta (Zero-Start)',
                        style: TextStyle(color: AppTheme.textPrimary, fontWeight: FontWeight.w700, fontSize: 14),
                      ),
                      subtitle: const Text(
                        'Acelera mesmo parado sem precisar de impulso físico com o pé.',
                        style: TextStyle(color: AppTheme.textMuted, fontSize: 12),
                      ),
                      value: telemetry.startMode == StartMode.zeroStart,
                      activeTrackColor: AppTheme.warningAmber,
                      onChanged: isConnected
                          ? (val) {
                              widget.bleService.setStartMode(
                                val ? StartMode.zeroStart : StartMode.kickToStart,
                              );
                            }
                          : null,
                    ),
                    const Divider(color: AppTheme.cardBorder, height: 1),
                    SwitchListTile(
                      title: const Text(
                        'Piloto Automático (Cruise Control)',
                        style: TextStyle(color: AppTheme.textPrimary, fontWeight: FontWeight.w700, fontSize: 14),
                      ),
                      subtitle: const Text(
                        'Mantém a velocidade travada após 5s acelerando.',
                        style: TextStyle(color: AppTheme.textMuted, fontSize: 12),
                      ),
                      value: telemetry.isCruiseControlEnabled,
                      activeTrackColor: AppTheme.neonGreen,
                      onChanged: isConnected
                          ? (val) {
                              widget.bleService.setCruiseControl(val);
                            }
                          : null,
                    ),
                  ],
                ),
              ),

              const SizedBox(height: 24),

              // 3. Informações do Dispositivo
              _buildSectionHeader('DISPOSITIVO CONECTADO'),
              Container(
                padding: const EdgeInsets.all(16),
                decoration: BoxDecoration(
                  color: AppTheme.surface,
                  borderRadius: BorderRadius.circular(16),
                  border: Border.all(color: AppTheme.cardBorder),
                ),
                child: Column(
                  children: [
                    _buildInfoRow('Nome do Patinete', isConnected ? connectedDevice!.name : 'Nenhum patinete conectado'),
                    const SizedBox(height: 8),
                    _buildInfoRow('Endereço BLE', isConnected ? connectedDevice!.id : '--:--:--:--:--:--'),
                    const SizedBox(height: 8),
                    _buildInfoRow('Status do Link', isConnected ? 'Conectado e Operacional' : 'Desconectado'),
                    const SizedBox(height: 8),
                    _buildInfoRow('Controladora', 'MiniRobot / LebiTEC X-Bot (0x20)'),
                    const SizedBox(height: 8),
                    _buildInfoRow('Protocolo', 'UART Serial 0x55AA (16-bit Checksum)'),
                    if (isConnected && !isMockActive) ...[
                      const SizedBox(height: 16),
                      SizedBox(
                        width: double.infinity,
                        child: OutlinedButton.icon(
                          onPressed: () async {
                            await widget.bleService.disconnect();
                          },
                          icon: const Icon(Icons.link_off_rounded, color: AppTheme.lockCrimson),
                          label: const Text('Desconectar Patinete', style: TextStyle(color: AppTheme.lockCrimson)),
                          style: OutlinedButton.styleFrom(
                            side: const BorderSide(color: AppTheme.lockCrimson),
                            padding: const EdgeInsets.symmetric(vertical: 12),
                            shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(12)),
                          ),
                        ),
                      ),
                    ],
                  ],
                ),
              ),

              const SizedBox(height: 24),

              // 4. Modo de Demonstração / Simulador
              _buildSectionHeader('MODO DE TESTE (OFFLINE)'),
              Container(
                padding: const EdgeInsets.all(16),
                decoration: BoxDecoration(
                  color: AppTheme.surface,
                  borderRadius: BorderRadius.circular(16),
                  border: Border.all(color: AppTheme.cardBorder),
                ),
                child: Row(
                  children: [
                    Expanded(
                      child: Column(
                        crossAxisAlignment: CrossAxisAlignment.start,
                        children: const [
                          Text(
                            'Simulador de Patinete',
                            style: TextStyle(color: AppTheme.textPrimary, fontWeight: FontWeight.w700, fontSize: 14),
                          ),
                          SizedBox(height: 4),
                          Text(
                            'Gera telemetria virtual para testar a interface sem um patinete físico.',
                            style: TextStyle(color: AppTheme.textMuted, fontSize: 11),
                          ),
                        ],
                      ),
                    ),
                    const SizedBox(width: 10),
                    ElevatedButton(
                      onPressed: () async {
                        if (!isMockActive) {
                          await widget.bleService.connect(
                            const ScooterDevice(
                              name: 'M0RobotDFE54C32BA (Simulador)',
                              id: 'DF:E5:4C:32:BA:01',
                              rssi: -52,
                            ),
                            transport: MockScooterBleTransport(),
                          );
                        } else {
                          await widget.bleService.disconnect();
                        }
                      },
                      style: ElevatedButton.styleFrom(
                        backgroundColor: isMockActive ? AppTheme.lockCrimson : AppTheme.surfaceLight,
                        foregroundColor: isMockActive ? Colors.white : AppTheme.textPrimary,
                      ),
                      child: Text(isMockActive ? 'Desativar' : 'Ativar'),
                    ),
                  ],
                ),
              ),

              const SizedBox(height: 30),
            ],
          ),
        );
      },
    );
  }

  Widget _buildSectionHeader(String title) {
    return Padding(
      padding: const EdgeInsets.only(left: 4, bottom: 8),
      child: Text(
        title,
        style: const TextStyle(
          color: AppTheme.textSecondary,
          fontSize: 11,
          fontWeight: FontWeight.w800,
          letterSpacing: 1.2,
        ),
      ),
    );
  }

  Widget _buildInfoRow(String label, String value) {
    return Row(
      mainAxisAlignment: MainAxisAlignment.spaceBetween,
      children: [
        Text(label, style: const TextStyle(color: AppTheme.textMuted, fontSize: 13)),
        const SizedBox(width: 8),
        Flexible(
          child: Text(
            value,
            style: const TextStyle(color: AppTheme.textPrimary, fontSize: 13, fontWeight: FontWeight.w600),
            overflow: TextOverflow.ellipsis,
            textAlign: TextAlign.end,
          ),
        ),
      ],
    );
  }
}
