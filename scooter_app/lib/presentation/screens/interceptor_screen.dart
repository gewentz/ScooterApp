import 'dart:async';
import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:flutter_blue_plus/flutter_blue_plus.dart';
import '../../core/ble/scooter_ble_service.dart';
import '../../core/models/ble_traffic_log.dart';
import '../../core/models/scooter_enums.dart';
import '../../core/protocol/packet_builder.dart';
import '../../core/protocol/scooter_constants.dart';
import '../theme/app_theme.dart';

/// Tela de Diagnóstico Avançado, Interceptação e Mapeamento 1-a-1 de Funções BLE
class InterceptorScreen extends StatefulWidget {
  final ScooterBleService bleService;

  const InterceptorScreen({super.key, required this.bleService});

  @override
  State<InterceptorScreen> createState() => _InterceptorScreenState();
}

class _InterceptorScreenState extends State<InterceptorScreen> with SingleTickerProviderStateMixin {
  late TabController _tabController;
  final ScrollController _scrollController = ScrollController();
  final TextEditingController _hexInputController = TextEditingController(text: '55 AA 03 06 01 B0 1C');
  final List<BleTrafficLog> _logs = [];
  StreamSubscription<BleTrafficLog>? _logSub;

  // Filtros e opções
  bool _autoScroll = true;
  bool _filterRxOnly = false;
  bool _filterTxOnly = false;
  bool _writeWithoutResponse = false;
  bool _autoCalculateChecksum = true;
  BluetoothCharacteristic? _selectedWriteChar;

  // Dicionário de funções mapeadas pelo usuário
  final Map<String, String> _mappedFunctions = {};

  // Estado do assistente guiado
  int _wizardStep = 0;
  BleTrafficLog? _lastRxPacket;
  String _assistantStatusMessage = 'Aguardando ação no patinete...';
  bool _isProbing = false;

  @override
  void initState() {
    super.initState();
    _tabController = TabController(length: 2, vsync: this);

    // Carregar histórico prévio do transporte
    _logs.addAll(widget.bleService.trafficHistory);

    // Ouvir novos pacotes em tempo real
    _logSub = widget.bleService.trafficLogStream.listen((log) {
      if (!mounted) return;
      setState(() {
        _logs.add(log);
        if (log.direction == TrafficDirection.rx) {
          _lastRxPacket = log;
          _assistantStatusMessage = '🎉 Pacote interceptado do patinete! (${log.bytes.length} bytes)';
        }
      });

      if (_autoScroll && _scrollController.hasClients) {
        Future.delayed(const Duration(milliseconds: 50), () {
          if (_scrollController.hasClients) {
            _scrollController.animateTo(
              _scrollController.position.maxScrollExtent,
              duration: const Duration(milliseconds: 200),
              curve: Curves.easeOut,
            );
          }
        });
      }
    });

    // Selecionar característica de escrita UART preferencial (Nordic UART txChar ou primeira válida)
    _selectedWriteChar = widget.bleService.txCharacteristic ??
        (widget.bleService.writableCharacteristics.isNotEmpty
            ? widget.bleService.writableCharacteristics.first
            : null);
  }

  @override
  void dispose() {
    _logSub?.cancel();
    _scrollController.dispose();
    _hexInputController.dispose();
    _tabController.dispose();
    super.dispose();
  }

  // --------------------------------------------------------------------------
  // AÇÕES DO INJETOR E TESTES
  // --------------------------------------------------------------------------

  Future<void> _sendCustomHex() async {
    String text = _hexInputController.text.trim();
    if (text.isEmpty) return;

    if (_autoCalculateChecksum) {
      final clean = text.replaceAll(RegExp(r'[^0-9a-fA-F]'), '');
      if (clean.length >= 6) {
        final List<int> raw = [];
        for (int i = 0; i < clean.length; i += 2) {
          raw.add(int.parse(clean.substring(i, i + 2), radix: 16));
        }

        if ((raw[0] == 0x55 && raw[1] == 0xAA) || (raw[0] == 0x5A && raw[1] == 0xA5)) {
          int sum = 0;
          for (int i = 2; i < raw.length; i++) {
            sum += raw[i];
          }
          final chk = ((sum & 0xFFFF) ^ 0xFFFF);
          final chkLow = chk & 0xFF;
          final chkHigh = (chk >> 8) & 0xFF;

          final fullList = List<int>.from(raw)..add(chkLow)..add(chkHigh);
          text = fullList.map((b) => b.toRadixString(16).padLeft(2, '0').toUpperCase()).join(' ');
          _hexInputController.text = text;
        }
      }
    }

    final success = await widget.bleService.sendRawHex(
      text,
      withoutResponse: _writeWithoutResponse,
      customChar: _selectedWriteChar,
    );

    if (!success && mounted) {
      ScaffoldMessenger.of(context).showSnackBar(
        const SnackBar(
          content: Text('Falha ao enviar pacote HEX! Verifique a conexão BLE.'),
          backgroundColor: AppTheme.lockCrimson,
        ),
      );
    }
  }

  Future<void> _sendPresetPacket(List<int> packet, String description) async {
    setState(() {
      _assistantStatusMessage = 'Enviando: $description...';
    });

    final success = await widget.bleService.sendRawBytes(
      packet,
      withoutResponse: _writeWithoutResponse,
      customChar: _selectedWriteChar,
    );

    if (mounted) {
      ScaffoldMessenger.of(context).showSnackBar(
        SnackBar(
          content: Text(success ? 'Enviado: $description' : 'Falha no envio!'),
          backgroundColor: success ? AppTheme.surfaceLight : AppTheme.lockCrimson,
          duration: const Duration(seconds: 1),
        ),
      );
    }
  }

