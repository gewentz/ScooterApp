import 'package:flutter/material.dart';
import 'package:flutter_blue_plus/flutter_blue_plus.dart';
import '../../core/ble/ble_permission_helper.dart';
import '../../core/ble/scooter_ble_service.dart';
import '../../core/ble/scooter_device.dart';
import '../../core/models/scooter_enums.dart';
import '../theme/app_theme.dart';

/// Tela de busca e pareamento com o patinete via Bluetooth BLE real
class ScanScreen extends StatefulWidget {
  final ScooterBleService bleService;

  const ScanScreen({super.key, required this.bleService});

  @override
  State<ScanScreen> createState() => _ScanScreenState();
}

class _ScanScreenState extends State<ScanScreen> with SingleTickerProviderStateMixin {
  late AnimationController _pulseController;
  String? _connectingDeviceId;
  bool _filterOnlyCompatible = false;
  BluetoothAdapterState _adapterState = BluetoothAdapterState.unknown;
  BlePermissionState _permissionState = BlePermissionState.granted;

  @override
  void initState() {
    super.initState();
    _pulseController = AnimationController(
      vsync: this,
      duration: const Duration(seconds: 2),
    )..repeat();

    FlutterBluePlus.adapterState.listen((state) {
      if (mounted) {
        setState(() => _adapterState = state);
      }
    });

    _checkPermissionsAndScan();
  }

  Future<void> _checkPermissionsAndScan() async {
    final state = await BlePermissionHelper.checkAndRequestPermissions();
    if (mounted) {
      setState(() => _permissionState = state);
    }
    if (state == BlePermissionState.granted) {
      _startScan();
    }
  }

  void _startScan() {
    widget.bleService.startScan(filterOnlyCompatible: _filterOnlyCompatible);
  }

  @override
  void dispose() {
    _pulseController.dispose();
    widget.bleService.stopScan();
    super.dispose();
  }

