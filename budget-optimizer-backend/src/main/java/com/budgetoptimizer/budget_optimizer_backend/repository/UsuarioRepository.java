package com.budgetoptimizer.budget_optimizer_backend.repository;

import com.budgetoptimizer.budget_optimizer_backend.enums.AccountType;
import com.budgetoptimizer.budget_optimizer_backend.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    // ==========================================
    // BÚSQUEDAS BÁSICAS
    // ==========================================

    Optional<Usuario> findByEmail(String email);

    boolean existsByEmail(String email);

    List<Usuario> findByNombreContainingIgnoreCase(String texto);

    // ==========================================
    // CONSULTAS POR TIPO DE CUENTA
    // ==========================================

    List<Usuario> findByAccountType(AccountType accountType);

    List<Usuario> findByAccountTypeIn(List<AccountType> types);

    List<Usuario> findByAccountTypeAndActivoTrue(AccountType type);

    long countByAccountType(AccountType accountType);

    // ==========================================
    // CONSULTAS POR ESTADO
    // ==========================================

    List<Usuario> findByActivoTrue();

    List<Usuario> findByActivoFalse();

    long countByActivoTrue();

    // ==========================================
    // CONSULTAS POR PRESUPUESTO
    // ==========================================

    List<Usuario> findByPresupuestoMensualBaseGreaterThan(
            BigDecimal monto
    );

    List<Usuario> findByPresupuestoMensualBaseBetween(
            BigDecimal min,
            BigDecimal max
    );

    List<Usuario> findByPresupuestoMensualBaseLessThan(
            BigDecimal monto
    );

    // ==========================================
    // CONSULTAS POR FECHAS
    // ==========================================

    List<Usuario> findByFechaCreacionAfter(LocalDateTime fecha);

    List<Usuario> findByFechaCreacionBetween(
            LocalDateTime inicio,
            LocalDateTime fin
    );

    List<Usuario> findByUltimoAccesoAfter(LocalDateTime fecha);

    // ==========================================
    // ORDENAMIENTO
    // ==========================================

    List<Usuario> findTop10ByActivoTrueOrderByPresupuestoMensualBaseDesc();

    List<Usuario> findByActivoTrueOrderByFechaCreacionDesc();

    // ==========================================
    // CONSULTAS GEOESPACIALES
    // ==========================================

    /**
     * Busca usuarios cercanos usando fórmula Haversine
     */
    @Query("""
        SELECT u
        FROM Usuario u
        WHERE
        6371 * acos(
            cos(radians(:lat)) *
            cos(radians(u.ubicacion.latitud)) *
            cos(radians(u.ubicacion.longitud) - radians(:lng)) +
            sin(radians(:lat)) *
            sin(radians(u.ubicacion.latitud))
        ) <= :radioKm
        AND u.activo = true
    """)
    List<Usuario> findUsuariosCercanos(
            @Param("lat") double latitud,
            @Param("lng") double longitud,
            @Param("radioKm") double radioKm
    );

    // ==========================================
    // CONSULTAS PERSONALIZADAS
    // ==========================================

    /**
     * Usuarios activos sin presupuestos
     */
    @Query("""
        SELECT u
        FROM Usuario u
        WHERE u.activo = true
        AND SIZE(u.presupuestos) = 0
    """)
    List<Usuario> findUsuariosSinPresupuestos();
}