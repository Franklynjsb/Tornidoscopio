package com.tornidoscopio.tp.dao;

import com.tornidoscopio.tp.dominio.Competencia;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class CompetenciaDAOImpl implements ICompetenciaDAO {

    @PersistenceContext
    private EntityManager em;

    @Override
    public void crear(Competencia c) {
        throw new UnsupportedOperationException("No implementado en CU021");
    }

    @Override
    public void modificar(Competencia c) {
        throw new UnsupportedOperationException("No implementado en CU021");
    }

    @Override
    public void eliminar(Long id) {
        throw new UnsupportedOperationException("No implementado en CU021");
    }

    // CU021 - Carga inicial del selector de Competencias.
    // Si el criterio es vacío devuelve todas las competencias activas; si no, filtra por código o nombre.
    @Override
    public List<Competencia> buscar(String criterio) {
        if (criterio == null || criterio.isBlank()) {
            return em.createQuery(
                            "SELECT c FROM Competencia c WHERE c.activo = true ORDER BY c.nombre",
                            Competencia.class)
                    .getResultList();
        }
        return em.createQuery(
                        "SELECT c FROM Competencia c " +
                        "WHERE c.activo = true " +
                        "AND (LOWER(c.codigo) LIKE LOWER(:criterio) OR LOWER(c.nombre) LIKE LOWER(:criterio)) " +
                        "ORDER BY c.nombre",
                        Competencia.class)
                .setParameter("criterio", "%" + criterio.trim() + "%")
                .getResultList();
    }

    // CU021 - 1.A.1 Generar con IA
    @Override
    public Competencia buscarPorId(Long id) {
        return em.find(Competencia.class, id);
    }
}
