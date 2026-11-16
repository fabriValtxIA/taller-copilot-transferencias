package com.taller.transferencias;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class BancoRepository {

    private final JdbcTemplate jdbc;

    public BancoRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public Optional<Cuenta> buscarCuenta(String numero) {
        return jdbc.query("SELECT numero, documento, saldo FROM cuenta WHERE numero = ?",
                (rs, i) -> new Cuenta(rs.getString("numero"), rs.getString("documento"), rs.getBigDecimal("saldo")),
                numero).stream().findFirst();
    }

    public Optional<Cliente> buscarCliente(String documento) {
        return jdbc.query("SELECT documento, nombre, limite_diario FROM cliente WHERE documento = ?",
                (rs, i) -> new Cliente(rs.getString("documento"), rs.getString("nombre"), rs.getBigDecimal("limite_diario")),
                documento).stream().findFirst();
    }

    public void actualizarSaldo(String numero, BigDecimal saldo) {
        jdbc.update("UPDATE cuenta SET saldo = ? WHERE numero = ?", saldo, numero);
    }

    public void guardarTransferencia(String origen, String destino, BigDecimal monto) {
        jdbc.update("INSERT INTO transferencia (cuenta_origen, cuenta_destino, monto, fecha) VALUES (?, ?, ?, ?)",
                origen, destino, monto, LocalDateTime.now());
    }
}
