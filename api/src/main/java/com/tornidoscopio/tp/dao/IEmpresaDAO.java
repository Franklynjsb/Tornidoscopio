package com.tornidoscopio.tp.dao;

import com.tornidoscopio.tp.dominio.Empresa;

import java.util.List;

public interface IEmpresaDAO {
    Empresa buscarPorId(Long id);
    List<Empresa> buscarTodas();
}
