# Servicio de transferencias

Servicio REST pequeño para transferir dinero entre cuentas. Usa datos sintéticos en una base H2 en memoria.

## Requisitos previos

- JDK 21
- VS Code con las extensiones GitHub Copilot y GitHub Copilot Chat, con sesión iniciada
- Git

No hace falta instalar Maven: el repo trae el wrapper (`mvnw` / `mvnw.cmd`), que descarga Maven 3.9.16 la primera vez.

## Compilar y ejecutar

```bash
./mvnw verify            # Windows: mvnw.cmd verify
./mvnw spring-boot:run
```

## Probar

```bash
curl -X POST http://localhost:8080/api/transferencias \
  -H "Content-Type: application/json" \
  -d '{"cuentaOrigen":"0011-0457-0200123456","cuentaDestino":"0011-0457-0200987654","monto":150.75}'
```

Las cuentas de prueba están en `src/main/resources/data.sql`. La consola de H2 queda en http://localhost:8080/h2-console (URL JDBC `jdbc:h2:mem:banco`, usuario `sa`).
