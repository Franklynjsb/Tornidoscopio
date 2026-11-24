package com.tornidoscopio.tp.dao;

import com.tornidoscopio.tp.dominio.ItemOpcion;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class ItemOpcionDAOImpl implements IItemOpcionDAO {

    @PersistenceContext
    private EntityManager em;

    @Override
    public void crear(ItemOpcion i) {
        throw new UnsupportedOperationException("No implementado en CU021");
    }

    @Override
    public void modificar(ItemOpcion i) {
        throw new UnsupportedOperationException("No implementado en CU021");
    }

    @Override
    public void eliminar(Long id) {
        throw new UnsupportedOperationException("No implementado en CU021");
    }

    // CU021 - armarOpcionRespuestaDTO: ítems de la OpcionRespuesta ordenados para la grilla
    @Override
    public List<ItemOpcion> buscarPorOpcion(Long idOpcion) {
        return em.createQuery(
                        "SELECT i FROM ItemOpcion i " +
                        "WHERE i.opcionRespuesta.id = :idOpcion " +
                        "ORDER BY i.ordenVisualizacion",
                        ItemOpcion.class)
                .setParameter("idOpcion", idOpcion)
                .getResultList();
    }
}
