package com.tornidoscopio.tp.servicio;

import com.tornidoscopio.tp.dao.ICandidatoDAO;
import com.tornidoscopio.tp.dao.ICuestionarioDAO;
import com.tornidoscopio.tp.dominio.TipoDocumento;
import org.springframework.stereotype.Service;

@Service
public class GestorAutenticacion {

    private IServicioAutenticacion servicioAutenticacion;
    private ICandidatoDAO candidatoDAO;
    private ICuestionarioDAO cuestionarioDAO;

    public Boolean loginConsultor(String u, String p) {
        return null;
    }

    public Boolean loginCandidato(TipoDocumento t, String d, String c) {
        return null;
    }
}
