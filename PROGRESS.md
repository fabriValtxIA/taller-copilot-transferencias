# Estado del proyecto

## Hecho
- Agregada la regla de límite diario por cliente en `TransferenciaService`.
- El límite diario compara el monto de la operación con el acumulado de transferencias del día.
- Migrado el flujo de montos y saldos de tipos flotantes a `BigDecimal`.
- Agregada auditoría separada con `AuditoriaService` y la tabla `auditoria`.
- Agregado enmascaramiento seguro para cuentas y documentos en logs.
- Agregadas pruebas de aceptación y rechazo del límite diario.
- Verificados `compile`, `test` y `verify` sin errores.
