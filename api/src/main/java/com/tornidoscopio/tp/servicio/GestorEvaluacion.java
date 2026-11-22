package com.tornidoscopio.tp.servicio;

import com.tornidoscopio.tp.dao.ICandidatoDAO;
import com.tornidoscopio.tp.dao.ICuestionarioDAO;
import com.tornidoscopio.tp.dominio.Candidato;
import com.tornidoscopio.tp.dominio.Cuestionario;
import com.tornidoscopio.tp.dominio.Puesto;
import com.tornidoscopio.tp.dto.CandidatoClaveDTO;
import com.tornidoscopio.tp.dto.CandidatoGestionDTO;
import com.tornidoscopio.tp.dto.PuestoDTO;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.List;

@Service
public class GestorEvaluacion {

    // GestorEvaluacion "1" --> "1" ICandidatoDAO
    private ICandidatoDAO candidatoDAO;
    // GestorEvaluacion "1" --> "1" ICuestionarioDAO
    private ICuestionarioDAO cuestionarioDAO;

    public List<CandidatoGestionDTO> buscarCandidatos(String criterios) {
        return null;
    }

    public List<Cuestionario> validarCuestionariosActivos(List<Long> ids) {
        return null;
    }

    public void generarCuestionarios(PuestoDTO p, List<CandidatoGestionDTO> c) {
    }

    public void confirmarYGuardarCuestionarios() {
    }

    public File exportarCandidatosConClaves(Long idPuesto, List<CandidatoClaveDTO> c) {
        return null;
    }

    public void actualizarCuestionariosVencidos() {
    }

    private CandidatoGestionDTO armarCandidatoGestionDTO(Candidato c) {
        return null;
    }

    private CandidatoClaveDTO armarCandidatoClaveDTO(Candidato c) {
        return null;
    }

    private Puesto mapearPuesto(PuestoDTO dto) {
        return null;
    }

    private String generarClaveIngreso() {
        return null;
    }

    private String generarNombreArchivo() {
        return null;
    }
}
