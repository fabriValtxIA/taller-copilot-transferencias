# Taller GitHub Copilot: arma el harness de tu repo

Guía del asistente. Vas a pedir la misma feature dos veces con el mismo prompt: primero en un repo sin configurar y después con el *harness* completo. La diferencia entre los dos resultados es lo que viniste a aprender.

**El harness tiene 5 partes:** Instrucciones · Herramientas · Entorno · Estado · Feedback.

---

## Requisitos previos

- JDK 21 (`java -version`).
- Git.
- VS Code con **GitHub Copilot** y **GitHub Copilot Chat**, con la sesión SSO iniciada. El modo **Agent** debe estar disponible.
- El repo `transferencias-taller` clonado. Comprueba que compila:
  ```bash
  ./mvnw verify          # Windows: mvnw.cmd verify
  ```
  La primera vez el wrapper descarga Maven. Hazlo **antes** del taller.
- Tiempos de referencia: la app tarda **~40 s** en arrancar y la suite de pruebas **~30 s**.

**Ramas del repo:** `main` es la línea base, sin harness. Las ramas `bloque-1` a `bloque-6` son **soluciones de referencia**: si te atrasas, cámbiate a la del bloque siguiente. `bloque-1` es **ilustrativa**, una muestra de lo que suele producir Copilot sin harness. Tu resultado real puede variar.

---

## Lista de control: las 4 normas no negociables

Márcala dos veces: al final del Bloque 1 (línea base) y al final del Bloque 6 (segunda vuelta).

| # | Norma | Cómo comprobarla | B1 línea base | B6 segunda vuelta |
|---|---|---|---|---|
| 1 | Datos sensibles enmascarados en logs | Ningún `log.*` imprime la cuenta o el documento completos | ☐ | ☐ |
| 2 | Dinero en `BigDecimal` | No hay `double`/`float` en montos, saldos ni límites; en SQL, `DECIMAL(15,2)` | ☐ | ☐ |
| 3 | Auditoría en toda operación | Toda transferencia, aceptada o rechazada, queda registrada | ☐ | ☐ |
| 4 | Una prueba por regla de negocio | La regla nueva tiene una prueba que acepta y otra que rechaza, y pasan | ☐ | ☐ |

---

## Bloque 1. Línea base (15 min) — sin harness

**Objetivo:** ver qué hace Copilot cuando el repo no le dice nada.

1. Crea tu rama **antes** de pedir nada:
   ```bash
   git switch main
   git switch -c linea-base
   ```
2. **Chat 1, modo Ask:**
   ```
   ¿Qué hace este servicio y qué riesgos ves en el código?
   ```
   Lee la respuesta y anota qué riesgos detecta.
3. **Abre un chat NUEVO** (botón `+`) y cambia a modo **Agent**:
   ```
   Agrega una regla que rechace las transferencias que superen el límite diario del cliente.
   ```
   Acepta los cambios.
4. Revisa el resultado con la **lista de control** (columna B1).
5. Guarda el resultado:
   ```bash
   git add -A
   git commit -m "Línea base: límite diario sin harness"
   ```

**Esperado:** el código funciona pero falla casi toda la lista de control: logs con la cuenta y el documento completos, `double`, sin auditoría y sin pruebas.

**Errores comunes**
- Hacer Ask y Agent en el mismo chat. El agente "recuerda" los riesgos del análisis y la línea base sale mejor de lo real. Usa siempre un chat nuevo.
- Olvidar el commit. Sin la rama `linea-base` no hay comparación en el Bloque 6.
- Tener instrucciones o skills personales activas (`~/.copilot/...`). Contaminan la línea base.

---

## Bloque 2. Instrucciones (20 min) — parte *Instrucciones*

**Objetivo:** que el agente conozca el proyecto y las normas sin que tú las repitas en cada prompt.

Trabaja sobre una rama nueva desde `main`:
```bash
git switch main
git switch -c harness
```

### 2.1 `AGENTS.md` en la raíz (menos de 40 líneas)

