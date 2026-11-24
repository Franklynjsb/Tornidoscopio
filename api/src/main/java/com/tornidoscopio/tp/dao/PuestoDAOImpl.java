package com.tornidoscopio.tp.dao;

import com.tornidoscopio.tp.dominio.Puesto;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Implementación JPA del patrón DAO para la entidad {@link Puesto}.
 */
@Repository
public class PuestoDAOImpl implements IPuestoDAO {

    @PersistenceContext
    private EntityManager em;

    @Override
    public void crear(Puesto p) {
        em.persist(p);
    }

    @Override
    public void modificar(Puesto p) {
        em.merge(p);
    }

    @Override
    public void eliminar(Long id) {
        Puesto p = em.find(Puesto.class, id);
        if (p != null) {
            p.setActivo(false);
            em.merge(p);
        }
    }

    /**
     * CU025 - Paso 8: busca puestos activos cuyo código, nombre o nombre de empresa
     * contenga el criterio indicado. Si el criterio es nulo o vacío, retorna todos
     * los puestos activos ordenados por nombre.
     */
    @Override
    public List<Puesto> buscar(String criterio) {
        if (criterio == null || criterio.isBlank()) {
            return em.createQuery(
                            "SELECT p FROM Puesto p " +
                            "JOIN FETCH p.empresa e " +
                            "WHERE p.activo = true " +
                            "ORDER BY p.nombre ASC",
                            Puesto.class)
                    .getResultList();
        }

        String patron = "%" + criterio.trim().toLowerCase() + "%";

        return em.createQuery(
                        "SELECT p FROM Puesto p " +
                        "JOIN FETCH p.empresa e " +
                        "WHERE p.activo = true " +
                        "AND (LOWER(p.codigo) LIKE :patron " +
                        "OR LOWER(p.nombre) LIKE :patron " +
                        "OR LOWER(e.nombre) LIKE :patron) " +
                        "ORDER BY p.nombre ASC",
                        Puesto.class)
                .setParameter("patron", patron)
                .getResultList();
    }

    /**
     * CU025 - Paso 9: recupera un puesto por su identificador.
     */
    @Override
    public Puesto buscarPorId(Long id) {
        if (id == null) {
            return null;
        }
        return em.find(Puesto.class, id);
    }
}
