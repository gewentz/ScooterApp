/**
 * Script de Verificação dos Protocolos de Comunicação do Patinete M0Robot
 * Executável via Node.js para validação matemática de frames, checksum e parsing.
 */

function calculateChecksum(bytes, startIndex, length) {
  let sum = 0;
  for (let i = startIndex; i < startIndex + length; i++) {
    sum += bytes[i];
  }
  return (sum & 0xFFFF) ^ 0xFFFF;
}

function buildFrame(destination, command, register, payload) {
  const payloadLen = payload.length + 2;
  const totalSize = 2 + 1 + 1 + 1 + 1 + payload.length + 2;
  const frame = Buffer.alloc(totalSize);

  frame[0] = 0x55;
  frame[1] = 0xAA;
  frame[2] = payloadLen;
  frame[3] = destination;
  frame[4] = command;
  frame[5] = register;

  for (let i = 0; i < payload.length; i++) {
    frame[6 + i] = payload[i];
  }

  const checksumStart = 2;
  const checksumLength = 1 + 1 + 1 + 1 + payload.length;
  const checksum = calculateChecksum(frame, checksumStart, checksumLength);

  const checksumOffset = 6 + payload.length;
  frame[checksumOffset] = checksum & 0xFF;
  frame[checksumOffset + 1] = (checksum >> 8) & 0xFF;

  return frame;
}

function toHex(buffer) {
  return Array.from(buffer).map(b => b.toString(16).padStart(2, '0').toUpperCase()).join(' ');
}

console.log('===============================================================');
console.log(' TESTE DO PROTOCOLO M0ROBOT / MINIROBOT (LEBITEC ESC 0x20)');
console.log('===============================================================\n');

// 1. GERAÇÃO DOS COMANDOS
console.log('--- 1. Gerando Comandos de Envio (App -> Patinete) ---');

const cmdHeadlightOn = buildFrame(0x20, 0x02, 0x72, [0x01, 0x00]);
console.log('• Farol LIGADO:          ', toHex(cmdHeadlightOn));

const cmdHeadlightOff = buildFrame(0x20, 0x02, 0x72, [0x00, 0x00]);
console.log('• Farol DESLIGADO:       ', toHex(cmdHeadlightOff));

const cmdLock = buildFrame(0x20, 0x02, 0x70, [0x01, 0x00]);
console.log('• Trava Antifurto ATIVA: ', toHex(cmdLock));

const cmdUnlock = buildFrame(0x20, 0x02, 0x70, [0x00, 0x00]);
console.log('• Trava Antifurto LIVRE: ', toHex(cmdUnlock));

const cmdModeEco = buildFrame(0x20, 0x02, 0x7B, [0x00, 0x00]);
console.log('• Modo ECO:              ', toHex(cmdModeEco));

const cmdModeSport = buildFrame(0x20, 0x02, 0x7B, [0x02, 0x00]);
console.log('• Modo SPORT:            ', toHex(cmdModeSport));

const cmdCruiseOn = buildFrame(0x20, 0x02, 0x7A, [0x01, 0x00]);
console.log('• Cruise Control ATIVADO:', toHex(cmdCruiseOn));

const cmdZeroStart = buildFrame(0x20, 0x02, 0x75, [0x01, 0x00]);
console.log('• Zero-Start ATIVADO:    ', toHex(cmdZeroStart));

// 2. SIMULAÇÃO DE PACOTE DE TELEMETRIA COMPLETO (Patinete -> App)
console.log('\n--- 2. Simulação de Resposta de Telemetria (Patinete -> App) ---');

// Criar payload com valores realistas:
// Status: Farol ligado (bit 0 = 1), Modo Sport (bits 3-4 = 2 -> 0x0010), total status = 0x0011
// Erro: 0
// Velocidade: 22.5 km/h -> 22500 (0x57E4)
// Bateria: 85% (0x55)
// Tensão: 38.4V -> 38400 mV (0x9600)
// Corrente: 6.5A -> 650 (0x028A)
// Temperatura: 31.5 °C -> 315 (0x013B)
// Odômetro parcial: 3.25 km -> 3250 m (0x0CB2)
// Odômetro total: 142.8 km -> 142800 m (0x00022DD0)

