/**
 * Teste de Integração de Ponta a Ponta:
 * Simula o ciclo completo de comunicação entre o Serviço BLE e o Patinete M0Robot
 */

const { EventEmitter } = require('events');

// --- 1. Protocolo e Construtor ---
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

function toHex(b) {
  return Array.from(b).map(x => x.toString(16).padStart(2, '0').toUpperCase()).join(' ');
}

// --- 2. Simulador de Hardware do Patinete ---
class SimulatedScooterHardware extends EventEmitter {
  constructor() {
    super();
    this.headlight = false;
    this.locked = false;
    this.speedMode = 1; // Drive
    this.speedKmh = 18.5;
    this.battery = 82;
    this.voltage = 38.1;
  }

  receivePacket(packet) {
    const cmd = packet[4];
    const reg = packet[5];

    if (cmd === 0x02) { // Write
      const val = packet[6];
      if (reg === 0x72) this.headlight = val === 1;
      if (reg === 0x70) this.locked = val === 1;
      if (reg === 0x7B) this.speedMode = val;
    }

    // Gerar resposta de telemetria
    this.emitTelemetry();
  }

  emitTelemetry() {
    const payload = Buffer.alloc(21);
    let p = 0;

    let status = 0;
    if (this.headlight) status |= 0x0001;
    if (this.locked) status |= 0x0002;
    status |= (this.speedMode << 3);

    payload.writeUInt16LE(status, p); p += 2;
    payload.writeUInt16LE(0, p); p += 2; // Error
    payload.writeInt16LE(this.locked ? 0 : Math.round(this.speedKmh * 1000), p); p += 2;
    payload.writeUInt8(this.battery, p); p += 1;
    payload.writeUInt16LE(Math.round(this.voltage * 1000), p); p += 2;
    payload.writeInt16LE(450, p); p += 2; // 4.5A
    payload.writeInt16LE(295, p); p += 2; // 29.5°C
    payload.writeUInt16LE(2800, p); p += 2; // 2.8 km
    payload.writeUInt32LE(158000, p); p += 4; // 158 km

    const frame = buildFrame(0x21, 0x65, 0xB0, payload);
    this.emit('data', frame);
  }
}

// --- 3. Execução do Teste de Integração ---
console.log('===============================================================');
console.log(' TESTE DE INTEGRAÇÃO DO SERVIÇO BLE (M0ROBOT)');
console.log('===============================================================\n');

const hardware = new SimulatedScooterHardware();

hardware.on('data', (frame) => {
  // Simula o callback do Flutter / ScooterBleService
  const data = frame.subarray(6, frame.length - 2);
  const status = data.readUInt16LE(0);
  const speed = data.readInt16LE(4) / 1000.0;
  const battery = data.readUInt8(6);
  const voltage = data.readUInt16LE(7) / 1000.0;
  const headlight = (status & 0x01) !== 0;
  const locked = (status & 0x02) !== 0;
  const mode = ['Eco', 'Drive', 'Sport'][(status >> 3) & 0x03];

  console.log(`[TELEMETRIA RECEBIDA] Farol: ${headlight ? 'ON' : 'OFF'} | Trava: ${locked ? 'SIM' : 'NÃO'} | Modo: ${mode} | Velocidade: ${speed.toFixed(1)} km/h | Bateria: ${battery}% (${voltage.toFixed(1)}V)`);
});

console.log('1. Conectando ao M0RobotDFE54C32BA...');
hardware.emitTelemetry();

console.log('\n2. Enviando Comando: Ligar Farol...');
const cmdFarol = buildFrame(0x20, 0x02, 0x72, [0x01, 0x00]);
console.log('   TX ->', toHex(cmdFarol));
hardware.receivePacket(cmdFarol);

console.log('\n3. Enviando Comando: Mudar para Modo SPORT...');
const cmdSport = buildFrame(0x20, 0x02, 0x7B, [0x02, 0x00]);
console.log('   TX ->', toHex(cmdSport));
hardware.receivePacket(cmdSport);

console.log('\n4. Enviando Comando: Ativar Trava Antifurto...');
const cmdTrava = buildFrame(0x20, 0x02, 0x70, [0x01, 0x00]);
console.log('   TX ->', toHex(cmdTrava));
hardware.receivePacket(cmdTrava);

console.log('\n===============================================================');
console.log(' >> FLUXO DE COMUNICAÇÃO COMPLETO EXECUTADO COM SUCESSO! <<');
console.log('===============================================================');
