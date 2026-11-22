package com.tornidoscopio.tp.servicio;

import org.springframework.stereotype.Component;

@Component
public class AutenticadorLDAP implements IServicioAutenticacion {

    @Override
    public Boolean validarUsuario(String u, String p) {
        return null;
    }
}
