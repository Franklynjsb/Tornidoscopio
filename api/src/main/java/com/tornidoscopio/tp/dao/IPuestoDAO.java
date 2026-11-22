package com.tornidoscopio.tp.dao;

import com.tornidoscopio.tp.dominio.Puesto;

import java.util.List;

public interface IPuestoDAO {
    void crear(Puesto p);
    void modificar(Puesto p);
    void eliminar(Long id);
    List<Puesto> buscar(String criterio);
    Puesto buscarPorId(Long id);
}
