package com.taller.transferencias;

import java.math.BigDecimal;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/transferencias")
public class TransferenciaController {

    public record TransferenciaRequest(String cuentaOrigen, String cuentaDestino, BigDecimal monto) {
    }

    private final TransferenciaService service;

    public TransferenciaController(TransferenciaService service) {
        this.service = service;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Map<String, String> transferir(@RequestBody TransferenciaRequest request) {
        service.transferir(request.cuentaOrigen(), request.cuentaDestino(), request.monto());
        return Map.of("estado", "COMPLETADA");
    }

    @ExceptionHandler(TransferenciaRechazadaException.class)
    @ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
    public Map<String, String> rechazada(TransferenciaRechazadaException e) {
        return Map.of("estado", "RECHAZADA", "motivo", e.getMessage());
    }
}