  Future<void> _runAutoProbe() async {
    if (_isProbing) return;
    setState(() {
      _isProbing = true;
      _assistantStatusMessage = 'Iniciando Auto-Sonda de destinos e registradores...';
    });

    final destinations = [
      ScooterConstants.addrMiniRobot, // 0x06
      ScooterConstants.addrEsc,       // 0x20
      ScooterConstants.addrBms,       // 0x21
      ScooterConstants.addrDashboard, // 0x22
    ];

    final registers = [
      ScooterConstants.regVersion,          // 0x1A
      ScooterConstants.regSerial,           // 0x10
      ScooterConstants.regTelemetryGeneral, // 0xB0
    ];

    int responsesFound = 0;
    final initialRxCount = _logs.where((l) => l.direction == TrafficDirection.rx).length;

    for (final dest in destinations) {
      for (final reg in registers) {
        if (!_isProbing || !mounted) break;

        setState(() {
          _assistantStatusMessage =
              'Sondando Dest: 0x${dest.toRadixString(16).padLeft(2, '0').toUpperCase()} | Reg: 0x${reg.toRadixString(16).padLeft(2, '0').toUpperCase()}...';
        });

        final pkt = PacketBuilder.buildFrame(
          destination: dest,
          command: ScooterConstants.cmdRead,
          register: reg,
          payload: [(reg == ScooterConstants.regTelemetryGeneral) ? 0x1C : 0x02],
        );

        await widget.bleService.sendRawBytes(
          pkt,
          withoutResponse: _writeWithoutResponse,
          customChar: _selectedWriteChar,
        );

        await Future.delayed(const Duration(milliseconds: 250));

        final currentRxCount = _logs.where((l) => l.direction == TrafficDirection.rx).length;
        if (currentRxCount > initialRxCount + responsesFound) {
          responsesFound = currentRxCount - initialRxCount;
          setState(() {
            _assistantStatusMessage =
                '✅ RESPOSTA DETECTADA no Destino 0x${dest.toRadixString(16).padLeft(2, '0').toUpperCase()}!';
          });
          await Future.delayed(const Duration(milliseconds: 500));
        }
      }
    }

    if (mounted) {
      setState(() {
        _isProbing = false;
        _assistantStatusMessage = responsesFound > 0
            ? '🎉 Auto-Sonda concluída! $responsesFound resposta(s) obtida(s).'
            : 'Sonda concluída. Nenhuma resposta RX automática detectada. Teste alternando "WithResponse".';
      });
    }
  }

