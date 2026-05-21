package com.budgetoptimizer.budget_optimizer_backend.repository;

import com.budgetoptimizer.budget_optimizer_backend.enums.TipoEmpresa;
import com.budgetoptimizer.budget_optimizer_backend.model.Empresa;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface EmpresaRepository extends JpaRepository<Empresa, String> {

    // ==========================================
    // BÚSQUEDAS BÁSICAS
    // ==========================================

    List<Empresa> findByActivaTrue();

    Optional<Empresa> findByNombre(String nombre);

    List<Empresa> findByNombreContainingIgnoreCase(String nombre);

    boolean existsByNombreIgnoreCase(String nombre);

    // ==========================================
    // FILTROS POR TIPO
    // ==========================================

    List<Empresa> findByTipoEmpresa(TipoEmpresa tipo);

    List<Empresa> findByTipoEmpresaAndActivaTrue(TipoEmpresa tipo);

    // ==========================================
    // FILTROS POR CALIFICACIÓN
    // ==========================================

    List<Empresa> findByCalificacionPromedioGreaterThanEqual(
            BigDecimal calificacion
    );

    List<Empresa> findByCalificacionPromedioBetween(
            BigDecimal min,
            BigDecimal max
    );

    // ==========================================
    // CONSULTAS GEOESPACIALES
    // ==========================================

    /**
     * Busca empresas cercanas usando fórmula de Haversine
     */
    @Query("""
        SELECT e
        FROM Empresa e
        WHERE e.activa = true
        AND (
            6371 * acos(
                cos(radians(:lat)) *
                cos(radians(e.ubicacion.latitud)) *
                cos(radians(e.ubicacion.longitud) - radians(:lng)) +
                sin(radians(:lat)) *
                sin(radians(e.ubicacion.latitud))
            )
        ) <= :radioKm
    """)
    List<Empresa> findEmpresasCercanas(
            @Param("lat") double lat,
            @Param("lng") double lng,
            @Param("radioKm") double radioKm
    );

    /**
     * Empresas cercanas filtradas por tipo
     */
    @Query("""
        SELECT e
        FROM Empresa e
        WHERE e.activa = true
        AND e.tipoEmpresa = :tipo
        AND (
            6371 * acos(
                cos(radians(:lat)) *
                cos(radians(e.ubicacion.latitud)) *
                cos(radians(e.ubicacion.longitud) - radians(:lng)) +
                sin(radians(:lat)) *
                sin(radians(e.ubicacion.latitud))
            )
        ) <= :radioKm
    """)
    List<Empresa> findEmpresasCercanasPorTipo(
            @Param("lat") double lat,
            @Param("lng") double lng,
            @Param("radioKm") double radioKm,
            @Param("tipo") TipoEmpresa tipo
    );
}