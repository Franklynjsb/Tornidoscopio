package com.tornidoscopio.tp.dao;

import com.tornidoscopio.tp.dominio.ItemOpcion;
import com.tornidoscopio.tp.dominio.Pregunta;
import com.tornidoscopio.tp.dominio.PreguntaOpcion;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;

@Repository
public class PreguntaOpcionDAOImpl implements IPreguntaOpcionDAO {

    @PersistenceContext
    private EntityManager em;

    // CU021 - asignarOpciones: persiste la ponderación de un ItemOpcion para la Pregunta.
    // La PreguntaOpcion llega con referencias que sólo tienen el id (idPregunta, idItemOpcion),
    // por eso se reemplazan por referencias gestionadas antes de persistir.
    @Override
    public void crear(PreguntaOpcion po) {
        po.setPregunta(em.getReference(Pregunta.class, po.getPregunta().getId()));
        po.setItemOpcion(em.getReference(ItemOpcion.class, po.getItemOpcion().getId()));
        em.persist(po);
    }

    @Override
    public void modificar(PreguntaOpcion po) {
        throw new UnsupportedOperationException("No implementado en CU021");
    }

    @Override
    public void eliminar(Long idPregunta, Long idItemOpcion) {
        throw new UnsupportedOperationException("No implementado en CU021");
    }
}
