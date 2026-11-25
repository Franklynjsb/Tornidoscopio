package com.tornidoscopio.tp.dominio;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class FactorTest {

    @Test
    @DisplayName("cantidadPreguntasActivas retorna 0 cuando la lista es nula o vacía")
    void cantidadPreguntasActivas_nulaOVacia() {
        Factor sinPreguntas = Factor.builder().id(1L).preguntas(null).build();
        Factor vacio = Factor.builder().id(2L).preguntas(List.of()).build();

        assertEquals(0, sinPreguntas.cantidadPreguntasActivas());
        assertEquals(0, vacio.cantidadPreguntasActivas());
    }

    @Test
    @DisplayName("cantidadPreguntasActivas cuenta únicamente preguntas con activo == true")
    void cantidadPreguntasActivas_soloCuentaActivas() {
        Factor factor = Factor.builder()
                .id(1L)
                .preguntas(List.of(
                        Pregunta.builder().id(1L).activo(true).build(),
                        Pregunta.builder().id(2L).activo(false).build(),
                        Pregunta.builder().id(3L).activo(true).build(),
                        Pregunta.builder().id(4L).activo(null).build()
                ))
                .build();

        assertEquals(2, factor.cantidadPreguntasActivas());
    }
}
