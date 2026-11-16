// Hook preToolUse (vista previa): si el agente intenta `git commit`, corre las pruebas primero
// y niega la herramienta si fallan. Cualquier otra herramienta pasa sin decisión.
const fs = require('node:fs');
const { spawnSync } = require('node:child_process');

const evento = fs.readFileSync(0, 'utf8');
if (!/git\s+commit/.test(evento)) {
  process.stdout.write('{}');
  process.exit(0);
}

const windows = process.platform === 'win32';
const pruebas = spawnSync(windows ? 'mvnw.cmd' : './mvnw', ['-q', 'test'], { encoding: 'utf8', shell: windows });

if (pruebas.status === 0) {
  process.stdout.write('{}');
} else {
  const salida = `${pruebas.stdout ?? ''}${pruebas.stderr ?? ''}`.slice(-1500);
  process.stdout.write(JSON.stringify({
    permissionDecision: 'deny',
    permissionDecisionReason: `Pruebas en rojo: corrige antes de hacer commit.\n${salida}`,
  }));
}