```markdown
# Servicio de transferencias

Servicio REST que transfiere dinero entre cuentas de clientes del banco.
Todos los datos del repo son sintéticos.

## Stack
- Java 21, Spring Boot 3.5.9, Maven 3.9 (usar siempre el wrapper `./mvnw`).
- Persistencia con `JdbcTemplate` sobre H2 en memoria (`schema.sql`, `data.sql`).
- Paquete único: `com.taller.transferencias`.

## Compilar y probar
- `./mvnw verify` compila y corre todas las pruebas.
- `./mvnw spring-boot:run` levanta el servicio en el puerto 8080.

## Normas no negociables
1. **Datos sensibles enmascarados en logs.** Nunca registrar número de cuenta
   ni documento de identidad completos. Usar `Enmascarar.cuenta(..)` y
   `Enmascarar.documento(..)` (solo últimos 4 dígitos visibles).
2. **Dinero en `BigDecimal`.** Nunca `double` ni `float` para montos, saldos o
   límites. Escala 2 y `RoundingMode.HALF_EVEN`. En SQL usar `DECIMAL(15,2)`.
3. **Auditoría en toda operación.** Cada transferencia, aceptada o rechazada,
   se registra con `AuditoriaService.registrar(..)` (tabla `auditoria`).
4. **Una prueba por cada regla de negocio.** Cada validación nueva lleva al
   menos una prueba que la acepte y otra que la rechace.

Si una norma choca con el código existente, corrige el código existente.
Para agregar una regla de negocio, sigue la skill `regla-de-negocio`.
```

### 2.2 Regla por ruta: `.github/instructions/pruebas.instructions.md`

Solo se aplica cuando el agente toca archivos de `src/test/`.

```markdown
---
applyTo: 'src/test/**/*.java'
description: Convenciones para las pruebas del servicio de transferencias
---
# Convenciones de pruebas

- JUnit 5 + AssertJ (vienen con `spring-boot-starter-test`). No agregar otras librerías.
- Una clase de prueba por clase probada: `<Clase>Test`, en el mismo paquete.
- Nombre de método en español y en snake_case que diga la regla:
  `rechaza_transferencia_si_supera_limite_diario()`.
- Estructura en tres bloques con comentarios `// dado`, `// cuando`, `// entonces`.
- Pruebas de servicio con `@SpringBootTest` y los datos de `data.sql`. Si la prueba
  modifica datos, anotarla con `@Transactional` para que se reviertan.
- Montos siempre con `new BigDecimal("150.75")`, nunca con literales `double`.
- Para reglas que rechazan: `assertThatThrownBy(..).isInstanceOf(TransferenciaRechazadaException.class)`
  y verificar también que quedó el registro de auditoría con resultado `RECHAZADA`.
```

### 2.3 Skill: `.github/skills/regla-de-negocio/SKILL.md`

El `name` debe coincidir **exactamente** con el nombre de la carpeta. Si no coincide, la skill no se carga y no hay ningún aviso.

````markdown
---
name: regla-de-negocio
description: Procedimiento para agregar o modificar una regla de negocio (validación) en el servicio de transferencias, por ejemplo límites, montos máximos, horarios o bloqueos. Úsala siempre que se pida rechazar, limitar o validar una transferencia.
---
# Agregar una regla de negocio

Sigue estos pasos en orden. No des la tarea por terminada sin el paso 5.

## 1. Dónde va la validación
- En `TransferenciaService`, como método privado `validar<Regla>(..)`.
- Se llama dentro de `transferir(..)`, después de `validarSaldo(..)` y antes de mover dinero.
- Si falla, lanza `TransferenciaRechazadaException` con un motivo legible para el cliente
  (sin datos sensibles en el mensaje).
- Si la regla necesita datos nuevos, agrega la columna en `schema.sql` como `DECIMAL(15,2)`
  para montos y la consulta en `BancoRepository`, leyendo con `rs.getBigDecimal(..)`.

## 2. Montos
- Todo monto, saldo, límite o acumulado es `BigDecimal`. Compara con `compareTo`, nunca con `equals`.
- Si encuentras `double` en el camino que tocas, conviértelo a `BigDecimal` en el mismo cambio.

## 3. Auditoría
- Registra el resultado con `AuditoriaService.registrar(origen, destino, monto, resultado, motivo)`
  tanto si se acepta (`ACEPTADA`) como si se rechaza (`RECHAZADA`).
- Si `AuditoriaService` no existe todavía, créalo junto con la tabla `auditoria`
  (id, fecha, cuenta_origen, cuenta_destino, monto `DECIMAL(15,2)`, resultado, motivo).
  El rechazo debe auditarse aunque la transacción se revierta
  (`@Transactional(propagation = REQUIRES_NEW)` en `registrar`).

## 4. Logs
- Si la regla registra algo en el log, usa `Enmascarar.cuenta(..)` y `Enmascarar.documento(..)`.
  Si `Enmascarar` no existe, créalo: deja visibles solo los últimos 4 caracteres.

## 5. Pruebas (plantilla)
En `src/test/java/com/taller/transferencias/TransferenciaServiceTest.java`:

```java
@Test
void rechaza_transferencia_si_<regla>() {
    // dado
    String origen = "0011-0457-0200987654";   // cliente con límite 1500.00
    // cuando / entonces
    assertThatThrownBy(() -> service.transferir(origen, DESTINO, new BigDecimal("1600.00")))
            .isInstanceOf(TransferenciaRechazadaException.class)
            .hasMessageContaining("<motivo>");
    // y quedó auditada como RECHAZADA
}

