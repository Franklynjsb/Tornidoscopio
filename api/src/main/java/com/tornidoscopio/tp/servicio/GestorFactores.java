package com.tornidoscopio.tp.servicio;

import com.tornidoscopio.tp.dao.IFactorDAO;
import com.tornidoscopio.tp.dominio.Factor;
import com.tornidoscopio.tp.dto.FactorDTO;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class GestorFactores {

    // GestorFactores "1" --> "1" IFactorDAO
    private IFactorDAO factorDAO;
    // GestorFactores "1" --> "1" IAuditor
    private IAuditor auditor;

    public GestorFactores(IFactorDAO factorDAO) {
        this.factorDAO = factorDAO;
    }

    public List<FactorDTO> buscar(String criterios) {
        return null;
    }

    // CU021 - Selección de Competencia -> carga los Factores de esa Competencia
    public List<FactorDTO> buscarPorCompetencia(Long idCompetencia) {
        List<Factor> listaFactores = factorDAO.buscarPorCompetencia(idCompetencia);
        List<FactorDTO> listaFactorDTO = new ArrayList<>();

        for (Factor f : listaFactores) {
            FactorDTO facDTO = armarFactorDTO(f);
            listaFactorDTO.add(facDTO);
        }

        return listaFactorDTO;
    }

    public void alta(FactorDTO dto) {
    }

    public void modificar(FactorDTO dto) {
    }

    public void eliminar(Long id) {
    }

    private FactorDTO armarFactorDTO(Factor f) {
        return new FactorDTO(
                f.getId(),
                f.getNombre(),
                f.getDescripcion(),
                f.getNroOrden(),
                f.getCompetencia().getId(),
                f.getCompetencia().getNombre());
    }

    private Factor mapearFactor(FactorDTO dto) {
        return null;
    }
}
