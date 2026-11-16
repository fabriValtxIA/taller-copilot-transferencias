package com.taller.transferencias;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;

/** Verificación automática de la norma 2: el dinero nunca va en double/float. */
class NormasDelRepoTest {

    private static final Pattern TIPO_FLOTANTE = Pattern.compile("\b(double|Double|float|Float|DOUBLE|FLOAT|REAL)\b");

    @Test
    void ningun_archivo_de_produccion_usa_tipos_flotantes() throws IOException {
        // dado
        List<Path> fuentes;
        try (Stream<Path> archivos = Files.walk(Path.of("src/main"))) {
            fuentes = archivos.filter(p -> p.toString().endsWith(".java") || p.toString().endsWith(".sql")).toList();
        }
        // cuando
        List<String> infracciones = fuentes.stream()
                .flatMap(NormasDelRepoTest::lineasConFlotantes)
                .toList();
        // entonces
        assertThat(infracciones).as("Usa BigDecimal / DECIMAL(15,2) para dinero").isEmpty();
    }

    private static Stream<String> lineasConFlotantes(Path archivo) {
        try {
            List<String> lineas = Files.readAllLines(archivo);
            return java.util.stream.IntStream.range(0, lineas.size())
                    .filter(i -> TIPO_FLOTANTE.matcher(lineas.get(i)).find())
                    .mapToObj(i -> archivo + ":" + (i + 1) + "  " + lineas.get(i).trim());
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }
}
