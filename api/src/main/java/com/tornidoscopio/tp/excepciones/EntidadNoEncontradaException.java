package com.tornidoscopio.tp.excepciones;

/**
 * Excepción de negocio lanzada cuando no se encuentra una entidad requerida
 * en la base de datos o se encuentra dada de baja/eliminada.
 */
public class EntidadNoEncontradaException extends RuntimeException {

    public EntidadNoEncontradaException(String mensaje) {
        super(mensaje);
    }
}
