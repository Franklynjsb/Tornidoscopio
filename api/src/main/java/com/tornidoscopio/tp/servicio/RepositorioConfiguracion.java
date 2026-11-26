package com.tornidoscopio.tp.servicio;

import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Component;

import java.time.LocalTime;

@Component
@Getter
@Setter
public class RepositorioConfiguracion {
    private String instrucciones =
            "Lea atentamente cada pregunta y seleccione una única respuesta. "
            + "Las preguntas se presentan en bloques: una vez confirmado un bloque no podrá "
            + "regresar a modificar ni consultar sus respuestas anteriores.";
    private Integer cantidadPreguntasPorBloque = 4;
    private Integer limiteAccesos = 3;
    private LocalTime tiempoMaxCompletar = LocalTime.of(1, 0);
    private LocalTime tiempoMaxComenzar = LocalTime.of(23, 59);

    public String obtenerInstrucciones() {
        return this.instrucciones;
    }

    public Integer obtenerCantidadPreguntasPorBloque() {
        return this.cantidadPreguntasPorBloque;
    }
}
