package com.tornidoscopio.tp.dao;

import com.tornidoscopio.tp.dominio.ItemOpcion;

import java.util.List;

public interface IItemOpcionDAO {
    void crear(ItemOpcion i);
    void modificar(ItemOpcion i);
    void eliminar(Long id);
    List<ItemOpcion> buscarPorOpcion(Long idOpcion);
}
