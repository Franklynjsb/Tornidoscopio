package com.tornidoscopio.tp.dao;

import com.tornidoscopio.tp.dominio.PreguntaOpcion;

public interface IPreguntaOpcionDAO {
    void crear(PreguntaOpcion po);
    void modificar(PreguntaOpcion po);
    void eliminar(Long idPregunta, Long idItemOpcion);
}