  Future<void> _handleConnect(ScooterDevice device) async {
    setState(() => _connectingDeviceId = device.id);

    final success = await widget.bleService.connect(device);

    if (!mounted) return;

    if (success) {
      ScaffoldMessenger.of(context).showSnackBar(
        SnackBar(
          content: Text('Conectado a ${device.name} com sucesso!'),
          backgroundColor: AppTheme.neonGreen,
          duration: const Duration(seconds: 2),
        ),
      );
      Navigator.pop(context);
    } else {
      setState(() => _connectingDeviceId = null);
      ScaffoldMessenger.of(context).showSnackBar(
        SnackBar(
          content: Text('Falha ao conectar a ${device.name}. Certifique-se de que o patinete está ligado e próximo.'),
          backgroundColor: AppTheme.lockCrimson,
          duration: const Duration(seconds: 4),
        ),
      );
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text('BUSCAR PATINETE'),
        actions: [
          IconButton(
            icon: const Icon(Icons.help_outline_rounded, color: AppTheme.textSecondary),
            tooltip: 'Ajuda de Conexão',
            onPressed: _showHelpDialog,
          ),
          IconButton(
            icon: Icon(
              _filterOnlyCompatible ? Icons.filter_alt : Icons.filter_alt_outlined,
              color: _filterOnlyCompatible ? AppTheme.electricCyan : AppTheme.textSecondary,
            ),
            tooltip: _filterOnlyCompatible ? 'Mostrando apenas compatíveis' : 'Mostrando todos os BLE',
            onPressed: () {
              setState(() {
                _filterOnlyCompatible = !_filterOnlyCompatible;
              });
              _startScan();
            },
          ),
        ],
      ),
      body: StreamBuilder<BleConnectionStatus>(
        stream: widget.bleService.statusStream,
        initialData: widget.bleService.currentStatus,
        builder: (context, statusSnapshot) {
          final isScanning = statusSnapshot.data == BleConnectionStatus.scanning;

          return Column(
            children: [
              // Alerta de Permissão Negada
              if (_permissionState == BlePermissionState.denied)
                Container(
                  margin: const EdgeInsets.all(16),
                  padding: const EdgeInsets.all(12),
                  decoration: BoxDecoration(
                    color: AppTheme.warningAmber.withValues(alpha: 0.15),
                    borderRadius: BorderRadius.circular(12),
                    border: Border.all(color: AppTheme.warningAmber),
                  ),
                  child: Row(
                    children: [
                      const Icon(Icons.security_update_warning_rounded, color: AppTheme.warningAmber),
                      const SizedBox(width: 12),
                      const Expanded(
                        child: Text(
                          'Permissão de Dispositivos Próximos/Localização necessária para encontrar o patinete.',
                          style: TextStyle(color: AppTheme.textPrimary, fontSize: 13),
                        ),
                      ),
                      TextButton(
                        onPressed: _checkPermissionsAndScan,
                        child: const Text('PERMITIR', style: TextStyle(color: AppTheme.electricCyan, fontWeight: FontWeight.bold)),
                      ),
                    ],
                  ),
                ),

              // Alerta de Permissão Permanentemente Negada
              if (_permissionState == BlePermissionState.permanentlyDenied)
                Container(
                  margin: const EdgeInsets.all(16),
                  padding: const EdgeInsets.all(12),
                  decoration: BoxDecoration(
                    color: AppTheme.lockCrimson.withValues(alpha: 0.15),
                    borderRadius: BorderRadius.circular(12),
                    border: Border.all(color: AppTheme.lockCrimson),
                  ),
                  child: Row(
                    children: [
                      const Icon(Icons.block_rounded, color: AppTheme.lockCrimson),
                      const SizedBox(width: 12),
                      const Expanded(
                        child: Text(
                          'Permissão bloqueada no Android. Abra as configurações para permitir Dispositivos Próximos.',
                          style: TextStyle(color: AppTheme.textPrimary, fontSize: 13),
                        ),
                      ),
                      TextButton(
                        onPressed: () => BlePermissionHelper.openSettings(),
                        child: const Text('CONFIGURAR', style: TextStyle(color: AppTheme.electricCyan, fontWeight: FontWeight.bold)),
                      ),
                    ],
                  ),
                ),

              // Alerta se o Bluetooth estiver desligado no celular
              if (_adapterState == BluetoothAdapterState.off)
                Container(
                  margin: const EdgeInsets.symmetric(horizontal: 16, vertical: 8),
                  padding: const EdgeInsets.all(12),
                  decoration: BoxDecoration(
                    color: AppTheme.lockCrimson.withValues(alpha: 0.15),
                    borderRadius: BorderRadius.circular(12),
                    border: Border.all(color: AppTheme.lockCrimson),
                  ),
                  child: Row(
                    children: [
                      const Icon(Icons.bluetooth_disabled_rounded, color: AppTheme.lockCrimson),
                      const SizedBox(width: 12),
                      const Expanded(
                        child: Text(
                          'O Bluetooth do seu celular está desligado.',
                          style: TextStyle(color: AppTheme.textPrimary, fontSize: 13),
                        ),
                      ),
                      TextButton(
                        onPressed: () => FlutterBluePlus.turnOn(),
                        child: const Text('LIGAR', style: TextStyle(color: AppTheme.electricCyan, fontWeight: FontWeight.bold)),
                      ),
                    ],
                  ),
                ),

              // Cabeçalho de radar animado
              Container(
                padding: const EdgeInsets.symmetric(vertical: 20),
                child: Center(
                  child: Stack(
                    alignment: Alignment.center,
                    children: [
                      if (isScanning)
                        RotationTransition(
                          turns: _pulseController,
                          child: Container(
                            width: 90,
                            height: 90,
                            decoration: BoxDecoration(
                              shape: BoxShape.circle,
                              border: Border.all(
                                color: AppTheme.electricCyan.withValues(alpha: 0.3),
                                width: 2,
                              ),
                            ),
                          ),
                        ),
                      Container(
                        width: 64,
                        height: 64,
                        decoration: BoxDecoration(
                          shape: BoxShape.circle,
                          color: AppTheme.surface,
                          border: Border.all(color: AppTheme.cardBorder, width: 2),
                          boxShadow: [
                            BoxShadow(
                              color: AppTheme.electricCyan.withValues(alpha: 0.2),
                              blurRadius: 16,
                            ),
                          ],
                        ),
                        child: Icon(
                          Icons.bluetooth_audio_rounded,
                          color: isScanning ? AppTheme.electricCyan : AppTheme.textSecondary,
                          size: 30,
                        ),
                      ),
                    ],
                  ),
                ),
              ),

              Padding(
                padding: const EdgeInsets.symmetric(horizontal: 20),
                child: Row(
                  mainAxisAlignment: MainAxisAlignment.spaceBetween,
                  children: [
                    Text(
                      isScanning ? 'PROCURANDO PATINETES...' : 'DISPOSITIVOS DETECTADOS',
                      style: const TextStyle(
                        color: AppTheme.textSecondary,
                        fontSize: 12,
                        fontWeight: FontWeight.w700,
                        letterSpacing: 1.0,
                      ),
                    ),
                    if (isScanning)
                      const SizedBox(
                        width: 14,
                        height: 14,
                        child: CircularProgressIndicator(
                          strokeWidth: 2,
                          color: AppTheme.electricCyan,
                        ),
                      ),
                  ],
                ),
              ),

              const SizedBox(height: 10),

              // Lista de Dispositivos Detectados
              Expanded(
                child: StreamBuilder<List<ScooterDevice>>(
                  stream: widget.bleService.scanResultsStream,
                  initialData: widget.bleService.discoveredDevices,
                  builder: (context, snapshot) {
                    final devices = snapshot.data ?? [];

                    if (devices.isEmpty) {
                      return Center(
                        child: SingleChildScrollView(
                          padding: const EdgeInsets.symmetric(horizontal: 24),
                          child: Column(
                            mainAxisSize: MainAxisSize.min,
                            children: [
                              const Icon(Icons.radar_rounded, size: 52, color: AppTheme.textMuted),
                              const SizedBox(height: 14),
                              const Text(
                                'Ligue o patinete próximo ao celular',
                                style: TextStyle(color: AppTheme.textPrimary, fontSize: 15, fontWeight: FontWeight.w600),
                              ),
                              const SizedBox(height: 6),
                              const Text(
                                'Aguardando sinal Bluetooth BLE da controladora...',
                                textAlign: TextAlign.center,
                                style: TextStyle(color: AppTheme.textMuted, fontSize: 13),
                              ),
                              const SizedBox(height: 20),
                              OutlinedButton.icon(
                                onPressed: _showHelpDialog,
                                icon: const Icon(Icons.help_outline_rounded, size: 18, color: AppTheme.electricCyan),
                                label: const Text('Dicas para o patinete ser detectado', style: TextStyle(color: AppTheme.electricCyan, fontSize: 13)),
                                style: OutlinedButton.styleFrom(
                                  side: BorderSide(color: AppTheme.electricCyan.withValues(alpha: 0.4)),
                                  padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 10),
                                  shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(12)),
                                ),
                              ),
                              if (_filterOnlyCompatible) ...[
                                const SizedBox(height: 10),
                                TextButton(
                                  onPressed: () {
                                    setState(() => _filterOnlyCompatible = false);
                                    _startScan();
                                  },
                                  child: const Text('Desativar filtro e mostrar todos os dispositivos', style: TextStyle(color: AppTheme.textSecondary, fontSize: 12)),
                                ),
                              ],
                            ],
                          ),
                        ),
                      );
                    }

                    return ListView.separated(
                      padding: const EdgeInsets.symmetric(horizontal: 20, vertical: 8),
                      itemCount: devices.length,
                      separatorBuilder: (_, __) => const SizedBox(height: 10),
                      itemBuilder: (context, index) {
                        final device = devices[index];
                        final isOfficialTarget = device.isOfficialTarget;
                        final isConnectingThis = _connectingDeviceId == device.id;
                        final isConnectingAny = _connectingDeviceId != null;

                        return Container(
                          padding: const EdgeInsets.all(16),
                          decoration: BoxDecoration(
                            color: AppTheme.surface,
                            borderRadius: BorderRadius.circular(16),
                            border: Border.all(
                              color: isOfficialTarget
                                  ? AppTheme.electricCyan.withValues(alpha: 0.8)
                                  : AppTheme.cardBorder,
                              width: isOfficialTarget ? 1.5 : 1,
                            ),
                            boxShadow: isOfficialTarget
                                ? [
                                    BoxShadow(
                                      color: AppTheme.electricCyan.withValues(alpha: 0.15),
                                      blurRadius: 10,
                                    )
                                  ]
                                : [],
                          ),
                          child: Row(
                            children: [
                              Container(
                                padding: const EdgeInsets.all(10),
                                decoration: BoxDecoration(
                                  color: AppTheme.surfaceLight,
                                  borderRadius: BorderRadius.circular(12),
                                ),
                                child: Icon(
                                  device.isCompatible
                                      ? Icons.electric_scooter_rounded
                                      : Icons.devices_other_rounded,
                                  color: isOfficialTarget
                                      ? AppTheme.electricCyan
                                      : AppTheme.textSecondary,
                                  size: 24,
                                ),
                              ),
                              const SizedBox(width: 14),
                              Expanded(
                                child: Column(
                                  crossAxisAlignment: CrossAxisAlignment.start,
                                  children: [
                                    Row(
                                      children: [
                                        Flexible(
                                          child: Text(
                                            device.name,
                                            style: const TextStyle(
                                              color: AppTheme.textPrimary,
                                              fontSize: 15,
                                              fontWeight: FontWeight.w800,
                                            ),
                                            overflow: TextOverflow.ellipsis,
                                          ),
                                        ),
                                        if (isOfficialTarget) ...[
                                          const SizedBox(width: 6),
                                          Container(
                                            padding: const EdgeInsets.symmetric(
                                                horizontal: 6, vertical: 2),
                                            decoration: BoxDecoration(
                                              color: AppTheme.electricCyan.withValues(alpha: 0.2),
                                              borderRadius: BorderRadius.circular(6),
                                            ),
                                            child: const Text(
                                              'COMPATÍVEL',
                                              style: TextStyle(
                                                color: AppTheme.electricCyan,
                                                fontSize: 9,
                                                fontWeight: FontWeight.w800,
                                              ),
                                            ),
                                          ),
                                        ],
                                      ],
                                    ),
                                    const SizedBox(height: 4),
                                    Text(
                                      'ID: ${device.id} • Sinal: ${device.rssi} dBm',
                                      style: const TextStyle(
                                        color: AppTheme.textMuted,
                                        fontSize: 12,
                                      ),
                                    ),
                                  ],
                                ),
                              ),
                              const SizedBox(width: 10),
                              ElevatedButton(
                                onPressed: isConnectingAny ? null : () => _handleConnect(device),
                                style: ElevatedButton.styleFrom(
                                  backgroundColor: isOfficialTarget
                                      ? AppTheme.electricCyan
                                      : AppTheme.surfaceLight,
                                  foregroundColor: isOfficialTarget
                                      ? Colors.black
                                      : AppTheme.textPrimary,
                                  shape: RoundedRectangleBorder(
                                    borderRadius: BorderRadius.circular(12),
                                  ),
                                  padding: const EdgeInsets.symmetric(
                                      horizontal: 16, vertical: 10),
                                ),
                                child: isConnectingThis
                                    ? const SizedBox(
                                        width: 18,
                                        height: 18,
                                        child: CircularProgressIndicator(
                                          strokeWidth: 2,
                                          color: Colors.black,
                                        ),
                                      )
                                    : const Text(
                                        'Conectar',
                                        style: TextStyle(fontWeight: FontWeight.w800),
                                      ),
                              ),
                            ],
                          ),
                        );
                      },
                    );
                  },
                ),
              ),

              // Botão de Nova Busca
              Padding(
                padding: const EdgeInsets.all(20),
                child: SizedBox(
                  width: double.infinity,
                  child: OutlinedButton.icon(
                    onPressed: () {
                      if (isScanning) {
                        widget.bleService.stopScan();
                      } else {
                        _startScan();
                      }
                    },
                    icon: Icon(isScanning ? Icons.stop_rounded : Icons.refresh_rounded),
                    label: Text(isScanning ? 'Parar Busca' : 'Buscar Novamente'),
                    style: OutlinedButton.styleFrom(
                      foregroundColor: AppTheme.textPrimary,
                      side: const BorderSide(color: AppTheme.cardBorder, width: 1.2),
                      padding: const EdgeInsets.symmetric(vertical: 14),
                      shape: RoundedRectangleBorder(
                        borderRadius: BorderRadius.circular(16),
                      ),
                    ),
                  ),
                ),
              ),
            ],
          );
        },
      ),
    );
  }

  void _showHelpDialog() {
    showDialog(
      context: context,
      builder: (ctx) => AlertDialog(
        backgroundColor: AppTheme.surface,
        shape: RoundedRectangleBorder(
          borderRadius: BorderRadius.circular(20),
          side: const BorderSide(color: AppTheme.cardBorder),
        ),
        title: const Row(
          children: [
            Icon(Icons.info_outline_rounded, color: AppTheme.electricCyan),
            SizedBox(width: 10),
            Text(
              'Ajuda de Conexão',
              style: TextStyle(color: AppTheme.textPrimary, fontSize: 18, fontWeight: FontWeight.bold),
            ),
          ],
        ),
        content: const SingleChildScrollView(
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            mainAxisSize: MainAxisSize.min,
            children: [
              _HelpItem(
                number: '1',
                title: 'Ligue o Patinete',
                description: 'Pressione o botão físico no guidão e veja se o display/farol acendeu.',
              ),
              SizedBox(height: 12),
              _HelpItem(
                number: '2',
                title: 'Feche outros aplicativos',
                description: 'Se o app oficial (MiniRobot, LebiTEC, YouFS) estiver aberto ou em segundo plano, ENCERRE-O. O Bluetooth do patinete só aceita 1 conexão por vez.',
              ),
              SizedBox(height: 12),
              _HelpItem(
                number: '3',
                title: 'Localização (GPS) e Bluetooth',
                description: 'No Android, certifique-se de que o Bluetooth e o GPS/Localização estejam ativados no atalho do celular.',
              ),
              SizedBox(height: 12),
              _HelpItem(
                number: '4',
                title: 'Desparear do celular',
                description: 'Se você já pareou o patinete em Configurações > Bluetooth do Android, clique em "Esquecer/Desconectar" para liberar o anúncio BLE.',
              ),
              SizedBox(height: 12),
              _HelpItem(
                number: '5',
                title: 'Proximidade',
                description: 'Mantenha o celular a menos de 2 metros do guidão.',
              ),
            ],
          ),
        ),
        actions: [
          TextButton(
            onPressed: () => Navigator.pop(ctx),
            child: const Text('ENTENDI', style: TextStyle(color: AppTheme.electricCyan, fontWeight: FontWeight.bold)),
          ),
        ],
      ),
    );
  }
}

class _HelpItem extends StatelessWidget {
  final String number;
  final String title;
  final String description;

  const _HelpItem({
    required this.number,
    required this.title,
    required this.description,
  });

  @override
  Widget build(BuildContext context) {
    return Row(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        Container(
          width: 22,
          height: 22,
          alignment: Alignment.center,
          decoration: BoxDecoration(
            color: AppTheme.electricCyan.withValues(alpha: 0.15),
            shape: BoxShape.circle,
            border: Border.all(color: AppTheme.electricCyan, width: 1.2),
          ),
          child: Text(
            number,
            style: const TextStyle(
              color: AppTheme.electricCyan,
              fontSize: 11,
              fontWeight: FontWeight.bold,
            ),
          ),
        ),
        const SizedBox(width: 10),
        Expanded(
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              Text(
                title,
                style: const TextStyle(
                  color: AppTheme.textPrimary,
                  fontSize: 13,
                  fontWeight: FontWeight.w600,
                ),
              ),
              const SizedBox(height: 2),
              Text(
                description,
                style: const TextStyle(
                  color: AppTheme.textMuted,
                  fontSize: 12,
                  height: 1.3,
                ),
              ),
            ],
          ),
        ),
      ],
    );
  }
}
