package com.tornidoscopio.tp.dao;

import com.tornidoscopio.tp.dominio.Candidato;
import com.tornidoscopio.tp.dominio.TipoDocumento;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Implementación JPA del patrón DAO para la entidad {@link Candidato}.
 */
@Repository
public class CandidatoDAOImpl implements ICandidatoDAO {

    @PersistenceContext
    private EntityManager em;

    @Override
    public void crear(Candidato c) {
        em.persist(c);
    }

    @Override
    public void modificar(Candidato c) {
        em.merge(c);
    }

    @Override
    public void eliminar(Long id) {
        Candidato c = em.find(Candidato.class, id);
        if (c != null) {
            c.setEliminado(true);
            em.merge(c);
        }
    }

    /**
     * CU025 - Busca candidatos no eliminados cuyo apellido, nombre, nombre completo
     * o número de candidato contenga el criterio indicado (sin distinguir mayúsculas).
     * Si el criterio es nulo o vacío, retorna todos los candidatos activos ordenados
     * por apellido y nombre.
     */
    @Override
    public List<Candidato> buscar(String criterio) {
        if (criterio == null || criterio.isBlank()) {
            return em.createQuery(
                            "SELECT c FROM Candidato c " +
                            "WHERE c.eliminado = false " +
                            "ORDER BY c.apellido ASC, c.nombre ASC",
                            Candidato.class)
                    .getResultList();
        }

        String patron = "%" + criterio.trim().toLowerCase() + "%";

        return em.createQuery(
                        "SELECT c FROM Candidato c " +
                        "WHERE c.eliminado = false " +
                        "AND (LOWER(c.apellido) LIKE :patron " +
                        "OR LOWER(c.nombre) LIKE :patron " +
                        "OR LOWER(CONCAT(c.apellido, ' ', c.nombre)) LIKE :patron " +
                        "OR LOWER(CONCAT(c.nombre, ' ', c.apellido)) LIKE :patron " +
                        "OR CAST(c.nroCandidato AS string) LIKE :patron) " +
                        "ORDER BY c.apellido ASC, c.nombre ASC",
                        Candidato.class)
                .setParameter("patron", patron)
                .getResultList();
    }

    @Override
    public Candidato buscarPorDocumento(TipoDocumento t, String n) {
        if (t == null || n == null || n.isBlank()) {
            return null;
        }
        return em.createQuery(
                        "SELECT c FROM Candidato c " +
                        "WHERE c.eliminado = false " +
                        "AND c.tipoDocumento = :tipo " +
                        "AND c.nroDocumento = :numero",
                        Candidato.class)
                .setParameter("tipo", t)
                .setParameter("numero", n.trim())
                .getResultStream()
                .findFirst()
                .orElse(null);
    }

    /**
     * CU025 - Recupera un candidato por su número único de candidato.
     */
    @Override
    public Candidato buscarPorNroCandidato(Long n) {
        if (n == null) {
            return null;
        }
        return em.find(Candidato.class, n);
    }
}
