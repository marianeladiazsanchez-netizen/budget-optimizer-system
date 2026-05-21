package com.budgetoptimizer.budget_optimizer_backend.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import com.budgetoptimizer.budget_optimizer_backend.enums.OptimizationType;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "ml_optimizations")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class MLOptimization {

    private static final ObjectMapper mapper = new ObjectMapper();

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "budget_id")
    private Presupuesto presupuesto;

    @NotBlank
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb", nullable = false)
    private String mlResponse;

    /* Ejemplo de JSON guardado:
    {
      "optimized_budget": 450,
      "recommended_categories": ["food", "transport"],
      "suggested_category_limits": {
        "food": 300,
        "transport": 150
      },
      "recommended_businesses": [
        {"id": "123", "name": "Restaurante A", "estimated_cost": 50},
        {"id": "456", "name": "Gym B", "estimated_cost": 30}
      ],
      "predicted_savings": 50,
      "alerts": ["Estás gastando 20% más en comida"],
      "confidence": 0.85,
      "model_version": "v1.2.3"
    }
    */

    @DecimalMin("0.0")
    @DecimalMax("1.0")
    @Column(precision = 3, scale = 2)
    private BigDecimal confidence;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OptimizationType tipo = OptimizationType.PREDICTION;

    @Column(nullable = false)
    private Boolean aplicada = false;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime fechaCreacion;

    /**
     * Extrae empresas sugeridas
     */
    public List<String> getEmpresasSugeridas() {

        List<String> empresaIds = new ArrayList<>();

        try {

            JsonNode node = mapper.readTree(mlResponse);

            if (node.has("recommended_businesses")) {

                JsonNode businesses = node.get("recommended_businesses");

                for (JsonNode business : businesses) {

                    if (business.has("id")) {
                        empresaIds.add(business.get("id").asText());
                    }
                }
            }

        } catch (JsonProcessingException e) {
            return new ArrayList<>();
        }

        return empresaIds;
    }

    /**
     * Extrae presupuesto optimizado
     */
    public BigDecimal getPresupuestoOptimizado() {

        try {

            JsonNode node = mapper.readTree(mlResponse);

            if (node.has("optimized_budget")) {

                return BigDecimal.valueOf(
                    node.get("optimized_budget").asDouble()
                );
            }

        } catch (JsonProcessingException e) {
            return null;
        }

        return null;
    }

    /**
     * Extrae alertas
     */
    public List<String> getAlertas() {

        List<String> alertas = new ArrayList<>();

        try {

            JsonNode node = mapper.readTree(mlResponse);

            if (node.has("alerts")) {

                JsonNode alertsNode = node.get("alerts");

                for (JsonNode alert : alertsNode) {
                    alertas.add(alert.asText());
                }
            }

        } catch (JsonProcessingException e) {
            return new ArrayList<>();
        }

        return alertas;
    }

    /**
     * Verifica si tiene alta confianza
     */
    public Boolean tieneAltaConfianza() {

        return confidence != null
            && confidence.compareTo(BigDecimal.valueOf(0.7)) >= 0;
    }

    /**
     * Verifica si fue aplicada
     */
    public Boolean fueAplicada() {

        return Boolean.TRUE.equals(aplicada);
    }
}

/* Ejemplo de JSON guardado:
    {
      "optimized_budget": 450,
      "recommended_categories": ["food", "transport"],
      "suggested_category_limits": {
        "food": 300,
        "transport": 150
      },
      "recommended_businesses": [
        {"id": "123", "name": "Restaurante A", "estimated_cost": 50},
        {"id": "456", "name": "Gym B", "estimated_cost": 30}
      ],
      "predicted_savings": 50,
      "alerts": ["Estás gastando 20% más en comida"],
      "confidence": 0.85,
      "model_version": "v1.2.3"
    }
    */