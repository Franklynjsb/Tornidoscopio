package com.tornidoscopio.tp.servicio;

import com.tornidoscopio.tp.dao.IFactorDAO;
import com.tornidoscopio.tp.dominio.Factor;
import com.tornidoscopio.tp.dto.FactorDTO;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class GestorFactores {

    // GestorFactores "1" --> "1" IFactorDAO
    private IFactorDAO factorDAO;
    // GestorFactores "1" --> "1" IAuditor
    private IAuditor auditor;

    public List<FactorDTO> buscar(String criterios) {
        return null;
    }

    public List<FactorDTO> buscarPorCompetencia(Long idCompetencia) {
        return null;
    }

    public void alta(FactorDTO dto) {
    }

    public void modificar(FactorDTO dto) {
    }

    public void eliminar(Long id) {
    }

    private FactorDTO armarFactorDTO(Factor f) {
        return null;
    }

    private Factor mapearFactor(FactorDTO dto) {
        return null;
    }
}
