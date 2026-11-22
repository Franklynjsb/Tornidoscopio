package com.tornidoscopio.tp.dao;

import com.tornidoscopio.tp.dominio.Cuestionario;

import java.util.List;

public interface ICuestionarioDAO {
    void crear(Cuestionario c);
    void modificar(Cuestionario c);
    void eliminar(Long id);
    Cuestionario buscarPorClaveIngreso(String c);
    List<Cuestionario> buscarVencidos();
    List<Cuestionario> buscarActivosOEnProceso(List<Long> ids);
    List<Cuestionario> buscarPorPuesto(Long idPuesto);
    Cuestionario buscarPorId(Long id);
}
