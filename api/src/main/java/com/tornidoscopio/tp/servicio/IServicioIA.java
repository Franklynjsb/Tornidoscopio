package com.tornidoscopio.tp.servicio;

import com.tornidoscopio.tp.dominio.Competencia;
import com.tornidoscopio.tp.dominio.Factor;
import com.tornidoscopio.tp.dominio.OpcionRespuesta;
import com.tornidoscopio.tp.dto.PreguntaSugeridaDTO;

public interface IServicioIA {
    PreguntaSugeridaDTO generarPregunta(Competencia c, Factor f, String descripcion, OpcionRespuesta opc);
}
