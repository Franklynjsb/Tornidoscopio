package com.tornidoscopio.tp.dao;

import com.tornidoscopio.tp.dominio.Pregunta;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class PreguntaDAOImpl implements IPreguntaDAO {

    @PersistenceContext
    private EntityManager em;

    // CU021 - Paso 3: persiste la nueva Pregunta (el id se asigna al persistir)
    @Override
    public void crear(Pregunta p) {
        em.persist(p);
    }

    @Override
    public void modificar(Pregunta p) {
        throw new UnsupportedOperationException("No implementado en CU021");
    }

    @Override
    public void eliminar(Long id) {
        throw new UnsupportedOperationException("No implementado en CU021");
    }

    @Override
    public List<Pregunta> buscar(String competencia, String factor, String nombrePregunta) {
        throw new UnsupportedOperationException("No implementado en CU021");
    }

    // CU021 - Validación 2.C: preguntas activas del factor con el mismo nombre o el mismo enunciado
    @Override
    public List<Pregunta> buscarDuplicada(Long idFactor, String nombre, String enunciado) {
        return em.createQuery(
                        "SELECT p FROM Pregunta p " +
                        "WHERE p.factor.id = :idFactor " +
                        "AND p.activo = true " +
                        "AND (LOWER(TRIM(p.nombre)) = LOWER(TRIM(:nombre)) " +
                        "OR LOWER(TRIM(p.enunciado)) = LOWER(TRIM(:enunciado)))",
                        Pregunta.class)
                .setParameter("idFactor", idFactor)
                .setParameter("nombre", nombre)
                .setParameter("enunciado", enunciado)
                .getResultList();
    }
}
