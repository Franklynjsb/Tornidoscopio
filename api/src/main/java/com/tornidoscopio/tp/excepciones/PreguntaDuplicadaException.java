package com.tornidoscopio.tp.excepciones;

/**
 * CU021 - Flujo Alternativo 2.C: ya existe una pregunta con el mismo nombre
 * y/o enunciado para la competencia y factor seleccionados.
 * Transporta el mensaje armado por GestorPreguntas.armarMensajeDuplicado(...)
 * (representa el "error(mensaje)" del diagrama de secuencia).
 */
public class PreguntaDuplicadaException extends RuntimeException {

    public PreguntaDuplicadaException(String mensaje) {
        super(mensaje);
    }
}
