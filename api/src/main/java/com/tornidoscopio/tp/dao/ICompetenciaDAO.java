package com.tornidoscopio.tp.dao;

import com.tornidoscopio.tp.dominio.Competencia;

import java.util.List;

public interface ICompetenciaDAO {
    void crear(Competencia c);
    void modificar(Competencia c);
    void eliminar(Long id);
    List<Competencia> buscar(String criterio);
    Competencia buscarPorId(Long id);
}
