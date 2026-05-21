package com.budgetoptimizer.budget_optimizer_backend.model;

import java.time.LocalDateTime;
import jakarta.persistence.Column;
import java.math.BigDecimal;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.RoundingMode;


@Getter
@Setter
@Entity
@Table(name = "category_limits")
@AllArgsConstructor
@NoArgsConstructor
public class CategoryLimit {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "presupuesto_id", nullable = false)
    private Presupuesto presupuesto;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "categoria_id", nullable = false)
    private Categoria categoria;
    
    @NotNull
    @Positive
    @Column(nullable = false)
    private BigDecimal limiteAsignado;
    
    @Column(nullable = false)
    private BigDecimal gastoActual = BigDecimal.ZERO;
    
    @CreationTimestamp
    private LocalDateTime fechaCreacion;

    @UpdateTimestamp
    private LocalDateTime fechaActualizacion;
    
    
    // Métodos helper

     /**
     * Calcula el monto restante del límite
     */
    public BigDecimal calcularRestante() {
        return limiteAsignado.subtract(gastoActual);
    }
    
    /**
     * Calcula el porcentaje usado del límite
     */
    public BigDecimal calcularPorcentajeUsado() {
        if ((limiteAsignado == null || limiteAsignado.compareTo(BigDecimal.ZERO) == 0)) {
            return BigDecimal.ZERO;
        }
        return gastoActual
        .multiply(BigDecimal.valueOf(100))
        .divide(limiteAsignado, 2, RoundingMode.HALF_UP);
    }
    
    /**
     * Verifica si está cerca del límite (>= 80%)
     */
    public Boolean estaCercaDelLimite() {
       return calcularPorcentajeUsado()
           .compareTo(BigDecimal.valueOf(80)) >= 0;
    }
    
    /**
     * Verifica si ha excedido el límite
     */
    public Boolean haExcedidoLimite() {
       return gastoActual.compareTo(limiteAsignado) > 0;
    }
    
    /**
     * Agrega un gasto al total actual
     */
    public void agregarGasto(BigDecimal monto) {
        if (monto != null && monto.compareTo(BigDecimal.ZERO) > 0) {
           this.gastoActual = this.gastoActual.add(monto);
        }
    }
    
    /**
     * Resta un gasto del total actual (para cuando se elimina un gasto)
     */
    public void restarGasto(BigDecimal monto) {
        if (monto != null && monto.compareTo(BigDecimal.ZERO) <= 0) {
           this.gastoActual = this.gastoActual.subtract(monto);

           if (this.gastoActual.compareTo(BigDecimal.ZERO) < 0) {
               this.gastoActual = BigDecimal.ZERO;
}
        }
    }
    
    /**
     * Verifica si puede agregar un gasto sin exceder el límite
     */
    public Boolean puedeAgregarGasto(BigDecimal monto) {
        if (monto == null || monto.compareTo(BigDecimal.ZERO) <= 0) {
            return false;
        }
        return gastoActual.add(monto).compareTo(limiteAsignado) <= 0;
    }
    
    /**
     * Calcula cuánto falta para alcanzar el límite
     */
    public BigDecimal cuantoFaltaParaLimite() {
       BigDecimal restante = limiteAsignado.subtract(gastoActual);

       return restante.compareTo(BigDecimal.ZERO) > 0
           ? restante
           : BigDecimal.ZERO;
    }
}

