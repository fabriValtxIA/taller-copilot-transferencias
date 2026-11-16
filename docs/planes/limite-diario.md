# Plan: rechazar transferencias que superen el límite diario del cliente

## Objetivo
Rechazar una transferencia cuando la suma de lo transferido hoy por el cliente (todas sus
cuentas) más el monto nuevo supere `cliente.limite_diario`. Toda transferencia, aceptada o
rechazada, queda en la tabla `auditoria`.

## Decisiones
- El límite es **por cliente**, no por cuenta. "Hoy" = fecha del servidor (`CURRENT_DATE`).
- Igualar el límite está permitido; superarlo por 0.01 se rechaza.
- Solo cuentan las transferencias completadas (tabla `transferencia`), no las rechazadas.

## Tareas
1. Crear `Enmascarar` (`cuenta(..)`, `documento(..)`: solo últimos 4 visibles) y usarlo en todos los logs de `TransferenciaService`.
2. Crear tabla `auditoria` y `AuditoriaService.registrar(..)` con `REQUIRES_NEW`.
3. Auditar en `transferir(..)`: `ACEPTADA` al final y `RECHAZADA` en cada rechazo.
4. `BancoRepository.totalTransferidoHoy(documento)` en `BigDecimal`.
5. `validarLimiteDiario(..)` en `TransferenciaService`, después de `validarSaldo(..)`.
6. Pruebas: igual al límite (acepta), supera por 0.01 (rechaza + auditoría `RECHAZADA`), acumulado de dos transferencias (rechaza la segunda), `Enmascarar`.
7. `./mvnw verify` en verde y revisión con `@revisor-cumplimiento` con veredicto APTO.
