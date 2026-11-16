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
