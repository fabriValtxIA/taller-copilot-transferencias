# ¿Tu repo está listo para agentes?

Checklist para preparar cualquier repositorio (Java, .NET, Python, JS…) para trabajar con GitHub Copilot en modo Agent.
Marca solo lo que puedas **demostrar** abriendo el repo, no lo que "se sabe" en el equipo.

## Mínimo viable (empieza por aquí)

- [ ] Existe `AGENTS.md` (o `.github/copilot-instructions.md`) en la raíz, con menos de 40 líneas.
- [ ] Ese archivo dice cómo compilar y probar, con los comandos exactos.
- [ ] Ese archivo lista las normas no negociables del equipo (seguridad, datos sensibles, dinero, auditoría…).
- [ ] Un solo comando corre todas las pruebas y termina en verde en una máquina limpia.
- [ ] Las versiones del lenguaje y de las dependencias están fijadas en el repo.

## 1. Instrucciones: que el agente conozca el proyecto y las normas

- [ ] `AGENTS.md` explica el propósito del sistema en 1–2 líneas, el stack con versiones y la estructura de carpetas relevante.
- [ ] Cada norma es verificable: dice qué usar ("usa `Enmascarar.cuenta(..)`"), no solo qué evitar ("cuidado con los logs").
- [ ] Las reglas que aplican solo a una parte del código (pruebas, migraciones, front) están en `.github/instructions/*.instructions.md` con su `applyTo`.
- [ ] Los procedimientos repetibles (agregar una regla de negocio, un endpoint, una migración) están como skill en `.github/skills/<nombre>/SKILL.md`, y el `name` coincide con la carpeta.
- [ ] Preguntaste en modo Ask "¿Qué normas debes seguir en este repo?" y Copilot las citó correctamente.
- [ ] Nada en el repo contradice las instrucciones: ni ejemplos viejos ni código que muestre lo prohibido sin un plan para corregirlo.

## 2. Herramientas: acceso justo, con mínimo privilegio

- [ ] Los roles que no deben modificar código (revisor, analista, planificador) son agentes en `.github/agents/*.agent.md` con `tools` restringidas, por ejemplo `['read', 'search']`.
- [ ] Probaste que el agente revisor **no puede** editar archivos: le pediste un cambio y se negó.
- [ ] Cada servidor MCP configurado (`.vscode/mcp.json` o `.mcp.json`) tiene un propósito claro y expone solo las herramientas necesarias.
- [ ] Ningún archivo de configuración del agente contiene secretos: se usan variables de entorno o `inputs`.
- [ ] Los servidores MCP apuntan a entornos de desarrollo o datos sintéticos, nunca a producción.

## 3. Entorno: que corra igual en todas las máquinas

- [ ] La versión del lenguaje y del runtime está fijada y validada (enforcer, `.tool-versions`, `global.json`, `.nvmrc`, etc.).
- [ ] La herramienta de build va dentro del repo (wrapper: `mvnw`, `gradlew`) o su versión está documentada.
- [ ] Las dependencias tienen versión fija o lockfile (`package-lock.json`, `poetry.lock`, BOM de Maven…).
- [ ] El proyecto compila y prueba sin servicios externos: base en memoria, contenedores o mocks.
- [ ] Los datos de prueba son sintéticos y viven en el repo.
- [ ] El README lista los requisitos previos y funciona con un clon limpio.

## 4. Estado: que el trabajo sobreviva entre sesiones

- [ ] Las tareas de más de una sesión tienen su plan escrito en el repo (por ejemplo `docs/planes/<tarea>.md`), con decisiones y pasos.
- [ ] Existe un `PROGRESS.md` (o equivalente) con tres listas: Hecho / En curso / Bloqueado.
- [ ] `AGENTS.md` le pide al agente leer el progreso al empezar y actualizarlo al terminar cada tarea.
- [ ] Probaste abrir un chat nuevo con "Lee PROGRESS.md y continúa", y el agente retomó sin que le explicaras nada.

## 5. Feedback: que el agente verifique su propio trabajo

- [ ] `AGENTS.md` tiene una sección "Verificación" con los comandos exactos: pruebas, análisis estático y build.
- [ ] Dice explícitamente: "ninguna tarea está terminada si algún comando falla".
- [ ] Las normas más importantes tienen una prueba automática que falla si se violan (por ejemplo, una que busque tipos prohibidos).
- [ ] El compilador o linter trata las advertencias como errores, o hay un umbral acordado.
- [ ] Cada regla de negocio tiene al menos una prueba que la acepta y otra que la rechaza.
- [ ] Antes de integrar cambios hechos por el agente, pasa la revisión del agente revisor y la de una persona.
