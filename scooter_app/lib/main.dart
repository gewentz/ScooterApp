import 'package:flutter/material.dart';
import 'core/ble/scooter_ble_service.dart';
import 'presentation/screens/dashboard_screen.dart';
import 'presentation/theme/app_theme.dart';

import 'package:flutter_blue_plus/flutter_blue_plus.dart';

void main() async {
  WidgetsFlutterBinding.ensureInitialized();

  // Ativar logs detalhados do Bluetooth
  FlutterBluePlus.setLogLevel(LogLevel.verbose, color: false);

  final bleService = ScooterBleService();

  runApp(ScooterApp(bleService: bleService));
}

class ScooterApp extends StatefulWidget {
  final ScooterBleService bleService;

  const ScooterApp({super.key, required this.bleService});

  @override
  State<ScooterApp> createState() => _ScooterAppState();
}

class _ScooterAppState extends State<ScooterApp> {
  @override
  void initState() {
    super.initState();
    // Inicia desconectado no estado real, aguardando conexão física BLE
  }

  @override
  void dispose() {
    widget.bleService.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      title: 'M0 Scooter Cockpit',
      debugShowCheckedModeBanner: false,
      theme: AppTheme.darkTheme,
      home: DashboardScreen(bleService: widget.bleService),
    );
  }
}
