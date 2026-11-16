# Servicio de transferencias

Servicio REST que transfiere dinero entre cuentas de clientes del banco.
Todos los datos del repo son sintéticos.

## Stack
- Java 21, Spring Boot 3.5.9, Maven 3.9 (usar siempre el wrapper `./mvnw`; en Windows `mvnw.cmd`).
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

## Verificación
Ejecuta estos comandos antes de dar por terminada cualquier tarea. Si alguno
falla, lee el error, corrige y vuelve a ejecutar. **Ninguna tarea está
terminada con un comando en rojo.**
1. `./mvnw -q compile` (compila con `-Xlint:all -Werror`: cero advertencias).
2. `./mvnw -q test` (incluye `NormasDelRepoTest`, que rechaza `double`).
3. `./mvnw -q verify` (build completo).

Si una norma choca con el código existente, corrige el código existente.
Para agregar una regla de negocio, sigue la skill `regla-de-negocio`.
