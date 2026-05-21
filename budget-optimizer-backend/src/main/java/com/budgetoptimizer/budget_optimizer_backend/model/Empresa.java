package com.budgetoptimizer.budget_optimizer_backend.model;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.hibernate.annotations.CreationTimestamp;

import com.budgetoptimizer.budget_optimizer_backend.enums.TipoEmpresa;
import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "empresas")
@AllArgsConstructor
@NoArgsConstructor
public class Empresa {

    @Id
    @Column(unique = true, nullable = false, length = 36)
    private String id;

    @NotBlank
    @Column(nullable = false, length = 200)
    private String nombre;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoEmpresa tipoEmpresa;

    /**
     * Categorías asociadas a la empresa
     */
    @ManyToMany
    @JoinTable(
        name = "empresa_categorias",
        joinColumns = @JoinColumn(name = "empresa_id"),
        inverseJoinColumns = @JoinColumn(name = "categoria_id")
    )
    private List<Categoria> categorias = new ArrayList<>();

    @Embedded
    private Coordenada ubicacion;

    @Embedded
    private RangoPrecios rangoPrecios;

    @Column(precision = 3, scale = 2)
    private BigDecimal calificacionPromedio;

    @JsonIgnore
    @OneToMany(
        mappedBy = "empresa",
        cascade = CascadeType.ALL,
        orphanRemoval = true
    )
    private List<Review> reviews = new ArrayList<>();

    @JsonIgnore
    @OneToMany(mappedBy = "empresa")
    private List<Expense> expenses = new ArrayList<>();

    @Column(nullable = false)
    private Boolean activa = true;

    @CreationTimestamp
    private LocalDateTime fechaCreacion;

    /**
     * Genera UUID automáticamente
     */
    @PrePersist
    public void generarId() {

        if (this.id == null) {
            this.id = UUID.randomUUID().toString();
        }
    }

    /**
     * Calcula distancia hacia otra coordenada
     */
    public Double calcularDistanciaA(Coordenada otraUbicacion) {

        if (this.ubicacion == null || otraUbicacion == null) {
            return 0.0;
        }

        return this.ubicacion.distanciaA(otraUbicacion);
    }

    /**
     * Verifica si cumple con un presupuesto dado
     */
    public Boolean cumpleConPresupuesto(BigDecimal presupuesto) {

        if (this.rangoPrecios == null || presupuesto == null) {
            return false;
        }

        return this.rangoPrecios.esAccesible(presupuesto);
    }

    /**
     * Actualiza calificación promedio basada en reviews
     */
    public void actualizarCalificacionPromedio() {

        if (reviews == null || reviews.isEmpty()) {
            this.calificacionPromedio = BigDecimal.ZERO;
            return;
        }

        BigDecimal suma = BigDecimal.ZERO;

        for (Review review : reviews) {

            if (review.getCalificacion() != null) {
                suma = suma.add(
                    BigDecimal.valueOf(review.getCalificacion())
                );
            }
        }

        this.calificacionPromedio = suma.divide(
            BigDecimal.valueOf(reviews.size()),
            2,
            RoundingMode.HALF_UP
        );
    }
}