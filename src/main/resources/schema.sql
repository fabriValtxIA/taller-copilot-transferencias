CREATE TABLE cliente (
    documento      VARCHAR(12)  PRIMARY KEY,
    nombre         VARCHAR(100) NOT NULL,
    limite_diario  DECIMAL(15,2) NOT NULL
);

CREATE TABLE cuenta (
    numero     VARCHAR(20) PRIMARY KEY,
    documento  VARCHAR(12) NOT NULL REFERENCES cliente(documento),
    saldo      DECIMAL(15,2) NOT NULL
);

CREATE TABLE transferencia (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    cuenta_origen   VARCHAR(20) NOT NULL,
    cuenta_destino  VARCHAR(20) NOT NULL,
    monto           DECIMAL(15,2) NOT NULL,
    fecha           TIMESTAMP   NOT NULL
);

CREATE TABLE auditoria (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    fecha           TIMESTAMP NOT NULL,
    cuenta_origen   VARCHAR(20),
    cuenta_destino  VARCHAR(20),
    monto           DECIMAL(15,2),
    resultado       VARCHAR(20) NOT NULL,
    motivo          VARCHAR(200) NOT NULL
);
