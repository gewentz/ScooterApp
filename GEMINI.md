# Contexto do Projeto: Reconstrução App Patinete Elétrico (E-Scooter)

Este documento define o contexto, decisões de arquitetura e diretrizes para o desenvolvimento do novo aplicativo focado exclusivamente no controle e telemetria de **Patinetes Elétricos** (compatíveis com o ecossistema MiniRobot / LebiTEC / clones M365).

---

## 1. Escopo do Projeto

* **Foco Único:** Patinetes Elétricos (E-Scooters). Componentes de hoverboards, karts e robôs de duas rodas foram descartados.
* **Dispositivo Alvo Identificado:** `M0RobotDFE54C32BA` (Controladora padrão MiniRobot / X-Bot / LebiTEC - Prefixo `M0`).
* **Estratégia de Engenharia Reversa:** Abordagem de **caixa-preta (Black-Box)** via comunicação UART sobre Bluetooth Low Energy (BLE) e protocolos abertos da comunidade. **Não é necessária a descompilação da engine C++ Cocos2d-x**.
* **Objetivo:** Criar um aplicativo moderno, fluido, com baixo consumo de bateria e interface reativa utilizando **Flutter**.

---

## 2. Parâmetros de Conexão Bluetooth (BLE)

### 2.1 Prefixos de Patinetes Compatíveis
Filtro de busca para localização de veículos (extraído de `IsMyDevice` no Java original):
* `E9PRO`, `E9G`, `MAX`, `E12`, `E-WHEELS`, `TECAR`, `XRIDER`, `EO STREET`, `i10`, `NEXRIDE`, `XT-`, `SPARK`, `KING`, `SFSO`.

### 2.2 UUIDs GATT (Serviços e Características)
A comunicação serial transparente opera primordialmente sob estes identificadores:

| Perfil | UUID Serviço | UUID TX (Write) | UUID RX (Notify) |
| :--- | :--- | :--- | :--- |
| **Serial Padrão (HM-10/Clone)** | `0000ffe0-0000-1000-8000-00805f9b34fb` | `0000ffe1-0000-1000-8000-00805f9b34fb` | `0000ffe1-0000-1000-8000-00805f9b34fb` |
| **Nordic UART (nRF)** | `6e400001-b5a3-f393-e0a9-e50e24dcca9e` | `6e400002-b5a3-f393-e0a9-e50e24dcca9e` | `6e400003-b5a3-f393-e0a9-e50e24dcca9e` |
| **Controladoras E9/MAX** | `0000fff0-0000-1000-8000-00805f9b34fb` | `0000fff3-0000-1000-8000-00805f9b34fb` | `0000fff4-0000-1000-8000-00805f9b34fb` |
| **Controladoras Custom** | `0000ae00-0000-1000-8000-00805f9b34fb` | `0000ae01-0000-1000-8000-00805f9b34fb` | `0000ae02-0000-1000-8000-00805f9b34fb` |

---

## 3. Protocolo de Comunicação do Patinete

### 3.1 Estrutura Básica do Frame (Padrão Ninebot/M365/LebiTEC)
```
[Header: 2B] [Comprimento: 1B] [Destino: 1B] [Comando: 1B] [Registrador: 1B] [Dados: NB] [Checksum: 2B]
```
* **Headers usuais:** `0x55 0xAA` ou `0x5A 0xA5`.
* **Destino comum:** `0x20` (Controladora principal / ESC).
* **Checksum:** Soma simples de 16 bits invertida (`XOR 0xFFFF` ou `Sum & 0xFFFF`).

### 3.2 Comandos e Funcionalidades do Patinete
1. **Luzes e Farol:** Ligar / Desligar o farol dianteiro e lanterna traseira.
2. **Trava do Motor:** Bloqueio eletrônico do motor de indução (aciona freio eletromagnético e alarme ao tentar empurrar).
3. **Modos de Velocidade:**
   * Modo Eco (ex: 15 km/h)
   * Modo Condução / Drive (ex: 20 km/h)
   * Modo Sport (ex: 25 km/h a 30 km/h)
4. **Modo de Partida:**
   * *Zero Start:* Acelera parado.
   * *Kick-to-start:* Exige impulso físico inicial antes do acelerador responder.
5. **Controle de Cruzeiro (Piloto Automático):** Manter velocidade constante após 5 segundos com aceleração fixa.
6. **Leitura de Telemetria:**
   * Velocidade atual instantânea (km/h)
   * Nível de bateria (%) e tensão (V)
   * Odômetro parcial (viagem atual) e odômetro total (km acumulados)
   * Temperatura da placa controladora
   * Códigos de erro do veículo

---

## 4. Arquitetura da Solução em Flutter

* **Stack Recomendada:**
  * **Framework:** Flutter (Android & iOS).
  * **BLE:** `flutter_blue_plus`.
  * **Gerenciamento de Estado:** `flutter_riverpod` ou `provider`.
  * **Visualização & Gauges:** `syncfusion_flutter_gauges` para o velocímetro dinâmico em 60/120fps.
  * **Armazenamento Local:** `shared_preferences` para memorizar o último patinete pareado.

### 4.1 Estrutura de Módulos
```
lib/
├── core/
│   ├── ble/                # Scanner, conexão GATT, subscrição de características
│   └── protocol/           # Parser de pacotes binários, gerador de comandos e checksum
├── domain/
│   └── models/             # ScooterTelemetry, ScooterSettings, ScooterCommand
├── state/                  # Providers de conexão e estado do veículo
└── presentation/
    ├── dashboard/          # Velocímetro em tempo real, odômetro, bateria
    ├── controls/           # Botões rápidos: Farol, Trava, Modo de Condução
    ├── settings/           # Ajuste de velocidade máxima, cruise control, zero-start
    └── scan/               # Tela de pareamento Bluetooth
```

---

## 5. Diretrizes de Desenvolvimento

1. **Eficiência Energética:** Atualizar a tela somente mediante alterações de estado (sem loops de renderização pesados).
2. **Reconexão Automática:** Se a conexão cair temporariamente, o app deve tentar restabelecer o link BLE de forma silenciosa e resiliente.
3. **Segurança:** Nunca permitir comandos críticos (como travar o motor) enquanto a velocidade detectada for maior que 0 km/h.
