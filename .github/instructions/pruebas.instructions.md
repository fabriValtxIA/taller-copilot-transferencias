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
