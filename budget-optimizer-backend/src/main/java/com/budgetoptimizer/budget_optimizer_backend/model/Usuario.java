package com.budgetoptimizer.budget_optimizer_backend.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import com.budgetoptimizer.budget_optimizer_backend.enums.AccountType;
import com.budgetoptimizer.budget_optimizer_backend.exception.LimiteTransaccionesException;
import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.AttributeOverrides;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "usuarios")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Usuario {

    // ==========================================
    // CLAVE PRIMARIA
    // ==========================================

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ==========================================
    // INFORMACIÓN BÁSICA
    // ==========================================

    @Column(nullable = false, length = 100)
    private String nombre;

    @Column(unique = true, nullable = false, length = 150)
    private String email;

    @Column(nullable = false)
    @JsonIgnore
    private String password;

    @Column(length = 20)
    private String telefono;

    // ==========================================
    // UBICACIÓN
    // ==========================================

    @Embedded
    @AttributeOverrides({
        @AttributeOverride(
            name = "latitud",
            column = @Column(name = "latitud")
        ),
        @AttributeOverride(
            name = "longitud",
            column = @Column(name = "longitud")
        )
    })
    private Coordenada ubicacion;

    @Column(length = 100)
    private String ciudad;

    @Column(length = 100)
    private String pais;

    // ==========================================
    // TIPO DE CUENTA
    // ==========================================

    @Enumerated(EnumType.STRING)
    @Column(name = "account_type", nullable = false)
    @Builder.Default
    private AccountType accountType = AccountType.USER;

    // ==========================================
    // PRESUPUESTO Y TRANSACCIONES
    // ==========================================

    @Column(
        name = "presupuesto_mensual_base",
        nullable = false,
        precision = 15,
        scale = 2
    )
    private BigDecimal presupuestoMensualBase;

    @Column(name = "transacciones_mes_actual")
    @Builder.Default
    private Integer transaccionesMesActual = 0;

    // ==========================================
    // ESTADO
    // ==========================================

    @Column(nullable = false)
    @Builder.Default
    private Boolean activo = true;

    // ==========================================
    // FECHAS
    // ==========================================

    @CreationTimestamp
    @Column(
        name = "fecha_creacion",
        nullable = false,
        updatable = false
    )
    private LocalDateTime fechaCreacion;

    @UpdateTimestamp
    @Column(name = "fecha_modificacion")
    private LocalDateTime fechaModificacion;

    @Column(name = "ultimo_acceso")
    private LocalDateTime ultimoAcceso;

    // ==========================================
    // RELACIONES
    // ==========================================

    @OneToMany(
        mappedBy = "usuario",
        cascade = CascadeType.ALL,
        orphanRemoval = true,
        fetch = FetchType.LAZY
    )
    @JsonIgnore
    @Builder.Default
    private List<Presupuesto> presupuestos = new ArrayList<>();

    @OneToMany(
        mappedBy = "usuario",
        cascade = CascadeType.ALL,
        orphanRemoval = true,
        fetch = FetchType.LAZY
    )
    @JsonIgnore
    @Builder.Default
    private List<Expense> expenses = new ArrayList<>();

    @OneToMany(
        mappedBy = "usuario",
        cascade = CascadeType.ALL,
        orphanRemoval = true,
        fetch = FetchType.LAZY
    )
    @JsonIgnore
    @Builder.Default
    private List<MLOptimization> mlOptimizations = new ArrayList<>();

    @OneToMany(
        mappedBy = "usuario",
        cascade = CascadeType.ALL,
        orphanRemoval = true,
        fetch = FetchType.LAZY
    )
    @JsonIgnore
    @Builder.Default
    private List<Review> reviews = new ArrayList<>();

    @OneToMany(
        mappedBy = "usuario",
        cascade = CascadeType.ALL,
        orphanRemoval = true,
        fetch = FetchType.LAZY
    )
    @JsonIgnore
    @Builder.Default
    private List<HistorialBusqueda> historialBusquedas = new ArrayList<>();

    @OneToOne(
        mappedBy = "usuario",
        cascade = CascadeType.ALL,
        orphanRemoval = true,
        fetch = FetchType.LAZY
    )
    @JsonIgnore
    private Cuenta cuenta;

    // ==========================================
    // MÉTODOS DE NEGOCIO
    // ==========================================

    /**
     * Valida si un monto está dentro del presupuesto base
     */
    public boolean validarPresupuesto(BigDecimal monto) {
        if (monto == null || presupuestoMensualBase == null) {
            return false;
        }

        return monto.compareTo(presupuestoMensualBase) <= 0;
    }

    /**
     * Establece la ubicación del usuario
     */
    public void establecerUbicacion(Coordenada coordenada) {
        this.ubicacion = coordenada;
    }

    /**
     * Verifica si el usuario puede realizar otra transacción
     */
    public Boolean puedeRealizarTransaccion() {
        return accountType.puedeRealizarTransaccion(
            transaccionesMesActual
        );
    }

    /**
     * Registra una transacción
     */
    public void registrarTransaccion() {

        if (!puedeRealizarTransaccion()) {
            throw new LimiteTransaccionesException(
                accountType,
                transaccionesMesActual
            );
        }

        this.transaccionesMesActual++;
    }

    /**
     * Reinicia el contador mensual
     */
    public void reiniciarContadorMensual() {
        this.transaccionesMesActual = 0;
    }

    /**
     * Actualiza último acceso
     */
    public void actualizarUltimoAcceso() {
        this.ultimoAcceso = LocalDateTime.now();
    }

    /**
     * Upgrade de cuenta
     */
    public void upgradeCuenta(AccountType nuevoTipo) {

        if (nuevoTipo == null) {
            throw new IllegalArgumentException(
                "El tipo de cuenta no puede ser null"
            );
        }

        if (!nuevoTipo.esSuperiorA(this.accountType)) {
            throw new IllegalArgumentException(
                "El nuevo tipo de cuenta debe ser superior al actual"
            );
        }

        this.accountType = nuevoTipo;
    }
}git 