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

import jakarta.validation.constraints.Min;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "historial_busquedas")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class HistorialBusqueda {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime fecha;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "empresa_id")
    private Empresa empresaSeleccionada;

    @Column(length = 1000)
    private String filtrosUsados;

    @Min(0)
    private Integer resultadosEncontrados;

    /**
     * Verifica si la búsqueda es reciente
     */
    public Boolean esReciente(LocalDateTime fechaComparacion) {

        if (fechaComparacion == null || this.fecha == null) {
            return false;
        }

        return this.fecha.isAfter(fechaComparacion);
    }
}