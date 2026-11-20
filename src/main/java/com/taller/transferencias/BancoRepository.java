package com.taller.transferencias;

import java.time.LocalDate;
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
                (rs, i) -> new Cuenta(rs.getString("numero"), rs.getString("documento"), rs.getDouble("saldo")),
                numero).stream().findFirst();
    }

    public Optional<Cliente> buscarCliente(String documento) {
        return jdbc.query("SELECT documento, nombre, limite_diario FROM cliente WHERE documento = ?",
                (rs, i) -> new Cliente(rs.getString("documento"), rs.getString("nombre"), rs.getDouble("limite_diario")),
                documento).stream().findFirst();
    }

    public double totalTransferidoHoy(String documento) {
        LocalDateTime inicioDia = LocalDate.now().atStartOfDay();
        LocalDateTime inicioDiaSiguiente = inicioDia.plusDays(1);

        return jdbc.queryForObject("""
                SELECT COALESCE(SUM(t.monto), 0)
                FROM transferencia t
                JOIN cuenta c ON c.numero = t.cuenta_origen
                WHERE c.documento = ? AND t.fecha >= ? AND t.fecha < ?
                """,
                Double.class, documento, inicioDia, inicioDiaSiguiente);
    }

    public void actualizarSaldo(String numero, double saldo) {
        jdbc.update("UPDATE cuenta SET saldo = ? WHERE numero = ?", saldo, numero);
    }

    public void guardarTransferencia(String origen, String destino, double monto) {
        jdbc.update("INSERT INTO transferencia (cuenta_origen, cuenta_destino, monto, fecha) VALUES (?, ?, ?, ?)",
                origen, destino, monto, LocalDateTime.now());
    }
}