@Test
void acepta_transferencia_si_<regla_se_cumple>() { ... }
```

Corre `./mvnw verify` y no termines hasta que pase.
````

### 2.4 Comprobar

En un chat nuevo, modo **Ask**:
```
¿Qué normas debes seguir en este repo?
```
- Abre **References** debajo de la respuesta: debe aparecer `AGENTS.md`. Esto confirma que se cargó, no que vaya a cumplirlo.
- La skill también se invoca a mano con `/regla-de-negocio` en el chat.

```bash
git add -A && git commit -m "Harness: instrucciones"
```

**Esperado:** Copilot cita las 4 normas y carga la skill cuando le pides una regla nueva.

**Errores comunes**
- `AGENTS.md` largo, con historia del proyecto. Mantenlo en menos de 40 líneas: lo que no ayuda a decidir, sobra.
- Skill en `.github/skill/` (singular) o con `name` distinto de la carpeta.
- `applyTo` con la ruta mal escrita: la regla nunca aplica y no hay error.

---

## Bloque 3. Agente revisor (15 min) — parte *Herramientas*

**Objetivo:** un agente con herramientas mínimas (solo lectura) que revisa el cumplimiento.

1. Crea `.github/agents/revisor-cumplimiento.agent.md`:
   ```markdown
   ---
   name: revisor-cumplimiento
   description: Revisa código o un diff contra las 4 normas no negociables del banco. Solo lee; no edita archivos ni ejecuta comandos.
   tools: ['read', 'search']
   ---
   Eres un revisor de cumplimiento para código bancario. **No puedes editar archivos ni ejecutar
   comandos**: si te piden corregir algo, responde que tu rol es solo revisar y sugiere el cambio.

   Qué revisar: lo que el usuario indique (un archivo `.diff`, una carpeta o archivos concretos).
   Si no indica nada, revisa `src/main/java`, `src/main/resources` y `src/test/java` completos.

   Evalúa cada una de estas normas (definidas en `AGENTS.md`):
   1. **Logs enmascarados**: ninguna llamada a `log.*` imprime número de cuenta o documento completo.
   2. **Dinero en BigDecimal**: no hay `double`, `Double`, `float` ni columnas `DOUBLE` para montos, saldos o límites.
   3. **Auditoría**: toda transferencia, aceptada o rechazada, pasa por `AuditoriaService.registrar(..)`.
   4. **Pruebas**: cada regla de negocio (`validar*` en `TransferenciaService`) tiene al menos una prueba que acepta y otra que rechaza.

   Responde **solo** con esta tabla, una fila por hallazgo (o una fila "Cumple" por norma si no hay hallazgos):

   | Norma | Cumple / No cumple | Archivo | Línea | Evidencia |
   |---|---|---|---|---|

   Termina con una línea: `Veredicto: APTO` si las 4 normas cumplen; si no, `Veredicto: NO APTO (n hallazgos)`.
   No inventes líneas: cita solo lo que leíste.
   ```
2. El revisor **no puede ejecutar git**, así que prepárale el diff tú:
   ```bash
   git diff main linea-base > linea-base.diff
   ```
3. En el chat, elige **revisor-cumplimiento** en el selector de agentes:
   ```
   Revisa los cambios de linea-base.diff
   ```
4. Prueba que no puede editar:
   ```
   Corrige el primer hallazgo
   ```

**Esperado:** una tabla con los defectos del Bloque 1 y `Veredicto: NO APTO`. Ante "corrige", responde que solo revisa y no modifica archivos.

**Errores comunes**
- Pedirle "revisa la rama linea-base": no puede ejecutar `git`. Dale el `.diff`.
- No ver el agente en el selector: revisa que el archivo termine en `.agent.md` y esté en `.github/agents/`.
- Dejar `linea-base.diff` en el commit. Bórralo o agrégalo a `.gitignore`.

*(Demo del instructor: servidor MCP de base de datos para que el agente consulte el esquema real de las tablas.)*

```bash
git add .github && git commit -m "Harness: agente revisor"
```

---

## Bloque 4. Verificación (20 min) — parte *Feedback*

**Objetivo:** que el agente verifique su trabajo y se corrija solo.

1. Agrega a `AGENTS.md`, antes de la última línea:
   ```markdown
   ## Verificación
   Ejecuta estos comandos antes de dar por terminada cualquier tarea. Si alguno
   falla, lee el error, corrige y vuelve a ejecutar. **Ninguna tarea está
   terminada con un comando en rojo.**
   1. `./mvnw -q compile` (compila con `-Xlint:all -Werror`: cero advertencias).
   2. `./mvnw -q test` (incluye `NormasDelRepoTest`, que rechaza `double`).
   3. `./mvnw -q verify` (build completo).
   ```
2. Trae el compilador estricto y la prueba de normas desde la rama de referencia:
   ```bash
   git checkout bloque-4 -- pom.xml src/test/java/com/taller/transferencias/NormasDelRepoTest.java
   ```
   `NormasDelRepoTest` recorre `src/main` y falla listando cada `double`/`DOUBLE` con su archivo y línea. Ese listado es el feedback que lee el agente.
3. Modo **Agent**:
   ```
   Corrige el manejo de montos para que use decimales y verifica.
   ```
4. Observa el ciclo: editar → correr pruebas → leer el error → corregir. Acepta las ejecuciones de terminal que te pida.

**Esperado:** el agente entrega con todas las pruebas en verde, por su cuenta.

**Errores comunes**
- El agente termina sin correr nada: revisa que la sección "Verificación" esté en `AGENTS.md` y que estés en modo Agent.
- Denegar la ejecución de comandos de terminal: sin ejecución no hay feedback.
- Literales como `new BigDecimal(0.1)`: arrastran el error de `double`. Lo correcto es `new BigDecimal("0.10")`.

```bash
git add -A && git commit -m "Harness: verificación y montos en BigDecimal"
```

---

## Bloque 5. Continuidad (10 min) — parte *Estado*

**Objetivo:** que el trabajo sobreviva al cerrar el chat.

1. Agrega a `AGENTS.md`:
   ```markdown
   ## Estado entre sesiones
   - Al empezar, lee `PROGRESS.md` y el plan que indique (`docs/planes/`).
   - Al terminar cada tarea, actualiza `PROGRESS.md` (Hecho / En curso / Bloqueado).
   ```
2. Modo **Plan**:
   ```
   Planifica la regla que rechaza las transferencias que superen el límite diario del cliente, con auditoría y pruebas.
   ```
3. Cambia a modo **Agent** (el modo Plan puede no tener permiso para escribir archivos):
   ```
   Guarda este plan en docs/planes/limite-diario.md y crea PROGRESS.md con tres listas: Hecho, En curso y Bloqueado. Ejecuta solo la primera tarea.
   ```
4. **Cierra el chat.** Abre uno nuevo en modo Agent:
   ```
   Lee PROGRESS.md y continúa con la siguiente tarea.
   ```

**Esperado:** retoma el trabajo sin que nadie le explique el contexto y actualiza `PROGRESS.md`.

**Errores comunes**
- Plan sin tareas numeradas: el agente no sabe cuál es "la siguiente". Pide tareas numeradas.
- Seguir en el mismo chat: así no se prueba nada. El chat tiene que ser nuevo.

Referencia: `git show bloque-5:docs/planes/limite-diario.md` y `git show bloque-5:PROGRESS.md`.

---

## Bloque 6. Segunda vuelta (18 min) — todas las partes

**Objetivo:** repetir **exactamente** el prompt del Bloque 1, ahora con el harness, y comparar.

1. Parte del código original (`main`) y copia solo el harness. Antes, deja limpio el árbol de trabajo: los archivos sin commitear viajan entre ramas. Si `linea-base.diff`, `PROGRESS.md` o `docs/planes/` pasan a la nueva rama, contaminan la comparación.
   ```bash
   git add -A && git commit -m "Harness: fin del bloque 5"   # en tu rama harness
   rm -f linea-base.diff
   git status            # debe decir "nothing to commit, working tree clean"
   git switch main
   git switch -c segunda-vuelta
   git checkout bloque-5 -- AGENTS.md .github .vscode .mcp.json demo-mcp pom.xml .gitignore src/test/java/com/taller/transferencias/NormasDelRepoTest.java
   git commit -m "Harness completo sobre el código original"
   ```
   *(Si prefieres usar tu propio harness: `git checkout harness -- AGENTS.md .github pom.xml src/test/java/com/taller/transferencias/NormasDelRepoTest.java`.)*
2. Chat **nuevo**, modo **Agent**, el mismo prompt del Bloque 1:
   ```
   Agrega una regla que rechace las transferencias que superen el límite diario del cliente.
   ```
3. Haz el commit y pásalo por el revisor:
   ```bash
   git add -A && git commit -m "Segunda vuelta: límite diario con harness"
   git diff main segunda-vuelta > segunda-vuelta.diff
   ```
   Con **revisor-cumplimiento**:
   ```
   Revisa los cambios de segunda-vuelta.diff
   ```
4. Completa la columna B6 de la lista de control y esta tabla:

| | `linea-base` (sin harness) | `segunda-vuelta` (con harness) |
|---|---|---|
| Logs enmascarados | | |
| Dinero en `BigDecimal` | | |
| Auditoría (aceptada y rechazada) | | |
| Pruebas de la regla (acepta / rechaza) | | |
| `./mvnw verify` en verde | | |
| Veredicto del revisor | | |
| Archivos tocados / líneas | `git diff --stat main linea-base` | `git diff --stat main segunda-vuelta` |

**Esperado:** cumple las 4 normas e incluye pruebas que pasan. Referencia: rama `bloque-6`.

**Errores comunes**
- Cambiar el prompt "para ayudarlo": entonces ya no comparas el harness, comparas prompts. Usa el texto exacto.
- Partir de `harness` en vez de `main`: ese código ya tiene los montos corregidos en el Bloque 4 y la comparación queda sesgada.
- Al inicio `NormasDelRepoTest` falla, porque `main` tiene `double`. Es esperado: obliga al agente a migrar los montos.

---

## Para llevar a tu repo

| Parte | Archivo |
|---|---|
| Instrucciones | `AGENTS.md`, `.github/instructions/*.instructions.md`, `.github/skills/<nombre>/SKILL.md` |
| Herramientas | `.github/agents/*.agent.md` con `tools` mínimos, MCP (`.vscode/mcp.json` / `.mcp.json`) |
| Entorno | Versiones fijadas (`pom.xml`, wrapper, enforcer) |
| Estado | `docs/planes/`, `PROGRESS.md` |
| Feedback | Sección "Verificación" en `AGENTS.md` + pruebas que fallan con mensajes claros |