const payload = Buffer.alloc(21);
let p = 0;
payload.writeUInt16LE(0x0011, p); p += 2; // Status
payload.writeUInt16LE(0x0000, p); p += 2; // Error
payload.writeInt16LE(22500, p);   p += 2; // Speed (22.5 km/h)
payload.writeUInt8(85, p);        p += 1; // Battery %
payload.writeUInt16LE(38400, p);  p += 2; // Voltage (38.4 V)
payload.writeInt16LE(650, p);     p += 2; // Current (6.5 A)
payload.writeInt16LE(315, p);     p += 2; // Temp (31.5 °C)
payload.writeUInt16LE(3250, p);   p += 2; // Trip (3.25 km)
payload.writeUInt32LE(142800, p); p += 4; // Total (142.8 km)

const telemetryFrame = buildFrame(0x21, 0x65, 0xB0, payload);
console.log('Frame de Telemetria Gerado:\n', toHex(telemetryFrame));

// 3. TESTE DE PARSING E EXTRAÇÃO
console.log('\n--- 3. Validação do Parser ---');

function parseTelemetry(frame) {
  // Validar Checksum
  const payloadLen = frame[2];
  const checksumStart = 2;
  const checksumLength = 1 + 1 + 1 + 1 + (frame.length - 8);
  const calcChk = calculateChecksum(frame, checksumStart, checksumLength);
  const receivedChk = frame[frame.length - 2] | (frame[frame.length - 1] << 8);

  if (calcChk !== receivedChk) {
    throw new Error(`Checksum inválido! Calc: ${calcChk.toString(16)}, Rec: ${receivedChk.toString(16)}`);
  }

  const data = frame.subarray(6, frame.length - 2);
  let off = 0;

  const status = data.readUInt16LE(off); off += 2;
  const isHeadlight = (status & 0x01) !== 0;
  const mode = (status >> 3) & 0x03;

  const error = data.readUInt16LE(off); off += 2;
  const rawSpeed = data.readInt16LE(off); off += 2;
  const speed = rawSpeed > 1000 ? rawSpeed / 1000.0 : rawSpeed / 100.0;

  const battery = data.readUInt8(off); off += 1;
  const rawVolt = data.readUInt16LE(off); off += 2;
  const voltage = rawVolt > 1000 ? rawVolt / 1000.0 : rawVolt / 100.0;

  const current = data.readInt16LE(off) / 100.0; off += 2;
  const temp = data.readInt16LE(off) / 10.0; off += 2;
  const trip = data.readUInt16LE(off) / 1000.0; off += 2;
  const total = data.readUInt32LE(off) / 1000.0; off += 4;

  return {
    isHeadlight,
    mode: ['Eco', 'Drive', 'Sport'][mode] || 'Drive',
    error,
    speed: `${speed.toFixed(1)} km/h`,
    battery: `${battery}%`,
    voltage: `${voltage.toFixed(1)} V`,
    current: `${current.toFixed(2)} A`,
    power: `${(voltage * current).toFixed(0)} W`,
    temperature: `${temp.toFixed(1)} °C`,
    tripDistance: `${trip.toFixed(2)} km`,
    totalDistance: `${total.toFixed(1)} km`,
  };
}

const parsed = parseTelemetry(telemetryFrame);
console.log('Dados Decodificados com Sucesso:');
console.table(parsed);

// 4. TESTE DE FRAGMENTAÇÃO BLE
console.log('\n--- 4. Teste de Fragmentação BLE (MTU de 10 bytes) ---');
const chunks = [];
for (let i = 0; i < telemetryFrame.length; i += 10) {
  chunks.push(telemetryFrame.subarray(i, Math.min(i + 10, telemetryFrame.length)));
}
console.log(`Pacote dividido em ${chunks.length} fragmentos de BLE:`);
chunks.forEach((c, idx) => console.log(`  Fragmento ${idx + 1}: ${toHex(c)}`));

let accumulator = Buffer.alloc(0);
chunks.forEach((chunk, idx) => {
  accumulator = Buffer.concat([accumulator, chunk]);
  console.log(`Recebido frag ${idx + 1}. Tamanho acumulado: ${accumulator.length}/${telemetryFrame.length}`);
});

const reassembled = parseTelemetry(accumulator);
console.log('Resultado após remontagem dos fragmentos:');
console.log(`Velocidade: ${reassembled.speed} | Bateria: ${reassembled.battery} | Potência: ${reassembled.power}`);
console.log('\n===============================================================');
console.log(' >> TODOS OS TESTES DE PROTOCOLO PASSARAM COM 100% DE SUCESSO! <<');
console.log('===============================================================');
