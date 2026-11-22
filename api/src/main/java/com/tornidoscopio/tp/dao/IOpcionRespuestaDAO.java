package com.tornidoscopio.tp.dao;

import com.tornidoscopio.tp.dominio.OpcionRespuesta;

import java.util.List;

public interface IOpcionRespuestaDAO {
    void crear(OpcionRespuesta o);
    void modificar(OpcionRespuesta o);
    void eliminar(Long id);
    List<OpcionRespuesta> buscar(String criterio);
    OpcionRespuesta buscarPorId(Long id);
}
