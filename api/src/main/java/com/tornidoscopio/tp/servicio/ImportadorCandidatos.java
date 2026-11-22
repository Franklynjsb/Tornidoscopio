package com.tornidoscopio.tp.servicio;

import com.tornidoscopio.tp.dao.ICandidatoDAO;
import com.tornidoscopio.tp.dto.ResultadoImportacionDTO;
import org.springframework.stereotype.Service;

import java.io.File;

@Service
public class ImportadorCandidatos {

    // ImportadorCandidatos "1" --> "1" ICandidatoDAO
    private ICandidatoDAO candidatoDAO;

    public ResultadoImportacionDTO importarCandidatos(File a) {
        return null;
    }
}
