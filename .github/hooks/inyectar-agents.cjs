// Hook sessionStart (vista previa): inyecta AGENTS.md en el contexto al iniciar la sesion.
// EXPERIMENTAL: la documentacion no publica el formato de salida de sessionStart, asi que
// se devuelve additionalContext en el nivel superior y tambien en hookSpecificOutput.
// Cada ejecucion deja una linea en target/hook-sessionstart.log para saber si el hook corrio.
const fs = require('node:fs');
const path = require('node:path');

const MARCADOR = 'HOOK-SESSIONSTART-ACTIVO';

let evento = '';
try {
  evento = fs.readFileSync(0, 'utf8');
} catch {
  // sin stdin: seguimos
}

const raiz = process.cwd();
let agents = '';
try {
  agents = fs.readFileSync(path.join(raiz, 'AGENTS.md'), 'utf8');
} catch {
  agents = '(AGENTS.md no encontrado)';
}

try {
  fs.mkdirSync(path.join(raiz, 'target'), { recursive: true });
  fs.appendFileSync(
    path.join(raiz, 'target', 'hook-sessionstart.log'),
    `${new Date().toISOString()} cwd=${raiz} evento=${evento.slice(0, 500).replace(/\s+/g, ' ')}\n`,
  );
} catch {
  // el log es solo diagnostico
}

const contexto = `[${MARCADOR}] Normas del repo (AGENTS.md), de lectura obligatoria antes de editar:\n\n${agents}`;

process.stdout.write(JSON.stringify({
  additionalContext: contexto,
  hookSpecificOutput: { hookEventName: 'SessionStart', additionalContext: contexto },
}));
