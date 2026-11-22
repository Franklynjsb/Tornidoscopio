package com.tornidoscopio.tp.dao;

import com.tornidoscopio.tp.dominio.Factor;

import java.util.List;

public interface IFactorDAO {
    void crear(Factor f);
    void modificar(Factor f);
    void eliminar(Long id);
    List<Factor> buscar(String criterio);
    Factor buscarPorId(Long id);
}
