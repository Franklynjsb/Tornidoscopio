package com.tornidoscopio.tp.dao;

import com.tornidoscopio.tp.dominio.Candidato;
import com.tornidoscopio.tp.dominio.Cuestionario;
import com.tornidoscopio.tp.dominio.EstadoCuestionario;
import com.tornidoscopio.tp.dominio.Puesto;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Implementación JPA del patrón DAO para la entidad {@link Cuestionario}.
 */
@Repository
public class CuestionarioDAOImpl implements ICuestionarioDAO {

    @PersistenceContext
    private EntityManager em;

    /**
     * CU025 - Persiste un nuevo cuestionario asegurando referencias administradas
     * para Candidato y Puesto.
     */
    @Override
    public void crear(Cuestionario c) {
        if (c.getCandidato() != null
                && !em.contains(c.getCandidato())
                && c.getCandidato().getNroCandidato() != null) {
            c.setCandidato(em.getReference(Candidato.class, c.getCandidato().getNroCandidato()));
        }
        if (c.getPuesto() != null
                && !em.contains(c.getPuesto())
                && c.getPuesto().getId() != null) {
            c.setPuesto(em.getReference(Puesto.class, c.getPuesto().getId()));
        }
        em.persist(c);
    }

    @Override
    public void modificar(Cuestionario c) {
        em.merge(c);
    }

    @Override
    public void eliminar(Long id) {
        Cuestionario c = em.find(Cuestionario.class, id);
        if (c != null) {
            em.remove(c);
        }
    }

    @Override
    public Cuestionario buscarPorClaveIngreso(String c) {
        if (c == null || c.isBlank()) {
            return null;
        }
        return em.createQuery(
                        "SELECT q FROM Cuestionario q " +
                        "JOIN FETCH q.candidato " +
                        "JOIN FETCH q.puesto " +
                        "WHERE q.claveIngreso = :clave",
                        Cuestionario.class)
                .setParameter("clave", c.trim())
                .getResultStream()
                .findFirst()
                .orElse(null);
    }

    @Override
    public List<Cuestionario> buscarVencidos() {
        return em.createQuery(
                        "SELECT q FROM Cuestionario q " +
                        "WHERE q.estado IN :estados",
                        Cuestionario.class)
                .setParameter("estados", List.of(EstadoCuestionario.ACTIVO, EstadoCuestionario.EN_PROCESO))
                .getResultList();
    }

    /**
     * CU025 - Paso 6 / Flujo Alternativo 6.A: recupera los cuestionarios en estado
     * ACTIVO o EN_PROCESO pertenecientes a los candidatos cuyos números se reciben.
     */
    @Override
    public List<Cuestionario> buscarActivosOEnProceso(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return List.of();
        }
        return em.createQuery(
                        "SELECT q FROM Cuestionario q " +
                        "JOIN FETCH q.candidato cand " +
                        "JOIN FETCH q.puesto " +
                        "WHERE cand.nroCandidato IN :ids " +
                        "AND q.estado IN :estados " +
                        "ORDER BY cand.apellido ASC, cand.nombre ASC",
                        Cuestionario.class)
                .setParameter("ids", ids)
                .setParameter("estados", List.of(EstadoCuestionario.ACTIVO, EstadoCuestionario.EN_PROCESO))
                .getResultList();
    }

    @Override
    public List<Cuestionario> buscarPorPuesto(Long idPuesto) {
        if (idPuesto == null) {
            return List.of();
        }
        return em.createQuery(
                        "SELECT q FROM Cuestionario q " +
                        "JOIN FETCH q.candidato " +
                        "JOIN FETCH q.puesto " +
                        "WHERE q.puesto.id = :idPuesto " +
                        "ORDER BY q.fechaCreacion DESC",
                        Cuestionario.class)
                .setParameter("idPuesto", idPuesto)
                .getResultList();
    }

    @Override
    public Cuestionario buscarPorId(Long id) {
        if (id == null) {
            return null;
        }
        return em.find(Cuestionario.class, id);
    }
}