  void _tagPacket(BleTrafficLog packet) {
    final textController = TextEditingController(text: packet.taggedFunction ?? '');
    showDialog(
      context: context,
      builder: (ctx) => AlertDialog(
        backgroundColor: AppTheme.surface,
        title: const Text('Identificar e Marcar Função'),
        content: Column(
          mainAxisSize: MainAxisSize.min,
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Text(
              'Hex: ${packet.hexString}',
              style: const TextStyle(fontSize: 12, fontFamily: 'monospace', color: AppTheme.electricCyan),
            ),
            const SizedBox(height: 12),
            const Text('Qual função este pacote representa?', style: TextStyle(fontSize: 13)),
            const SizedBox(height: 8),
            Wrap(
              spacing: 6,
              runSpacing: 6,
              children: [
                _quickTagChip('Farol ON', textController),
                _quickTagChip('Farol OFF', textController),
                _quickTagChip('Trava Motor', textController),
                _quickTagChip('Destrava Motor', textController),
                _quickTagChip('Modo Eco', textController),
                _quickTagChip('Modo Drive', textController),
                _quickTagChip('Modo Sport', textController),
                _quickTagChip('Velocidade/Giro', textController),
                _quickTagChip('Freio', textController),
              ],
            ),
            const SizedBox(height: 12),
            TextField(
              controller: textController,
              decoration: const InputDecoration(
                labelText: 'Nome da Função / Descrição',
                hintText: 'Ex: Farol Ligado pelo Botão Físico',
              ),
            ),
          ],
        ),
        actions: [
          TextButton(
            onPressed: () => Navigator.pop(ctx),
            child: const Text('Cancelar'),
          ),
          ElevatedButton(
            onPressed: () {
              final label = textController.text.trim();
              if (label.isNotEmpty) {
                setState(() {
                  _mappedFunctions[packet.hexString] = label;
                  final idx = _logs.indexOf(packet);
                  if (idx >= 0) {
                    _logs[idx] = packet.copyWith(taggedFunction: label);
                  }
                });
              }
              Navigator.pop(ctx);
            },
            child: const Text('Salvar Marcação'),
          ),
        ],
      ),
    );
  }

  Widget _quickTagChip(String label, TextEditingController controller) {
    return ActionChip(
      label: Text(label, style: const TextStyle(fontSize: 11)),
      backgroundColor: AppTheme.surfaceLight,
      onPressed: () {
        controller.text = label;
      },
    );
  }

  void _showGattDetails() {
    showModalBottomSheet(
      context: context,
      backgroundColor: AppTheme.surface,
      isScrollControlled: true,
      builder: (ctx) => DraggableScrollableSheet(
        expand: false,
        initialChildSize: 0.6,
        maxChildSize: 0.9,
        builder: (_, scrollCtrl) {
          final services = widget.bleService.discoveredServices;
          return Column(
            children: [
              Padding(
                padding: const EdgeInsets.all(16.0),
                child: Row(
                  children: [
                    const Icon(Icons.hub_rounded, color: AppTheme.electricCyan),
                    const SizedBox(width: 10),
                    const Text(
                      'Serviços & Características GATT',
                      style: TextStyle(fontSize: 16, fontWeight: FontWeight.bold),
                    ),
                    const Spacer(),
                    IconButton(
                      icon: const Icon(Icons.close),
                      onPressed: () => Navigator.pop(ctx),
                    ),
                  ],
                ),
              ),
              Expanded(
                child: services.isEmpty
                    ? const Center(child: Text('Nenhum serviço descoberto.'))
                    : ListView.builder(
                        controller: scrollCtrl,
                        itemCount: services.length,
                        itemBuilder: (_, sIdx) {
                          final s = services[sIdx];
                          return ExpansionTile(
                            initiallyExpanded: true,
                            leading: const Icon(Icons.folder_open_rounded, color: AppTheme.warningAmber),
                            title: Text(
                              'Serviço: ${s.uuid}',
                              style: const TextStyle(fontSize: 13, fontWeight: FontWeight.w600),
                            ),
                            children: s.characteristics.map((c) {
                              final p = c.properties;
                              final props = <String>[];
                              if (p.read) props.add('READ');
                              if (p.write) props.add('WRITE');
                              if (p.writeWithoutResponse) props.add('WRITE_NO_RESP');
                              if (p.notify) props.add('NOTIFY');
                              if (p.indicate) props.add('INDICATE');

                              return ListTile(
                                dense: true,
                                leading: const Icon(Icons.code, size: 18, color: AppTheme.textMuted),
                                title: Text(
                                  c.uuid.toString(),
                                  style: const TextStyle(fontSize: 12, fontFamily: 'monospace'),
                                ),
                                subtitle: Text(
                                  props.join(' | '),
                                  style: TextStyle(
                                    fontSize: 10,
                                    color: (p.notify || p.indicate)
                                        ? AppTheme.neonGreen
                                        : (p.write || p.writeWithoutResponse)
                                            ? AppTheme.electricCyan
                                            : AppTheme.textMuted,
                                    fontWeight: FontWeight.bold,
                                  ),
                                ),
                              );
                            }).toList(),
                          );
                        },
                      ),
              ),
            ],
          );
        },
      ),
    );
  }

  void _copyAllLogs() {
    if (_logs.isEmpty) return;
    final sb = StringBuffer();
    sb.writeln('=== BLE SNIFFER TRAFFIC DUMP ===');
    sb.writeln('Dispositivo: ${widget.bleService.connectedDevice?.name} (${widget.bleService.connectedDevice?.id})');
    sb.writeln('Total de pacotes: ${_logs.length}\n');

    for (final l in _logs) {
      final tag = l.taggedFunction != null ? ' [TAG: ${l.taggedFunction}]' : '';
      sb.writeln('${l.timestamp.toIso8601String()} | ${l.directionLabel}$tag: ${l.hexString} (${l.parsedSummary})');
    }

    Clipboard.setData(ClipboardData(text: sb.toString()));
    ScaffoldMessenger.of(context).showSnackBar(
      const SnackBar(content: Text('Todo o histórico de tráfego copiado!')),
    );
  }

  @override
  Widget build(BuildContext context) {
    final filteredLogs = _logs.where((l) {
      if (_filterRxOnly && l.direction != TrafficDirection.rx) return false;
      if (_filterTxOnly && l.direction != TrafficDirection.tx) return false;
      return true;
    }).toList();

    return Scaffold(
      appBar: AppBar(
        title: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            const Text('Interceptador & Mapeamento', style: TextStyle(fontSize: 16)),
            Text(
              widget.bleService.connectedDevice != null
                  ? '${widget.bleService.connectedDevice!.name} (${widget.bleService.connectedDevice!.id})'
                  : 'Desconectado',
              style: const TextStyle(fontSize: 11, color: AppTheme.textMuted),
            ),
          ],
        ),
        actions: [
          IconButton(
            icon: const Icon(Icons.info_outline_rounded),
            tooltip: 'Ver Serviços GATT',
            onPressed: _showGattDetails,
          ),
          IconButton(
            icon: Icon(
              widget.bleService.isTelemetryPollingActive ? Icons.pause_circle_rounded : Icons.play_circle_rounded,
              color: widget.bleService.isTelemetryPollingActive ? AppTheme.warningAmber : AppTheme.neonGreen,
            ),
            tooltip: widget.bleService.isTelemetryPollingActive ? 'Pausar Polling' : 'Retomar Polling',
            onPressed: () {
              setState(() {
                widget.bleService.setTelemetryPolling(!widget.bleService.isTelemetryPollingActive);
              });
            },
          ),
          IconButton(
            icon: const Icon(Icons.delete_sweep_rounded),
            tooltip: 'Limpar Logs',
            onPressed: () {
              setState(() {
                _logs.clear();
                widget.bleService.clearTrafficLogs();
              });
            },
          ),
          IconButton(
            icon: const Icon(Icons.copy_all_rounded),
            tooltip: 'Copiar Tudo',
            onPressed: _copyAllLogs,
          ),
        ],
        bottom: TabBar(
          controller: _tabController,
          indicatorColor: AppTheme.electricCyan,
          tabs: const [
            Tab(icon: Icon(Icons.assistant_navigation), text: 'Assistente 1-a-1'),
            Tab(icon: Icon(Icons.terminal_rounded), text: 'Sniffer & Injetor'),
          ],
        ),
      ),
      body: TabBarView(
        controller: _tabController,
        children: [
          _buildGuidedWizardTab(),
          _buildSnifferTerminalTab(filteredLogs),
        ],
      ),
    );
  }

  // --------------------------------------------------------------------------
  // ABA 1: ASSISTENTE 1-A-1
  // --------------------------------------------------------------------------

  Widget _buildGuidedWizardTab() {
    return SingleChildScrollView(
      padding: const EdgeInsets.all(16.0),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.stretch,
        children: [
          Card(
            color: AppTheme.surfaceLight,
            elevation: 2,
            shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(16)),
            child: Padding(
              padding: const EdgeInsets.all(16.0),
              child: Column(
                children: [
                  Row(
                    children: [
                      Icon(
                        _lastRxPacket != null ? Icons.sensors_rounded : Icons.sensors_off_rounded,
                        color: _lastRxPacket != null ? AppTheme.neonGreen : AppTheme.warningAmber,
                      ),
                      const SizedBox(width: 10),
                      Expanded(
                        child: Text(
                          _assistantStatusMessage,
                          style: TextStyle(
                            fontSize: 13,
                            fontWeight: FontWeight.bold,
                            color: _lastRxPacket != null ? AppTheme.neonGreen : AppTheme.textPrimary,
                          ),
                        ),
                      ),
                    ],
                  ),
                  if (_lastRxPacket != null) ...[
                    const Divider(height: 20),
                    Row(
                      children: [
                        Expanded(
                          child: Column(
                            crossAxisAlignment: CrossAxisAlignment.start,
                            children: [
                              const Text('Último Pacote RX Capturado:', style: TextStyle(fontSize: 11, color: AppTheme.textMuted)),
                              const SizedBox(height: 4),
                              Text(
                                _lastRxPacket!.hexString,
                                style: const TextStyle(
                                  fontSize: 12,
                                  fontFamily: 'monospace',
                                  fontWeight: FontWeight.bold,
                                  color: AppTheme.warningAmber,
                                ),
                              ),
                            ],
                          ),
                        ),
                        ElevatedButton.icon(
                          icon: const Icon(Icons.bookmark_add_rounded, size: 16),
                          label: const Text('Marcar', style: TextStyle(fontSize: 12)),
                          style: ElevatedButton.styleFrom(backgroundColor: AppTheme.neonGreen, foregroundColor: Colors.black),
                          onPressed: () => _tagPacket(_lastRxPacket!),
                        ),
                      ],
                    ),
                  ],
                ],
              ),
            ),
          ),
          const SizedBox(height: 16),

          Row(
            mainAxisAlignment: MainAxisAlignment.spaceBetween,
            children: [
              _stepChip(0, '1. Farol'),
              _stepChip(1, '2. Trava'),
              _stepChip(2, '3. Modos'),
              _stepChip(3, '4. Roda/Sensor'),
              _stepChip(4, '5. Auto-Sonda'),
            ],
          ),
          const SizedBox(height: 16),

          _buildCurrentWizardStepContent(),

          const SizedBox(height: 24),

          if (_mappedFunctions.isNotEmpty) ...[
            const Text(
              'Funções Mapeadas & Confirmadas:',
              style: TextStyle(fontSize: 14, fontWeight: FontWeight.bold, color: AppTheme.electricCyan),
            ),
            const SizedBox(height: 8),
            Card(
              color: AppTheme.surfaceLight,
              child: ListView.separated(
                shrinkWrap: true,
                physics: const NeverScrollableScrollPhysics(),
                itemCount: _mappedFunctions.length,
                separatorBuilder: (_, __) => const Divider(height: 1),
                itemBuilder: (_, idx) {
                  final hex = _mappedFunctions.keys.elementAt(idx);
                  final name = _mappedFunctions[hex]!;
                  return ListTile(
                    dense: true,
                    leading: const Icon(Icons.check_circle_rounded, color: AppTheme.neonGreen, size: 20),
                    title: Text(name, style: const TextStyle(fontWeight: FontWeight.bold, fontSize: 13)),
                    subtitle: Text(hex, style: const TextStyle(fontFamily: 'monospace', fontSize: 11)),
                    trailing: IconButton(
                      icon: const Icon(Icons.delete_outline, size: 18),
                      onPressed: () {
                        setState(() {
                          _mappedFunctions.remove(hex);
                        });
                      },
                    ),
                  );
                },
              ),
            ),
          ],
        ],
      ),
    );
  }

  Widget _stepChip(int stepIndex, String title) {
    final isSelected = _wizardStep == stepIndex;
    return GestureDetector(
      onTap: () => setState(() => _wizardStep = stepIndex),
      child: Container(
        padding: const EdgeInsets.symmetric(horizontal: 10, vertical: 8),
        decoration: BoxDecoration(
          color: isSelected ? AppTheme.electricCyan : AppTheme.surfaceLight,
          borderRadius: BorderRadius.circular(10),
          border: Border.all(
            color: isSelected ? AppTheme.electricCyan : AppTheme.cardBorder,
          ),
        ),
        child: Text(
          title,
          style: TextStyle(
            fontSize: 11,
            fontWeight: FontWeight.bold,
            color: isSelected ? Colors.black : AppTheme.textSecondary,
          ),
        ),
      ),
    );
  }

  Widget _buildCurrentWizardStepContent() {
    switch (_wizardStep) {
      case 0:
        return _buildStepCard(
          title: 'Etapa 1: Farol Dianteiro / Luz (Reversa Oficial)',
          instruction:
              'No binário libcpp.so, controladoras M0 usam destino 0x0A e registrador 0xD3.\n'
              'O farol pode responder no Bit 0 (0x01), no Bit 7 (0x80) ou combinado (0x81). Teste as opções:',
          actions: [
            Wrap(
              spacing: 8,
              runSpacing: 8,
              children: [
                ElevatedButton.icon(
                  icon: const Icon(Icons.lightbulb_rounded, size: 18),
                  label: const Text('Farol ON (M0 0x0A, D3=0x01)'),
                  style: ElevatedButton.styleFrom(backgroundColor: AppTheme.electricCyan, foregroundColor: Colors.black),
                  onPressed: () => _sendPresetPacket(
                    PacketBuilder.setHeadlight(true, destination: ScooterConstants.addrEsc),
                    'Farol ON (0x0A, D3=0x01)',
                  ),
                ),
                ElevatedButton.icon(
                  icon: const Icon(Icons.lightbulb_outline_rounded, size: 18),
                  label: const Text('Farol OFF (M0 0x0A, D3=0x00)'),
                  onPressed: () => _sendPresetPacket(
                    PacketBuilder.setHeadlight(false, destination: ScooterConstants.addrEsc),
                    'Farol OFF (0x0A, D3=0x00)',
                  ),
                ),
                ElevatedButton.icon(
                  icon: const Icon(Icons.lightbulb_rounded, size: 18),
                  label: const Text('Farol ON (M0 0x0A, D3=0x80)'),
                  style: ElevatedButton.styleFrom(backgroundColor: AppTheme.neonGreen, foregroundColor: Colors.black),
                  onPressed: () => _sendPresetPacket(
                    PacketBuilder.setHeadlightBit7(true, destination: ScooterConstants.addrEsc),
                    'Farol ON (0x0A, D3=0x80)',
                  ),
                ),
                ElevatedButton.icon(
                  icon: const Icon(Icons.lightbulb_rounded, size: 18),
                  label: const Text('Farol ON (M0 0x0A, D3=0x81)'),
                  style: ElevatedButton.styleFrom(backgroundColor: AppTheme.warningAmber, foregroundColor: Colors.black),
                  onPressed: () => _sendPresetPacket(
                    PacketBuilder.setHeadlightCombined(true, destination: ScooterConstants.addrEsc),
                    'Farol ON (0x0A, D3=0x81)',
                  ),
                ),
                OutlinedButton.icon(
                  icon: const Icon(Icons.lightbulb_rounded, size: 18),
                  label: const Text('Farol ON (Ninebot 0x20)'),
                  onPressed: () => _sendPresetPacket(
                    PacketBuilder.setHeadlight(true, destination: ScooterConstants.addrEscNinebot),
                    'Farol ON (0x20)',
                  ),
                ),
                OutlinedButton.icon(
                  icon: const Icon(Icons.lightbulb_outline_rounded, size: 18),
                  label: const Text('Farol OFF (Ninebot 0x20)'),
                  onPressed: () => _sendPresetPacket(
                    PacketBuilder.setHeadlight(false, destination: ScooterConstants.addrEscNinebot),
                    'Farol OFF (0x20)',
                  ),
                ),
                OutlinedButton.icon(
                  icon: const Icon(Icons.lightbulb_rounded, size: 18),
                  label: const Text('Farol Alt ON (0xF2)'),
                  onPressed: () => _sendPresetPacket(
                    PacketBuilder.setHeadlightAlt(true, destination: ScooterConstants.addrEsc),
                    'Farol Alt ON (0xF2)',
                  ),
                ),
              ],
            ),
          ],
        );

      case 1:
        return _buildStepCard(
          title: 'Etapa 2: Trava Eletrônica do Motor (Reversa Oficial)',
          instruction:
              'O app oficial comanda a trava escrevendo 0x0001 no registrador 0x70 e destrava com 0x0000.\n'
              'Teste com destino M0 (0x0A) e Ninebot (0x20):',
          actions: [
            Wrap(
              spacing: 8,
              runSpacing: 8,
              children: [
                ElevatedButton.icon(
                  icon: const Icon(Icons.lock_rounded, size: 18),
                  label: const Text('Travar Motor (M0 0x0A, 0x70=1)'),
                  style: ElevatedButton.styleFrom(backgroundColor: AppTheme.lockCrimson),
                  onPressed: () => _sendPresetPacket(
                    PacketBuilder.setMotorLock(true, destination: ScooterConstants.addrEsc),
                    'Travar Motor (0x0A, 0x70)',
                  ),
                ),
                ElevatedButton.icon(
                  icon: const Icon(Icons.lock_open_rounded, size: 18),
                  label: const Text('Destravar (M0 0x0A, 0x70=0)'),
                  style: ElevatedButton.styleFrom(backgroundColor: AppTheme.neonGreen, foregroundColor: Colors.black),
                  onPressed: () => _sendPresetPacket(
                    PacketBuilder.setMotorLock(false, destination: ScooterConstants.addrEsc),
                    'Destravar Motor (0x0A, 0x70)',
                  ),
                ),
                OutlinedButton.icon(
                  icon: const Icon(Icons.lock_rounded, size: 18),
                  label: const Text('Travar Motor (Ninebot 0x20)'),
                  onPressed: () => _sendPresetPacket(
                    PacketBuilder.setMotorLock(true, destination: ScooterConstants.addrEscNinebot),
                    'Travar Motor (0x20, 0x70)',
                  ),
                ),
                OutlinedButton.icon(
                  icon: const Icon(Icons.lock_open_rounded, size: 18),
                  label: const Text('Destravar (Ninebot 0x20)'),
                  onPressed: () => _sendPresetPacket(
                    PacketBuilder.setMotorLock(false, destination: ScooterConstants.addrEscNinebot),
                    'Destravar Motor (0x20, 0x70)',
                  ),
                ),
              ],
            ),
          ],
        );

      case 2:
        return _buildStepCard(
          title: 'Etapa 3: Modos de Condução e Partida (Reversa Oficial)',
          instruction:
              'Registradores oficiais: 0x7E (Modos: 0=Eco, 1=Drive, 2=Sport), '
              '0x7D (Zero-Start: 1=Zero, 0=Kick) e 0x7C (Piloto Automático).',
          actions: [
            Wrap(
              spacing: 8,
              runSpacing: 8,
              children: [
                ElevatedButton(
                  onPressed: () => _sendPresetPacket(
                    PacketBuilder.setSpeedMode(SpeedMode.eco, destination: ScooterConstants.addrEsc),
                    'Modo ECO (0x0A, 0x7E=0)',
                  ),
                  child: const Text('Modo ECO (0x0A)'),
                ),
                ElevatedButton(
                  onPressed: () => _sendPresetPacket(
                    PacketBuilder.setSpeedMode(SpeedMode.drive, destination: ScooterConstants.addrEsc),
                    'Modo DRIVE (0x0A, 0x7E=1)',
                  ),
                  child: const Text('Modo DRIVE (0x0A)'),
                ),
                ElevatedButton(
                  onPressed: () => _sendPresetPacket(
                    PacketBuilder.setSpeedMode(SpeedMode.sport, destination: ScooterConstants.addrEsc),
                    'Modo SPORT (0x0A, 0x7E=2)',
                  ),
                  child: const Text('Modo SPORT (0x0A)'),
                ),
                ElevatedButton(
                  onPressed: () => _sendPresetPacket(
                    PacketBuilder.setSpeedMode(SpeedMode.sport, destination: ScooterConstants.addrEscNinebot),
                    'Modo SPORT (0x20, 0x7E=2)',
                  ),
                  child: const Text('SPORT (0x20)'),
                ),
                OutlinedButton(
                  onPressed: () => _sendPresetPacket(
                    PacketBuilder.setStartMode(StartMode.zeroStart, destination: ScooterConstants.addrEsc),
                    'Zero-Start ON (0x0A, 0x7D=1)',
                  ),
                  child: const Text('Zero-Start ON'),
                ),
                OutlinedButton(
                  onPressed: () => _sendPresetPacket(
                    PacketBuilder.setStartMode(StartMode.kickToStart, destination: ScooterConstants.addrEsc),
                    'Kick-Start ON (0x0A, 0x7D=0)',
                  ),
                  child: const Text('Kick-Start ON'),
                ),
                OutlinedButton(
                  onPressed: () => _sendPresetPacket(
                    PacketBuilder.setCruiseControl(true, destination: ScooterConstants.addrEsc),
                    'Piloto ON (0x0A, 0x7C=1)',
                  ),
                  child: const Text('Piloto ON'),
                ),
                OutlinedButton(
                  onPressed: () => _sendPresetPacket(
                    PacketBuilder.setCruiseControl(false, destination: ScooterConstants.addrEsc),
                    'Piloto OFF (0x0A, 0x7C=0)',
                  ),
                  child: const Text('Piloto OFF'),
                ),
              ],
            ),
          ],
        );

      case 3:
        return _buildStepCard(
          title: 'Etapa 4: Telemetria & Sensores M0Robot (Reversa Oficial)',
          instruction:
              'A controladora do M0Robot responde à leitura dos blocos 0xC2 (Velocidade, Tensão, Corrente, Farol) '
              'e 0x1A (Status, Odômetros). Teste as consultas abaixo e observe a resposta no painel.',
          actions: [
            Wrap(
              spacing: 8,
              runSpacing: 8,
              children: [
                ElevatedButton.icon(
                  icon: const Icon(Icons.speed_rounded, size: 18),
                  label: const Text('Query Telemetria C2 (44B)'),
                  style: ElevatedButton.styleFrom(backgroundColor: AppTheme.electricCyan, foregroundColor: Colors.black),
                  onPressed: () => _sendPresetPacket(
                    PacketBuilder.queryTelemetryC2(destination: ScooterConstants.addrEsc),
                    'Query C2 (0x20)',
                  ),
                ),
                ElevatedButton.icon(
                  icon: const Icon(Icons.info_rounded, size: 18),
                  label: const Text('Query Status 1A (52B)'),
                  onPressed: () => _sendPresetPacket(
                    PacketBuilder.queryStatus1A(destination: ScooterConstants.addrEsc),
                    'Query 1A (0x20)',
                  ),
                ),
                OutlinedButton.icon(
                  icon: const Icon(Icons.battery_charging_full_rounded, size: 18),
                  label: const Text('Query Secundária EA'),
                  onPressed: () => _sendPresetPacket(
                    PacketBuilder.querySecondaryEA(destination: ScooterConstants.addrEsc),
                    'Query EA (0x20)',
                  ),
                ),
                OutlinedButton.icon(
                  icon: const Icon(Icons.dashboard_customize_rounded, size: 18),
                  label: const Text('Query Painel 14 (MAC)'),
                  onPressed: () => _sendPresetPacket(
                    PacketBuilder.queryDash14(destination: ScooterConstants.addrBle),
                    'Query 14 (0x21)',
                  ),
                ),
              ],
            ),
          ],
        );

      case 4:
      default:
        return _buildStepCard(
          title: 'Etapa 5: Auto-Sonda Universal (Brute-Force Ping)',
          instruction:
              'Este assistente dispara leituras sequenciais em todos os endereços conhecidos (0x06, 0x20, 0x21, 0x22) '
              'e registradores fundamentais (0x1A, 0x10, 0xB0) para descobrir automaticamente com qual combinação o patinete responde.',
          actions: [
            ElevatedButton.icon(
              icon: Icon(_isProbing ? Icons.hourglass_top_rounded : Icons.radar_rounded),
              label: Text(_isProbing ? 'Executando Sonda...' : 'Iniciar Auto-Sonda'),
              style: ElevatedButton.styleFrom(
                backgroundColor: AppTheme.electricCyan,
                foregroundColor: Colors.black,
                padding: const EdgeInsets.symmetric(horizontal: 20, vertical: 12),
              ),
              onPressed: _isProbing ? () => setState(() => _isProbing = false) : _runAutoProbe,
            ),
          ],
        );
    }
  }

  Widget _buildStepCard({
    required String title,
    required String instruction,
    required List<Widget> actions,
  }) {
    return Card(
      color: AppTheme.surface,
      shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(16)),
      child: Padding(
        padding: const EdgeInsets.all(16.0),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Text(title, style: const TextStyle(fontSize: 15, fontWeight: FontWeight.bold, color: AppTheme.electricCyan)),
            const SizedBox(height: 10),
            Text(
              instruction,
              style: const TextStyle(fontSize: 12, height: 1.5, color: AppTheme.textSecondary),
            ),
            const SizedBox(height: 16),
            ...actions,
          ],
        ),
      ),
    );
  }

  // --------------------------------------------------------------------------
  // ABA 2: SNIFFER / TERMINAL BRUTO
  // --------------------------------------------------------------------------

  Widget _buildSnifferTerminalTab(List<BleTrafficLog> filteredLogs) {
    return Column(
      children: [
        Container(
          padding: const EdgeInsets.symmetric(horizontal: 12, vertical: 6),
          color: AppTheme.surface,
          child: Row(
            children: [
              FilterChip(
                label: const Text('RX', style: TextStyle(fontSize: 11)),
                selected: _filterRxOnly,
                onSelected: (v) {
                  setState(() {
                    _filterRxOnly = v;
                    if (v) _filterTxOnly = false;
                  });
                },
              ),
              const SizedBox(width: 6),
              FilterChip(
                label: const Text('TX', style: TextStyle(fontSize: 11)),
                selected: _filterTxOnly,
                onSelected: (v) {
                  setState(() {
                    _filterTxOnly = v;
                    if (v) _filterRxOnly = false;
                  });
                },
              ),
              const Spacer(),
              ChoiceChip(
                label: Text(
                  _writeWithoutResponse ? 'NoResponse' : 'WithResponse',
                  style: const TextStyle(fontSize: 10),
                ),
                selected: true,
                onSelected: (_) {
                  setState(() {
                    _writeWithoutResponse = !_writeWithoutResponse;
                  });
                },
              ),
              const SizedBox(width: 6),
              IconButton(
                icon: Icon(
                  _autoScroll ? Icons.vertical_align_bottom_rounded : Icons.pause_rounded,
                  size: 20,
                  color: _autoScroll ? AppTheme.electricCyan : AppTheme.textMuted,
                ),
                tooltip: 'Auto-scroll',
                onPressed: () => setState(() => _autoScroll = !_autoScroll),
              ),
            ],
          ),
        ),

        Expanded(
          child: filteredLogs.isEmpty
              ? Center(
                  child: Column(
                    mainAxisSize: MainAxisSize.min,
                    children: [
                      Icon(Icons.terminal_rounded, size: 48, color: AppTheme.textMuted.withValues(alpha: 0.5)),
                      const SizedBox(height: 12),
                      const Text(
                        'Nenhum tráfego registrado ainda.',
                        style: TextStyle(color: AppTheme.textMuted, fontSize: 13),
                      ),
                      const SizedBox(height: 6),
                      const Text(
                        'Conecte ao patinete e realize ações para ver os pacotes.',
                        style: TextStyle(color: AppTheme.textMuted, fontSize: 11),
                      ),
                    ],
                  ),
                )
              : ListView.builder(
                  controller: _scrollController,
                  itemCount: filteredLogs.length,
                  itemBuilder: (ctx, index) {
                    final log = filteredLogs[index];
                    return _buildLogItem(log);
                  },
                ),
        ),

        _buildHexInjectorPanel(),
      ],
    );
  }

  Widget _buildLogItem(BleTrafficLog log) {
    final isRx = log.direction == TrafficDirection.rx;
    final color = isRx ? AppTheme.warningAmber : AppTheme.electricCyan;

    return Container(
      padding: const EdgeInsets.symmetric(horizontal: 12, vertical: 8),
      decoration: BoxDecoration(
        border: Border(bottom: BorderSide(color: AppTheme.cardBorder.withValues(alpha: 0.3))),
      ),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Row(
            children: [
              Container(
                padding: const EdgeInsets.symmetric(horizontal: 6, vertical: 2),
                decoration: BoxDecoration(
                  color: color.withValues(alpha: 0.15),
                  borderRadius: BorderRadius.circular(4),
                  border: Border.all(color: color.withValues(alpha: 0.5)),
                ),
                child: Text(
                  isRx ? 'RX (Scooter)' : 'TX (App)',
                  style: TextStyle(color: color, fontSize: 10, fontWeight: FontWeight.bold),
                ),
              ),
              const SizedBox(width: 8),
              Text(
                '${log.timestamp.hour.toString().padLeft(2, '0')}:${log.timestamp.minute.toString().padLeft(2, '0')}:${log.timestamp.second.toString().padLeft(2, '0')}.${log.timestamp.millisecond.toString().padLeft(3, '0')}',
                style: const TextStyle(fontSize: 10, color: AppTheme.textMuted),
              ),
              const SizedBox(width: 8),
              Text('${log.bytes.length}B', style: const TextStyle(fontSize: 10, color: AppTheme.textMuted)),
              if (log.taggedFunction != null) ...[
                const SizedBox(width: 8),
                Container(
                  padding: const EdgeInsets.symmetric(horizontal: 6, vertical: 2),
                  decoration: BoxDecoration(
                    color: AppTheme.neonGreen.withValues(alpha: 0.2),
                    borderRadius: BorderRadius.circular(4),
                  ),
                  child: Text(
                    log.taggedFunction!,
                    style: const TextStyle(color: AppTheme.neonGreen, fontSize: 10, fontWeight: FontWeight.bold),
                  ),
                ),
              ],
              const Spacer(),
              InkWell(
                onTap: () => _tagPacket(log),
                child: const Padding(
                  padding: EdgeInsets.symmetric(horizontal: 6, vertical: 2),
                  child: Icon(Icons.bookmark_add_outlined, size: 16, color: AppTheme.textMuted),
                ),
              ),
              InkWell(
                onTap: () {
                  Clipboard.setData(ClipboardData(text: log.hexString));
                  ScaffoldMessenger.of(context).showSnackBar(
                    const SnackBar(content: Text('Bytes HEX copiados!'), duration: Duration(seconds: 1)),
                  );
                },
                child: const Padding(
                  padding: EdgeInsets.symmetric(horizontal: 6, vertical: 2),
                  child: Icon(Icons.copy_rounded, size: 16, color: AppTheme.textMuted),
                ),
              ),
            ],
          ),
          const SizedBox(height: 4),
          SelectableText(
            log.hexString,
            style: TextStyle(
              fontFamily: 'monospace',
              fontSize: 12,
              fontWeight: FontWeight.w700,
              color: isRx ? Colors.white : AppTheme.electricCyan,
            ),
          ),
          Text(
            log.parsedSummary,
            style: const TextStyle(fontSize: 11, color: AppTheme.textMuted),
          ),
        ],
      ),
    );
  }

  Widget _buildHexInjectorPanel() {
    return Container(
      padding: const EdgeInsets.all(12),
      decoration: BoxDecoration(
        color: AppTheme.surface,
        boxShadow: [
          BoxShadow(
            color: Colors.black.withValues(alpha: 0.4),
            blurRadius: 8,
            offset: const Offset(0, -2),
          ),
        ],
      ),
      child: Column(
        mainAxisSize: MainAxisSize.min,
        children: [
          Row(
            children: [
              Expanded(
                child: TextField(
                  controller: _hexInputController,
                  style: const TextStyle(fontFamily: 'monospace', fontSize: 13),
                  decoration: InputDecoration(
                    hintText: 'Digite bytes em HEX (ex: 55 AA 03 06 01 B0 1C)',
                    hintStyle: const TextStyle(fontSize: 11, color: AppTheme.textMuted),
                    isDense: true,
                    contentPadding: const EdgeInsets.symmetric(horizontal: 12, vertical: 10),
                    border: OutlineInputBorder(borderRadius: BorderRadius.circular(8)),
                  ),
                ),
              ),
              const SizedBox(width: 8),
              ElevatedButton.icon(
                icon: const Icon(Icons.send_rounded, size: 16),
                label: const Text('Enviar'),
                style: ElevatedButton.styleFrom(
                  backgroundColor: AppTheme.electricCyan,
                  foregroundColor: Colors.black,
                  padding: const EdgeInsets.symmetric(horizontal: 14, vertical: 12),
                ),
                onPressed: _sendCustomHex,
              ),
            ],
          ),
          const SizedBox(height: 6),
          Row(
            children: [
              Checkbox(
                value: _autoCalculateChecksum,
                visualDensity: VisualDensity.compact,
                onChanged: (v) => setState(() => _autoCalculateChecksum = v ?? true),
              ),
              const Text('Auto-Calcular Checksum (XOR 0xFFFF)', style: TextStyle(fontSize: 11)),
              const Spacer(),
              PopupMenuButton<String>(
                icon: const Icon(Icons.flash_on_rounded, size: 18, color: AppTheme.warningAmber),
                tooltip: 'Comandos Prontos',
                onSelected: (val) {
                  _hexInputController.text = val;
                },
                itemBuilder: (_) => [
                  const PopupMenuItem(value: '55 AA 04 20 03 D3 80 00 85 FE', child: Text('Farol ON (Reg 0xD3)')),
                  const PopupMenuItem(value: '55 AA 04 20 03 D3 00 00 05 FF', child: Text('Farol OFF (Reg 0xD3)')),
                  const PopupMenuItem(value: '55 AA 04 20 03 70 01 00 67 FF', child: Text('Travar Motor (Reg 0x70)')),
                  const PopupMenuItem(value: '55 AA 04 20 03 70 00 00 68 FF', child: Text('Destravar Motor (Reg 0x70)')),
                  const PopupMenuItem(value: '55 AA 04 20 03 7E 00 00 5A FF', child: Text('Modo Eco (Reg 0x7E)')),
                  const PopupMenuItem(value: '55 AA 04 20 03 7E 01 00 59 FF', child: Text('Modo Drive (Reg 0x7E)')),
                  const PopupMenuItem(value: '55 AA 04 20 03 7E 02 00 58 FF', child: Text('Modo Sport (Reg 0x7E)')),
                  const PopupMenuItem(value: '55 AA 04 20 03 7C 01 00 5B FF', child: Text('Piloto Automático ON (0x7C)')),
                  const PopupMenuItem(value: '55 AA 04 20 03 7C 00 00 5C FF', child: Text('Piloto Automático OFF (0x7C)')),
                  const PopupMenuItem(value: '55 AA 04 20 03 7D 01 00 5A FF', child: Text('Zero-Start ON (0x7D)')),
                  const PopupMenuItem(value: '55 AA 04 20 03 7D 00 00 5B FF', child: Text('Kick-Start ON (0x7D)')),
                  const PopupMenuItem(value: '55 AA 03 20 01 1A 34 8D FF', child: Text('Query Status/Velocidade (0x1A)')),
                  const PopupMenuItem(value: '55 AA 03 20 01 6C 34 3B FF', child: Text('Query Configs/Trava (0x6C)')),
                  const PopupMenuItem(value: '55 AA 03 20 01 C2 2C EC FE', child: Text('Query Telemetria/Luz (0xC2)')),
                ],
              ),
            ],
          ),
        ],
      ),
    );
  }
}
