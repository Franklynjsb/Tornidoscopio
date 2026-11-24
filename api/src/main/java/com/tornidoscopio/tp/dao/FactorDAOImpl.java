package com.tornidoscopio.tp.dao;

import com.tornidoscopio.tp.dominio.Factor;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class FactorDAOImpl implements IFactorDAO {

    @PersistenceContext
    private EntityManager em;

    @Override
    public void crear(Factor f) {
        throw new UnsupportedOperationException("No implementado en CU021");
    }

    @Override
    public void modificar(Factor f) {
        throw new UnsupportedOperationException("No implementado en CU021");
    }

    @Override
    public void eliminar(Long id) {
        throw new UnsupportedOperationException("No implementado en CU021");
    }

    @Override
    public List<Factor> buscar(String criterio) {
        throw new UnsupportedOperationException("No implementado en CU021");
    }

    // CU021 - Selección de Competencia: factores activos de esa competencia.
    // Se trae la Competencia junto al Factor para poder armar el FactorDTO (idCompetencia, nombreCompetencia).
    @Override
    public List<Factor> buscarPorCompetencia(Long idCompetencia) {
        return em.createQuery(
                        "SELECT f FROM Factor f JOIN FETCH f.competencia c " +
                        "WHERE c.id = :idCompetencia AND f.activo = true " +
                        "ORDER BY f.nroOrden",
                        Factor.class)
                .setParameter("idCompetencia", idCompetencia)
                .getResultList();
    }

    // CU021 - 1.A.1 Generar con IA y Paso 3 (mapeo de la Pregunta)
    @Override
    public Factor buscarPorId(Long id) {
        return em.find(Factor.class, id);
    }
}
