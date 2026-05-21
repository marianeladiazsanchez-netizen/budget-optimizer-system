package com.budgetoptimizer.budget_optimizer_backend.model;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "reviews")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Review {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "empresa_id", nullable = false)
    private Empresa empresa;

    @NotNull
    @Min(1)
    @Max(5)
    @Column(nullable = false)
    private Integer calificacion;

    @Size(max = 1000)
    @Column(length = 1000)
    private String comentario;

    @CreationTimestamp
    @Column(updatable = false, nullable = false)
    private LocalDateTime fecha;

    @Column(nullable = false)
    private Boolean verificada = false;

    /**
     * Verifica si la review es reciente
     */
    public Boolean esReciente(LocalDateTime fechaComparacion) {

        if (fecha == null || fechaComparacion == null) {
            return false;
        }

        return fecha.isAfter(fechaComparacion);
    }

    /**
     * Verifica si es positiva
     */
    public Boolean esPositiva() {

        return calificacion != null
            && calificacion >= 4;
    }