package com.tornidoscopio.tp.excepciones;

/**
 * Excepción lanzada cuando ocurre un error de entrada/salida al generar
 * el archivo Excel de exportación de candidatos con sus claves de ingreso.
 */
public class ExportacionExcelException extends RuntimeException {

    public ExportacionExcelException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}
