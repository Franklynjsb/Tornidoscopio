package com.tornidoscopio.tp.dao;

import com.tornidoscopio.tp.dominio.Candidato;
import com.tornidoscopio.tp.dominio.TipoDocumento;

import java.util.List;

public interface ICandidatoDAO {
    void crear(Candidato c);
    void modificar(Candidato c);
    void eliminar(Long id);
    List<Candidato> buscar(String criterio);
    Candidato buscarPorDocumento(TipoDocumento t, String n);
    Candidato buscarPorNroCandidato(Long n);
}
