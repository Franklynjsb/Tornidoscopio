package com.tornidoscopio.tp.dao;

import com.tornidoscopio.tp.dominio.Cuestionario;
import com.tornidoscopio.tp.dominio.Puesto;
import com.tornidoscopio.tp.dominio.Resultado;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Implementación JPA del patrón DAO para la entidad {@link Resultado}.
 */
@Repository
public class ResultadoDAOImpl implements IResultadoDAO {

    @PersistenceContext
    private EntityManager em;

    @Override
    public void crear(Resultado r) {
        if (r.getCuestionario() != null
                && !em.contains(r.getCuestionario())
                && r.getCuestionario().getId() != null) {
            r.setCuestionario(em.getReference(Cuestionario.class, r.getCuestionario().getId()));
        }
        em.persist(r);
    }

    @Override
    public void modificar(Resultado r) {
        em.merge(r);
    }

    @Override
    public void eliminar(Long id) {
        Resultado r = em.find(Resultado.class, id);
        if (r != null) {
            em.remove(r);
        }
    }

    @Override
    public List<Resultado> buscarPorPuesto(Puesto p) {
        if (p == null || p.getId() == null) {
            return List.of();
        }
        return em.createQuery(
                        "SELECT DISTINCT r FROM Resultado r " +
                        "JOIN FETCH r.cuestionario c " +
                        "LEFT JOIN FETCH r.resultadosCompetencia rc " +
                        "LEFT JOIN FETCH rc.competencia " +
                        "WHERE c.puesto.id = :idPuesto " +
                        "ORDER BY r.fechaCalculo DESC",
                        Resultado.class)
                .setParameter("idPuesto", p.getId())
                .getResultList();
    }
}
