package com.tornidoscopio.tp.dao;

import com.tornidoscopio.tp.dominio.Pregunta;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class PreguntaDAOImpl implements IPreguntaDAO {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public void crear(Pregunta p) {
        entityManager.persist(p);
    }

    @Override
    public void modificar(Pregunta p) {
        entityManager.merge(p);
    }

    @Override
    public void eliminar(Long id) {
        Pregunta p = entityManager.find(Pregunta.class, id);
        if (p != null) {
            p.setActivo(false);
            entityManager.merge(p);
        }
    }

    @Override
    public List<Pregunta> buscar(String competencia, String factor, String nombrePregunta) {
        String compFiltro = (competencia == null || competencia.trim().isEmpty()) ? "" : competencia.trim().toLowerCase();
        String facFiltro = (factor == null || factor.trim().isEmpty()) ? "" : factor.trim().toLowerCase();
        String pregFiltro = (nombrePregunta == null || nombrePregunta.trim().isEmpty()) ? "" : nombrePregunta.trim().toLowerCase();

        String jpql = """
                SELECT p FROM Pregunta p
                JOIN FETCH p.factor f
                JOIN FETCH f.competencia c
                WHERE p.activo = true
                  AND f.activo = true
                  AND c.activo = true
                  AND (:comp = '' OR LOWER(c.nombre) LIKE CONCAT('%', :comp, '%'))
                  AND (:fac = '' OR LOWER(f.nombre) LIKE CONCAT('%', :fac, '%'))
                  AND (:preg = '' OR LOWER(p.nombre) LIKE CONCAT('%', :preg, '%'))
                ORDER BY p.nombre ASC
                """;

        TypedQuery<Pregunta> query = entityManager.createQuery(jpql, Pregunta.class);
        query.setParameter("comp", compFiltro);
        query.setParameter("fac", facFiltro);
        query.setParameter("preg", pregFiltro);

        return query.getResultList();
    }

    @Override
    public List<Pregunta> buscarDuplicada(Long idFactor, String nombre, String enunciado) {
        String jpql = """
                SELECT p FROM Pregunta p
                WHERE p.activo = true
                  AND p.factor.id = :idFactor
                  AND (LOWER(TRIM(p.nombre)) = LOWER(:nombre) OR LOWER(TRIM(p.enunciado)) = LOWER(:enunciado))
                """;

        return entityManager.createQuery(jpql, Pregunta.class)
                .setParameter("idFactor", idFactor)
                .setParameter("nombre", nombre != null ? nombre.trim() : "")
                .setParameter("enunciado", enunciado != null ? enunciado.trim() : "")
                .getResultList();
    }
}