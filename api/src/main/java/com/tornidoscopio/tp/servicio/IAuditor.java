package com.tornidoscopio.tp.servicio;

public interface IAuditor {
    void registrar(String evento, String idUsuario);
}
