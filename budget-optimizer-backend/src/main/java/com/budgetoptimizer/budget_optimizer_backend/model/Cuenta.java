package com.budgetoptimizer.budget_optimizer_backend.model;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import jakarta.persistence.FetchType;

import org.hibernate.annotations.CreationTimestamp;

import com.budgetoptimizer.budget_optimizer_backend.enums.AccountType;
import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "cuentas")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Cuenta {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @JsonIgnore
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;
    
    @NotNull
    @PositiveOrZero
    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal saldo = BigDecimal.ZERO;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AccountType tipoCuenta;
    
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "cuenta_preferencias",
        joinColumns = @JoinColumn(name = "cuenta_id"),
        inverseJoinColumns = @JoinColumn(name = "categoria_id")
    )
    private Set<Categoria> preferencias = new HashSet<>();

    @CreationTimestamp
    private LocalDateTime fechaCreacion;

    /**
     * Suscribirse a una categoría
     */
    public void suscribirseACategoria(Categoria categoria) {

        if (categoria != null) {
            this.preferencias.add(categoria);
        }
    }

    /**
     * Remover categoría preferida
     */
    public void removerCategoria(Categoria categoria) {

        if (categoria != null) {
            this.preferencias.remove(categoria);
        }
    }

    /**
     * Calcula presupuesto disponible basado en preferencias
     */
    public BigDecimal calcularPresupuestoDisponible() {

        BigDecimal factorPreferencias =
                BigDecimal.valueOf(
                        1 + (this.preferencias.size() * 0.05)
                );

        return this.saldo.multiply(factorPreferencias)
                .setScale(2, RoundingMode.HALF_UP);
    }
}