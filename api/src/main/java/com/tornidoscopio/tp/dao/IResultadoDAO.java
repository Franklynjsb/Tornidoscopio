package com.tornidoscopio.tp.dao;

import com.tornidoscopio.tp.dominio.Puesto;
import com.tornidoscopio.tp.dominio.Resultado;

import java.util.List;

public interface IResultadoDAO {
    void crear(Resultado r);
    void modificar(Resultado r);
    void eliminar(Long id);
    List<Resultado> buscarPorPuesto(Puesto p);
}
