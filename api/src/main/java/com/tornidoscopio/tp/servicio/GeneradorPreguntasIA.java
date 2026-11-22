package com.tornidoscopio.tp.servicio;

import com.tornidoscopio.tp.dominio.Competencia;
import com.tornidoscopio.tp.dominio.Factor;
import com.tornidoscopio.tp.dominio.OpcionRespuesta;
import com.tornidoscopio.tp.dto.PreguntaSugeridaDTO;
import org.springframework.stereotype.Component;

@Component
public class GeneradorPreguntasIA implements IServicioIA {

    @Override
    public PreguntaSugeridaDTO generarPregunta(Competencia c, Factor f, String descripcion, OpcionRespuesta opc) {
        return null;
    }
}
