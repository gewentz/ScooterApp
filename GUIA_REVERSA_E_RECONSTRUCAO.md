# Guia de Engenharia Reversa e Reconstrução do MiniRobot

Este documento sintetiza o diagnóstico do código descompilado do aplicativo **MiniRobot** e fornece o roteiro completo para extrair o protocolo de comunicação da camada nativa **Cocos2d-x** e reconstruir o aplicativo em uma tecnologia moderna (**Flutter** ou **React Native**).

---

## 1. Diagnóstico do Projeto Atual

* **Identificação do App:** MiniRobot (LebiTEC / Guangzhou Lebi Computer Co., Ltd.)
* **Package ID:** `com.loby.balance.car.google`
* **Tipo de Projeto:** Código descompilado (JADX) de um APK Android.
* **Propósito:** Telemetria, configuração de parâmetros, controle remoto e diagnóstico para veículos elétricos de mobilidade pessoal (Hoverboards Ninebot/MiniPro, patinetes estilo Xiaomi M365/E9PRO/MAX, Go-Karts elétricos).
* **Arquitetura Original:**
  * **Interface/Visual:** Engine de jogos C++ **Cocos2d-x** renderizando mostradores e painéis via OpenGL ES.
  * **Camada de Hardware/Android:** Código Java responsável pela comunicação BLE, sensores do smartphone e permissões.
  * **Backend (BaaS):** Plataforma **LeanCloud** (autenticação de usuário, registro, validação SMS).

---

## 2. Pontos-Chave no Código Java

