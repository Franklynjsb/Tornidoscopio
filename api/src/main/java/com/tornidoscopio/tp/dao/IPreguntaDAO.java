package com.tornidoscopio.tp.dao;

import com.tornidoscopio.tp.dominio.Pregunta;

import java.util.List;

public interface IPreguntaDAO {
    void crear(Pregunta p);
    void modificar(Pregunta p);
    void eliminar(Long id);
    List<Pregunta> buscar(String competencia, String factor, String nombrePregunta);
}
