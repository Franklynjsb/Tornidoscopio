package com.tornidoscopio.tp.servicio;

import com.tornidoscopio.tp.dao.ICompetenciaDAO;
import com.tornidoscopio.tp.dominio.Competencia;
import com.tornidoscopio.tp.dto.CompetenciaDTO;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class GestorCompetencias {

    // GestorCompetencias "1" --> "1" ICompetenciaDAO
    private ICompetenciaDAO competenciaDAO;
    // GestorCompetencias "1" --> "1" IAuditor
    private IAuditor auditor;

    public List<CompetenciaDTO> buscar(String criterios) {
        return null;
    }

    public void alta(CompetenciaDTO dto) {
    }

    public void modificar(CompetenciaDTO dto) {
    }

    public void eliminar(Long id) {
    }

    private CompetenciaDTO armarCompetenciaDTO(Competencia c) {
        return null;
    }

    private Competencia mapearCompetencia(CompetenciaDTO dto) {
        return null;
    }
}
