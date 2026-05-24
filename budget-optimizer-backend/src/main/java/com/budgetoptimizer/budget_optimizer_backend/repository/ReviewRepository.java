package com.budgetoptimizer.budget_optimizer_backend.repository;

import com.budgetoptimizer.budget_optimizer_backend.model.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {

    // ==========================================
    // CONSULTAS POR EMPRESA
    // ==========================================

    List<Review> findByEmpresa_Id(String empresaId);

    List<Review> findByEmpresa_IdAndVerificadaTrue(String empresaId);

    long countByEmpresa_Id(String empresaId);

    long countByEmpresa_IdAndVerificadaTrue(String empresaId);

    // ==========================================
    // CONSULTAS POR USUARIO
    // ==========================================

    List<Review> findByUsuario_Id(Long usuarioId);

    // ==========================================
    // CONSULTAS POR CALIFICACIÓN
    // ==========================================

    List<Review> findByCalificacionGreaterThanEqual(Integer calificacion);

    List<Review> findByCalificacion(Integer calificacion);

    // ==========================================
    // CONSULTAS POR FECHA
    // ==========================================

    List<Review> findByFechaAfter(LocalDateTime fecha);

    List<Review> findByEmpresaIdAndFechaAfter(
            String empresaId,
            LocalDateTime fecha
    );

    // ==========================================
    // CONSULTAS POR COMENTARIO
    // ==========================================

    List<Review> findByComentarioContainingIgnoreCase(String keyword);

    // ==========================================
    // AGREGACIONES
    // ==========================================

    /**
     * Calcula promedio de calificaciones de una empresa
     */
    @Query("""
        SELECT COALESCE(AVG(r.calificacion), 0)
        FROM Review r
        WHERE r.empresa.id = :empresaId
    """)
    Double calcularPromedioCalificacion(
            @Param("empresaId") String empresaId
    );
}