package com.budgetoptimizer.budget_optimizer_backend.model;

import java.math.BigDecimal;
import java.math.RoundingMode;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RangoPrecios {

@NotNull
@DecimalMin("0.0")
@Column(nullable = false, precision = 15, scale = 2)
private BigDecimal minimo;

@NotNull
@DecimalMin("0.0")
@Column(nullable = false, precision = 15, scale = 2)
private BigDecimal maximo;
@NotNull
@DecimalMin("0.0")
@Column(nullable = false, precision = 15, scale = 2)
private BigDecimal promedio;

    /**
     * Verifica si un precio es accesible
     */
    public Boolean esAccesible(BigDecimal precio) {

        if (precio == null) {
            return false;
        }

        return precio.compareTo(minimo) >= 0
            && precio.compareTo(maximo) <= 0;
    }
}
