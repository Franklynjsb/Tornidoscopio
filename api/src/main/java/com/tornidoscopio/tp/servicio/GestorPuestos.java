package com.tornidoscopio.tp.servicio;

import com.tornidoscopio.tp.dao.ICuestionarioDAO;
import com.tornidoscopio.tp.dao.IEmpresaDAO;
import com.tornidoscopio.tp.dao.IPuestoDAO;
import com.tornidoscopio.tp.dominio.CompetenciaRequerida;
import com.tornidoscopio.tp.dominio.Puesto;
import com.tornidoscopio.tp.dto.CompetenciaRequeridaDTO;
import com.tornidoscopio.tp.dto.PuestoDTO;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class GestorPuestos {

    // GestorPuestos "1" --> "1" IPuestoDAO
    private IPuestoDAO puestoDAO;
    // GestorPuestos "1" --> "1" IEmpresaDAO
    private IEmpresaDAO empresaDAO;
    // GestorPuestos "1" --> "1" ICuestionarioDAO
    private ICuestionarioDAO cuestionarioDAO;
    // GestorPuestos "1" --> "1" IAuditor
    private IAuditor auditor;

    public List<PuestoDTO> buscarPuestos(String criterios) {
        return null;
    }

    public void nuevoPuesto(PuestoDTO p) {
    }

    public void modificarPuesto(PuestoDTO p) {
    }

    public void eliminarPuesto(Long idPuesto) {
    }

    public void definirCompetencias(Long idPuesto, List<CompetenciaRequeridaDTO> c) {
    }

    public Boolean tieneCuestionariosGenerados(Long idPuesto) {
        return null;
    }

    public List<CompetenciaRequeridaDTO> obtenerCompetenciasRequeridas(Long idPuesto) {
        return null;
    }

    private void validarFactoresEvaluables(List<CompetenciaRequerida> list) {
    }

    private PuestoDTO armarPuestoDTO(Puesto p) {
        return null;
    }

    private Puesto mapearPuesto(PuestoDTO dto) {
        return null;
    }

    private CompetenciaRequeridaDTO armarCompetenciaRequeridaDTO(CompetenciaRequerida cr) {
        return null;
    }
}
