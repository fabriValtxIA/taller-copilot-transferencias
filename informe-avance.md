# Taller "Arma el harness de tu repo": informe de avance

**Cliente:** equipo de un banco · **Responsable:** Fabrizio (Valtx)
**Formato:** taller práctico de GitHub Copilot en VS Code · nivel intermedio · 1 a 2 horas

---

## 1. Objetivo del taller

Enseñar a **sacarle más provecho a GitHub Copilot**.

Copilot rinde mucho mejor cuando el repositorio le dice cómo trabajar: qué reglas respetar, cómo verificar su trabajo y qué quedó pendiente. Sin eso, genera código que funciona pero ignora las normas del equipo. En el taller cada asistente prepara su repositorio para que Copilot cumpla esas normas por sí solo y compara el resultado antes y después.

Al terminar, cada asistente sabrá:
- Escribir las normas del equipo para que Copilot las lea y las cumpla.
- Darle una receta para tareas repetitivas, como agregar una regla de negocio.
- Usar un agente revisor que verifica si el código cumple las normas.
- Conectar Copilot a datos reales de apoyo, como una base de datos.
- Dejar el avance anotado para retomar el trabajo en otra sesión.
- Automatizar una comprobación, por ejemplo correr las pruebas antes de confirmar cambios.

---

## 2. Cómo se llega a eso: qué se arma en cada bloque

Se trabaja sobre un servicio de transferencias bancarias con datos sintéticos (Java y Spring Boot). El repositorio tiene una rama por bloque, de modo que quien se atrasa puede retomar desde ahí.

| Bloque | Qué se aprende | Herramienta de Copilot |
|---|---|---|
| 1. Punto de partida | Ver qué hace Copilot con un repo sin preparar | ninguna |
| 2. Normas | Enseñarle las reglas del equipo y una receta para agregar reglas de negocio | archivo de instrucciones (`AGENTS.md`), skill, instrucciones por tipo de archivo |
| 3. Ayudantes | Un revisor que solo lee y reporta, y acceso a una base de datos de apoyo | agente personalizado, MCP |
| 4. Verificación | Que Copilot no dé nada por terminado si el build o las pruebas fallan | comandos de verificación y una prueba automática de las normas |
| 5. Continuidad | Dejar el avance anotado y retomar en otra sesión | archivo de progreso y plan |
| 6. Cierre | Una comprobación automática antes de confirmar cambios | hook (demostración) |

**Las 4 normas del equipo que Copilot debe cumplir:**
1. No mostrar números de cuenta ni documentos completos en los logs.
2. Usar `BigDecimal` para dinero, nunca `double`.
3. Auditar toda operación, aceptada o rechazada.
4. Una prueba por cada regla de negocio.

---

## 3. El caso del taller: input y output

**Input.** Todos le piden a Copilot lo mismo, con las mismas palabras:

> *"Agrega una regla que rechace las transferencias que superen el límite diario del cliente."*

**Output antes de preparar el repo** (2 corridas):
- La regla funciona, pero el código incumple **las 4 normas**: usa `double` para dinero, muestra cuentas y documentos completos en los logs, no audita y no agrega pruebas.

**Output después de preparar el repo** (3 corridas):
- La regla funciona y cumple **las 4 normas**: dinero en `BigDecimal`, datos enmascarados, cada transferencia auditada y pruebas que aceptan y rechazan.
- El build completo pasa en verde (`./mvnw verify`).
- Copilot, de forma autónoma, ejecuta la verificación y deja anotado el avance en el archivo de progreso.

La diferencia entre el "antes" y el "después" es lo que cada asistente verá con su propio repositorio.

---

## 4. Lo que aprendimos en la prueba interna

- Preparar el repo **cambia el resultado**: de 0 a 4 de 4 normas cumplidas.
- El resultado **varía según la interfaz de Copilot**. En el editor, una corrida no llegó a leer el archivo de normas. En la ventana de Agentes sí lo hizo, tanto con el hook como sin él. Por eso el taller debe indicar en qué interfaz trabajar.
- Aunque Copilot cumple las normas, **deja defectos que las normas no cubren**. Los usaremos como material de cierre para mostrar por qué la revisión humana sigue siendo necesaria:
  - Dos transferencias simultáneas podrían saltarse el límite.
  - Una transferencia de una cuenta a sí misma infla el saldo.
  - No se valida que el monto tenga solo 2 decimales.
- Son pocas corridas (2 sin preparar y 3 preparadas). Alcanzan para mostrar el contraste en el taller, pero no para dar cifras estadísticas.

---

## 5. Mejoras pensadas

1. Agregar un índice del repositorio (un documento de arquitectura corto) para que Copilot encuentre rápido dónde está cada cosa.
2. Dejar normas específicas por módulo cuando el repositorio crezca.
3. Reemplazar el resultado ilustrativo del bloque 1 por el resultado real de las corridas "antes".
4. Sumar un bloque opcional sobre agentes y subagentes, para quien quiera profundizar.
5. Ajustar la receta de reglas de negocio con lo aprendido (por ejemplo, la regla de redondeo).

---

## 6. Qué falta antes del taller

- Repetir las corridas para confirmar que el resultado se sostiene.
- Probar el agente revisor, el acceso a la base de datos y el retomar con el archivo de progreso.
- Verificar que cada rama de bloque compile y medir los tiempos reales por bloque.
- Confirmar con el contacto del cliente: plan y cuota de Copilot de cada asistente (se estiman entre 20 y 30 solicitudes premium por persona), si la ventana de Agentes está disponible, si hay restricciones sobre hooks y si se puede fijar el modelo.
