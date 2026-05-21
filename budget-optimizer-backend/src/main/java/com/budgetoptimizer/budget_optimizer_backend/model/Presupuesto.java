package com.budgetoptimizer.budget_optimizer_backend.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.*;

import org.hibernate.annotations.CreationTimestamp;

import com.budgetoptimizer.budget_optimizer_backend.enums.BudgetPeriod;
import com.budgetoptimizer.budget_optimizer_backend.enums.BudgetStatus;
import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "presupuestos")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Presupuesto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @NotBlank
    @Column(nullable = false, length = 100)
    private String nombre;

    @NotNull
    @Positive
    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal montoTotal;

    @NotNull
    @Column(nullable = false)
    private LocalDateTime fechaInicio;

    @NotNull
    @Column(nullable = false)
    private LocalDateTime fechaFin;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private BudgetPeriod periodo = BudgetPeriod.MONTHLY;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private BudgetStatus status = BudgetStatus.ACTIVE;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime fechaCreacion;

    @OneToMany(
        mappedBy = "presupuesto",
        cascade = CascadeType.ALL,
        orphanRemoval = true
    )
    @Builder.Default
    private List<CategoryLimit> limitesCategorias = new ArrayList<>();

    @OneToMany(
        mappedBy = "presupuesto",
        cascade = CascadeType.ALL,
        orphanRemoval = true
    )
    @Builder.Default
    private List<Expense> expenses = new ArrayList<>();

    /**
     * Calcula gasto total
     */
    public BigDecimal calcularGastoTotal() {

        return expenses.stream()
            .map(Expense::getMonto)
            .filter(monto -> monto != null)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * Calcula presupuesto restante
     */
    public BigDecimal calcularPresupuestoRestante() {

        return montoTotal.subtract(calcularGastoTotal());
    }

    /**
     * Verifica si está sobre presupuesto
     */
    public Boolean estaSobrePresupuesto() {

        return calcularGastoTotal()
            .compareTo(montoTotal) > 0;
    }

}