### 2.1 Ponte JNI (Conexão Java $\leftrightarrow$ C++)
A comunicação entre o hardware Bluetooth e a lógica Cocos2d-x reside em:
* [AppActivity.java](file:///c:/Users/gabri/Documentos/Reverse/sources/org/cocos2dx/cpp/AppActivity.java):

```java
// 1. DADOS RECEBIDOS DO VEÍCULO (Java -> C++)
// Chamado quando a característica BLE notifica novos bytes:
public static native void ReadMsg(byte[] bArr);

// 2. DADOS ENVIADOS PARA O VEÍCULO (C++ -> Java)
// Chamado pela engine C++ quando o usuário toca em botões ou controles:
public static void SendMessage(byte[] bArr, int i) {
    if (instance.IsConnected()) {
        byte[] bArr2 = new byte[i];
        for (int i2 = 0; i2 < i; i2++) {
            bArr2[i2] = bArr[i2];
        }
        BluetoothHolder.getInstance().writeCharacteristic(bArr2);
    }
}
```

### 2.2 Serviços e Características Bluetooth (GATT UUIDs)
Definidos em [BluetoothHolder.java](file:///c:/Users/gabri/Documentos/Reverse/sources/org/cocos2dx/cpp/BluetoothHolder.java#L38-L52) e [BluetoothService.java](file:///c:/Users/gabri/Documentos/Reverse/sources/org/cocos2dx/cpp/BluetoothService.java#L33-L47):

| Tipo | UUID Serviço | UUID Característica (TX/RX) | Utilização |
| :--- | :--- | :--- | :--- |
| **Nordic UART** | `6E400001-B5A3-F393-E0A9-E50E24DCCA9E` | RX: `...0002` / TX: `...0003` | Padrão comum em placas nRF |
| **Serial Padrão** | `0000ffe0-0000-1000-8000-00805f9b34fb` | Notify: `0000ffe1-...` | Módulos Bluetooth estilo HM-10 |
| **Proprietário 1**| `0000ae00-0000-1000-8000-00805f9b34fb` | Notify: `0000ae01` / Read: `0000ae02` | Módulos proprietários chineses |
| **Proprietário 2**| `0000fff0-0000-1000-8000-00805f9b34fb` | Notify: `0000fff3` / Read: `0000fff7` | Módulos comuns em scooters E9/MAX |

### 2.3 Filtro de Dispositivos Compatíveis
O método `IsMyDevice(String name)` no [AppActivity.java](file:///c:/Users/gabri/Documentos/Reverse/sources/org/cocos2dx/cpp/AppActivity.java#L583-L590) filtra por estes prefixos Bluetooth:
> `M0`, `H1`, `M1`, `Mini`, `Plus`, `X1`, `X3`, `M6`, `GoKart`, `A6`, `MI`, `N3MTenbot`, `miniPLUS_`, `MiniPro`, `SFSO`, `V5Robot`, `EO STREET`, `XRIDER`, `TECAR`, `MAX`, `i10`, `NEXRIDE`, `E-WHEELS`, `E12`, `E9PRO`, `T10`, `KING`, `oP9G`, `Mentor`, `E9G`, `XT-`, `SPARK`, `EROBOT`.

### 2.4 Credenciais do Backend (LeanCloud)
Identificadas em [MyLeanCloudApp.java](file:///c:/Users/gabri/Documentos/Reverse/sources/org/cocos2dx/cpp/MyLeanCloudApp.java):
* **App ID:** `46zCeXLax6LkXK2aQ9Kz2I18-gzGzoHsz`
* **Server URL:** `https://46zcexla.lc-cn-n1-shared.com`

---

## 3. Como Descompilar a Camada Cocos2d-x (C++)

Como o Cocos2d-x é compilado diretamente para código de máquina ARM/x86:

1. **Obtenha a biblioteca nativa:**
   * Abra o arquivo original `.apk` usando o 7-Zip ou WinRAR.
   * Extraia o arquivo `libcocos2dcpp.so` localizado em `lib/arm64-v8a/` ou `lib/armeabi-v7a/`.

2. **Abra o arquivo no Ghidra:**
   * Baixe e instale o **[Ghidra](https://ghidra-sre.org/)** (ferramenta gratuita e open-source da NSA).
   * Crie um novo projeto e importe o `libcocos2dcpp.so`.
   * Permita que o Ghidra faça a análise automática inicial (auto-analysis).

3. **Localize as funções de comunicação:**
   * **Para recepção de dados:** Na janela *Symbol Tree* / *Functions*, pesquise por:
     ```text
     Java_org_cocos2dx_cpp_AppActivity_ReadMsg
     ```
     O descompilador do Ghidra mostrará o código C++ reconstituído. Acompanhe o parâmetro do buffer de bytes para ver onde ele faz o *parsing* (verificação de cabeçalho, cálculo de velocidade, leitura de bateria e odômetro).
   * **Para envio de comandos:** Na janela *Search* -> *Program Text* ou *Strings*, procure por:
     ```text
     SendMessage
     ```
     Encontre as referências cruzadas (Xrefs) dessa chamada JNI. Elas levarão às funções que montam os pacotes de ligar luzes, travar motor, ajustar velocidade máxima e controle remoto.

---

## 4. Método Alternativo e Mais Rápido: Sniffing BLE (Wireshark)

Se você possui um dos veículos compatíveis fisicamente, é possível capturar todo o protocolo sem precisar analisar assembly/C++:

1. **Ativar o Log no Android:**
   * No celular, vá em **Configurações > Opções do Desenvolvedor**.
   * Ative a opção **"Habilitar registro de rastreio de HCI do Bluetooth"** (Bluetooth HCI snoop log).
2. **Executar as Ações:**
   * Abra o aplicativo original **MiniRobot**.
   * Conecte-se ao veículo.
   * Execute as ações uma a uma, anotando o horário aproximado:
     * Ligar e desligar farol;
     * Ativar e desativar trava do motor;
     * Alternar modos de condução (Eco / Comfort / Sport);
     * Acelerar levemente para gerar dados de telemetria.
3. **Analisar no Wireshark:**
   * Desative o Bluetooth e recupere o arquivo gerado (geralmente em `/sdcard/btsnoop_hci.log` ou exportável via relatório de bugs do Android).
   * Abra o log no **Wireshark** e filtre por:
     ```text
     btatt
     ```
   * Você verá os pacotes hexadecimais brutos de *Write Command* (enviados) e *Handle Value Notification* (recebidos).

### Estrutura Padrão do Protocolo MiniRobot / M365
A maioria desses veículos utiliza variações do protocolo aberto Ninebot/M365:
```
[Header: 2 Bytes] [Comprimento: 1 Byte] [Destino: 1 Byte] [Comando: 1 Byte] [Registrador: 1 Byte] [Dados: N Bytes] [Checksum: 2 Bytes]
Exemplo de Header: 0x55 0xAA (ou 0x5A 0xA5)
```

---

## 5. Roteiro para Reconstrução com Tecnologias Modernas

### Por que **Flutter** é a melhor escolha?
* **BLE de Baixa Latência:** O plugin `flutter_blue_plus` é muito bem mantido, reativo com Streams e possui excelente suporte a Android e iOS.
* **Componentes de Telemetria:** Criação de velocímetros, mostradores e gráficos de consumo em 60/120 fps usando `syncfusion_flutter_gauges` ou `fl_chart`.
* **Sem a sobrecarga de uma Engine de Jogos:** O Cocos2d-x consome bateria excessiva renderizando um loop contínuo de jogo; uma interface declarativa moderna só renderiza quando há novos dados.

### Arquitetura Sugerida do Novo App (Flutter)

```
lib/
├── core/
│   ├── ble/
│   │   ├── ble_scanner.dart       // Escaneia prefixos (Mini, E9PRO, MAX...)
│   │   ├── ble_connection.dart    // Conexão GATT e escuta de notificações
│   │   └── ble_uuids.dart         // UUIDs mapeados neste guia
│   └── protocol/
│       ├── packet_parser.dart     // Decodifica bytes de velocidade, bateria, etc.
│       ├── command_builder.dart   // Monta pacotes (ligar farol, travar, etc.)
│       └── checksum.dart          // Cálculo de CRC ou soma de verificação
├── models/
│   ├── vehicle_telemetry.dart     // Velocidade atual, voltagem, distância, temperatura
│   └── vehicle_settings.dart      // Velocidade máx, modo de aceleração, farol
├── presentation/
│   ├── screens/
│   │   ├── scan_screen.dart       // Lista de dispositivos encontrados
│   │   ├── dashboard_screen.dart  // Velocímetro e controles principais
│   │   └── settings_screen.dart   // Ajustes de parâmetros
│   └── widgets/
│       └── speedometer_gauge.dart // Mostrador do velocímetro
└── main.dart
```

---

## 6. Próximos Passos Recomendados

1. **Verificar os pacotes existentes no GitHub:**  
   Antes de começar do zero, pesquise por projetos como `m365-ble`, `hoverboard-firmware-hack` ou `minirobot-protocol`. Grande parte das mensagens e registradores já está decodificada pela comunidade.
2. **Iniciar o projeto Flutter:**
   ```bash
   flutter create minirobot_modern
   cd minirobot_modern
   flutter pub add flutter_blue_plus syncfusion_flutter_gauges provider
   ```
3. **Implementar a conexão BLE:**
   Utilizar os UUIDs documentados na [Seção 2.2](#22-serviços-e-características-bluetooth-gatt-uuids) para conectar e assinar a característica de notificação.
