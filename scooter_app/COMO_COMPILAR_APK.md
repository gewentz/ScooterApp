# Guia de Compilação e Envio do APK para o Smartphone

Este guia explica como testar o aplicativo no seu celular de duas formas:
1. **Teste Imediato via Rede Local** (sem precisar compilar nada, para testar a interface tátil agora);
2. **Compilação do APK Nativo Android** (para conexão real com o Bluetooth do patinete).

---

## Opção 1: Teste Imediato no Celular (Sem Compilar)

O servidor de prévia interativa já está rodando no seu computador. Se o seu smartphone estiver conectado na **mesma rede Wi-Fi**:

1. Pegue o seu celular;
2. Abra o navegador (Chrome / Safari);
3. Digite o endereço do seu computador na rede local:
   ```text
   http://192.168.100.22:3000
   ```
4. A tela do cockpit abrirá ajustada perfeitamente para o formato do smartphone, permitindo testar o toque dos botões, o acelerador tátil e a fluidez do velocímetro.

---

## Opção 2: Compilação do APK Nativo (.apk) com Flutter

Para que o aplicativo converse via Bluetooth real com o seu patinete `M0RobotDFE54C32BA`, ele precisa ser empacotado como um aplicativo nativo Android (`.apk`).

### 1. Pré-requisitos
* Seu computador já possui o **Android SDK** instalado em:
  `C:\Users\gabri\AppData\Local\Android\Sdk`
* É necessário ter o **Flutter SDK** instalado no seu sistema. Se ainda não tiver o comando `flutter` configurado:
  * Baixe o Flutter SDK em: [flutter.dev](https://docs.flutter.dev/get-started/install/windows/mobile)
  * Extraia em `C:\src\flutter` (ou pasta de sua preferência) e adicione `C:\src\flutter\bin` à variável de ambiente `PATH`.

---

### 2. Gerar a Pasta Nativa Android
Abra o terminal PowerShell na pasta `scooter_app`:
```powershell
cd c:\Users\gabri\Documentos\Reverse\scooter_app
flutter create . --platforms=android
```

---

### 3. Configurar Permissões de Bluetooth no Android
Abra o arquivo gerado:
`scooter_app/android/app/src/main/AndroidManifest.xml`

Adicione as permissões de Bluetooth e Localização logo acima da tag `<application>`:
```xml
    <!-- Permissões Bluetooth clássicas (Android <= 11) -->
    <uses-permission android:name="android.permission.BLUETOOTH" android:maxSdkVersion="30" />
    <uses-permission android:name="android.permission.BLUETOOTH_ADMIN" android:maxSdkVersion="30" />
    <uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
    <uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION" />

    <!-- Permissões Bluetooth modernas (Android 12+) -->
    <uses-permission android:name="android.permission.BLUETOOTH_SCAN" android:usesPermissionFlags="neverForLocation" />
    <uses-permission android:name="android.permission.BLUETOOTH_CONNECT" />

    <uses-feature android:name="android.hardware.bluetooth_le" android:required="true" />
```

E no arquivo `scooter_app/android/app/build.gradle`, certifique-se de que o `minSdkVersion` seja no mínimo **21** (ou 23):
```groovy
defaultConfig {
    minSdkVersion 21
    targetSdkVersion 34
    ...
}
```

---

### 4. Conectar o Celular via USB e Habilitar Depuração
1. No seu smartphone Android, vá em **Configurações > Sobre o Telefone**.
2. Toque **7 vezes** seguidas em **"Número da Versão"** até ativar o "Modo de Desenvolvedor".
3. Volte em **Opções do Desenvolvedor** e ative:
   * **Depuração USB** (USB Debugging).
4. Conecte o celular ao computador usando um cabo USB.
5. Quando o celular perguntar *"Permitir depuração USB deste computador?"*, marque *"Sempre permitir"* e toque em **OK**.

---

### 5. Compilar e Instalar Direto no Celular

#### Método A: Rodar direto (com recarregamento instantâneo - Hot Reload)
No PowerShell, dentro de `scooter_app`:
```powershell
flutter run
```
O Flutter vai compilar o código, transferir para o smartphone e abrir o aplicativo automaticamente na tela do seu celular!

#### Método B: Gerar o arquivo APK instalável
Se quiser gerar o arquivo `.apk` para enviar por WhatsApp ou instalar manualmente:
```powershell
flutter build apk --debug
```
O instalador será gerado em:
`scooter_app/build/app/outputs/flutter-apk/app-debug.apk`

Para instalar no celular via linha de comando:
```powershell
& "$env:LOCALAPPDATA\Android\Sdk\platform-tools\adb.exe" install -r build/app/outputs/flutter-apk/app-debug.apk
```
