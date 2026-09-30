import 'dart:io';
import 'package:flutter/foundation.dart';
import 'package:flutter_blue_plus/flutter_blue_plus.dart';
import 'package:permission_handler/permission_handler.dart';

/// Status consolidado de permissões para Bluetooth Low Energy
enum BlePermissionState {
  granted,
  denied,
  permanentlyDenied,
  bluetoothOff,
  locationOff,
  notSupported,
}

/// Utilitário responsável pela validação e requisição das permissões
/// necessárias para escanear e conectar ao patinete via BLE.
class BlePermissionHelper {
  BlePermissionHelper._();

  /// Solicita todas as permissões necessárias para o BLE operar de acordo com a versão do SO
  static Future<BlePermissionState> checkAndRequestPermissions() async {
    // BLE não é suportado no Windows nativo ou em aparelhos sem rádio BLE
    if (kIsWeb) {
      return BlePermissionState.granted;
    }

    if (!Platform.isAndroid && !Platform.isIOS) {
      return BlePermissionState.granted;
    }

    try {
      if (await FlutterBluePlus.isSupported == false) {
        return BlePermissionState.notSupported;
      }

      // No Android, precisamos lidar com Android 12+ (Scan/Connect) e Android <= 11 (Location)
      if (Platform.isAndroid) {
        final Map<Permission, PermissionStatus> statuses = await [
          Permission.bluetoothScan,
          Permission.bluetoothConnect,
          Permission.location,
        ].request();

        final scanStatus = statuses[Permission.bluetoothScan];
        final connectStatus = statuses[Permission.bluetoothConnect];
        final locationStatus = statuses[Permission.location];

        // Se o usuário marcou "Não perguntar novamente"
        if (scanStatus == PermissionStatus.permanentlyDenied ||
            connectStatus == PermissionStatus.permanentlyDenied ||
            locationStatus == PermissionStatus.permanentlyDenied) {
          return BlePermissionState.permanentlyDenied;
        }

        // Se alguma das permissões foi negada
        final isScanGranted = scanStatus?.isGranted == true || scanStatus?.isLimited == true;
        final isConnectGranted = connectStatus?.isGranted == true || connectStatus?.isLimited == true;
        final isLocationGranted = locationStatus?.isGranted == true;

        // Se nem bluetooth nem location foram concedidos
        if ((!isScanGranted || !isConnectGranted) && !isLocationGranted) {
          return BlePermissionState.denied;
        }
      } else if (Platform.isIOS) {
        final status = await Permission.bluetooth.request();
        if (status.isPermanentlyDenied) {
          return BlePermissionState.permanentlyDenied;
        }
        if (!status.isGranted) {
          return BlePermissionState.denied;
        }
      }

      // Verificar se o rádio Bluetooth está ligado
      final adapterState = await FlutterBluePlus.adapterState.first;
      if (adapterState != BluetoothAdapterState.on) {
        return BlePermissionState.bluetoothOff;
      }

      return BlePermissionState.granted;
    } catch (e) {
      debugPrint('Erro ao verificar permissões BLE: $e');
      return BlePermissionState.denied;
    }
  }

  /// Abre a tela de configurações do aplicativo no sistema operacional
  static Future<bool> openSettings() async {
    return await openAppSettings();
  }

  /// Liga o adaptador Bluetooth caso esteja desligado (Android)
  static Future<void> turnOnBluetooth() async {
    try {
      await FlutterBluePlus.turnOn();
    } catch (e) {
      debugPrint('Erro ao tentar ligar Bluetooth: $e');
    }
  }
}
