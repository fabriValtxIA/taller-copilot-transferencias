-- Datos sintéticos. Ningún cliente ni cuenta es real.
INSERT INTO cliente (documento, nombre, limite_diario) VALUES ('45879632', 'Ana Torres Ruiz', 5000.00);
INSERT INTO cliente (documento, nombre, limite_diario) VALUES ('70214568', 'Luis Quispe Mamani', 1500.00);
INSERT INTO cliente (documento, nombre, limite_diario) VALUES ('10457896321', 'Comercial Andina SAC', 50000.00);

INSERT INTO cuenta (numero, documento, saldo) VALUES ('0011-0457-0200123456', '45879632', 12500.50);
INSERT INTO cuenta (numero, documento, saldo) VALUES ('0011-0457-0200987654', '70214568', 3200.10);
INSERT INTO cuenta (numero, documento, saldo) VALUES ('0011-0457-0200555777', '10457896321', 98000.00);
