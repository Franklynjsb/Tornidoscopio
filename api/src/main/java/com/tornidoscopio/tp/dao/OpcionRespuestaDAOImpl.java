package com.tornidoscopio.tp.dao;

import com.tornidoscopio.tp.dominio.OpcionRespuesta;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class OpcionRespuestaDAOImpl implements IOpcionRespuestaDAO {

    @PersistenceContext
    private EntityManager em;

    @Override
    public void crear(OpcionRespuesta o) {
        throw new UnsupportedOperationException("No implementado en CU021");
    }

    @Override
    public void modificar(OpcionRespuesta o) {
        throw new UnsupportedOperationException("No implementado en CU021");
    }

    @Override
    public void eliminar(Long id) {
        throw new UnsupportedOperationException("No implementado en CU021");
    }

    // CU021 - Carga inicial del selector de Opciones de Respuesta.
    // Si el criterio es vacío devuelve todas las opciones activas; si no, filtra por nombre.
    @Override
    public List<OpcionRespuesta> buscar(String criterio) {
        if (criterio == null || criterio.isBlank()) {
            return em.createQuery(
                            "SELECT o FROM OpcionRespuesta o WHERE o.activo = true ORDER BY o.nombre",
                            OpcionRespuesta.class)
                    .getResultList();
        }
        return em.createQuery(
                        "SELECT o FROM OpcionRespuesta o " +
                        "WHERE o.activo = true AND LOWER(o.nombre) LIKE LOWER(:criterio) " +
                        "ORDER BY o.nombre",
                        OpcionRespuesta.class)
                .setParameter("criterio", "%" + criterio.trim() + "%")
                .getResultList();
    }

    // CU021 - 1.A.1 Generar con IA y Paso 3 (mapeo de la Pregunta)
    @Override
    public OpcionRespuesta buscarPorId(Long id) {
        return em.find(OpcionRespuesta.class, id);
    }
}
