package com.tornidoscopio.tp.servicio;

import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Component;

import java.time.LocalTime;

@Component
@Getter
@Setter
public class RepositorioConfiguracion {
    private String instrucciones;
    private Integer cantidadPreguntasPorBloque;
    private Integer limiteAccesos;
    private LocalTime tiempoMaxCompletar;
    private LocalTime tiempoMaxComenzar;
}
