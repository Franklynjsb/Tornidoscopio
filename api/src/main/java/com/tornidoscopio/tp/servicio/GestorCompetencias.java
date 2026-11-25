package com.tornidoscopio.tp.servicio;

import com.tornidoscopio.tp.dao.ICompetenciaDAO;
import com.tornidoscopio.tp.dominio.Competencia;
import com.tornidoscopio.tp.dto.CompetenciaDTO;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class GestorCompetencias {

    // GestorCompetencias "1" --> "1" ICompetenciaDAO
    private ICompetenciaDAO competenciaDAO;
    // GestorCompetencias "1" --> "1" IAuditor
    private IAuditor auditor;

    public GestorCompetencias(ICompetenciaDAO competenciaDAO) {
        this.competenciaDAO = competenciaDAO;
    }

    // CU021 - Carga inicial de Competencias para el selector
    public List<CompetenciaDTO> buscar(String criterios) {
        List<Competencia> listaCompetencias = competenciaDAO.buscar(criterios);
        List<CompetenciaDTO> listaCompetenciaDTO = new ArrayList<>();

        for (Competencia c : listaCompetencias) {
            CompetenciaDTO compeDTO = armarCompetenciaDTO(c);
            listaCompetenciaDTO.add(compeDTO);
        }

        return listaCompetenciaDTO;
    }

    public void alta(CompetenciaDTO dto) {
    }

    public void modificar(CompetenciaDTO dto) {
    }

    public void eliminar(Long id) {
    }

    private CompetenciaDTO armarCompetenciaDTO(Competencia c) {
        return new CompetenciaDTO(c.getId(), c.getCodigo(), c.getNombre(), c.getDescripcion());
    }

    private Competencia mapearCompetencia(CompetenciaDTO dto) {
        return null;
    }
}
