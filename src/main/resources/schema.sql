CREATE TABLE cliente (
    documento      VARCHAR(12)  PRIMARY KEY,
    nombre         VARCHAR(100) NOT NULL,
    limite_diario  DOUBLE       NOT NULL
);

CREATE TABLE cuenta (
    numero     VARCHAR(20) PRIMARY KEY,
    documento  VARCHAR(12) NOT NULL REFERENCES cliente(documento),
    saldo      DOUBLE      NOT NULL
);

CREATE TABLE transferencia (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    cuenta_origen   VARCHAR(20) NOT NULL,
    cuenta_destino  VARCHAR(20) NOT NULL,
    monto           DOUBLE      NOT NULL,
    fecha           TIMESTAMP   NOT NULL
);
