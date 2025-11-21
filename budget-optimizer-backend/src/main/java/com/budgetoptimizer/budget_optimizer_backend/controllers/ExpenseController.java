package com.budgetoptimizer.budget_optimizer_backend.controllers;

import com.budgetoptimizer.budget_optimizer_backend.dto.expense.ExpenseDTO;
import com.budgetoptimizer.budget_optimizer_backend.dto.expense.ExpenseResponseDTO;
import com.budgetoptimizer.budget_optimizer_backend.service.ExpenseService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/expenses")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class ExpenseController {

    private final ExpenseService expenseService;

    /**
     * Crear un nuevo gasto
     * POST /api/expenses
     */
    @PostMapping
    public ResponseEntity<ExpenseResponseDTO> createExpense(@Valid @RequestBody ExpenseDTO expenseDTO) {
        ExpenseResponseDTO created = expenseService.createExpense(expenseDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    /**
     * Obtener todos los gastos de un presupuesto específico
     * GET /api/expenses/presupuesto/{presupuestoId}
     */
    @GetMapping("/presupuesto/{presupuestoId}")
    public ResponseEntity<List<ExpenseResponseDTO>> getExpensesByPresupuesto(@PathVariable Long presupuestoId) {
        List<ExpenseResponseDTO> expenses = expenseService.getExpensesByPresupuesto(presupuestoId);
        return ResponseEntity.ok(expenses);
    }

    /**
     * Obtener todos los gastos de un usuario
     * GET /api/expenses/usuario/{usuarioId}
     */
    @GetMapping("/usuario/{usuarioId}")
    public ResponseEntity<List<ExpenseResponseDTO>> getExpensesByUsuario(@PathVariable Long usuarioId) {
        List<ExpenseResponseDTO> expenses = expenseService.getExpensesByUsuario(usuarioId);
        return ResponseEntity.ok(expenses);
    }

    /**
     * Obtener gastos de un usuario en un rango de fechas
     * GET /api/expenses/usuario/{usuarioId}/rango?inicio=2024-01-01T00:00:00&fin=2024-12-31T23:59:59
     */
    @GetMapping("/usuario/{usuarioId}/rango")
    public ResponseEntity<List<ExpenseResponseDTO>> getExpensesByUsuarioAndDateRange(
            @PathVariable Long usuarioId,
            @RequestParam LocalDateTime inicio,
            @RequestParam LocalDateTime fin) {
        List<ExpenseResponseDTO> expenses = expenseService.getExpensesByUsuarioAndDateRange(usuarioId, inicio, fin);
        return ResponseEntity.ok(expenses);
    }

    /**
     * Obtener un gasto específico por ID
     * GET /api/expenses/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<ExpenseResponseDTO> getExpenseById(@PathVariable Long id) {
        ExpenseResponseDTO expense = expenseService.getExpenseById(id);
        return ResponseEntity.ok(expense);
    }

    /**
     * Actualizar un gasto existente
     * PUT /api/expenses/{id}
     */
    @PutMapping("/{id}")
    public ResponseEntity<ExpenseResponseDTO> updateExpense(
            @PathVariable Long id,
            @Valid @RequestBody ExpenseDTO expenseDTO) {
        ExpenseResponseDTO updated = expenseService.updateExpense(id, expenseDTO);
        return ResponseEntity.ok(updated);
    }

    /**
     * Eliminar un gasto
     * DELETE /api/expenses/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteExpense(@PathVariable Long id) {
        expenseService.deleteExpense(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Obtener gastos de un presupuesto por categoría
     * GET /api/expenses/presupuesto/{presupuestoId}/categoria/{categoriaId}
     */
    @GetMapping("/presupuesto/{presupuestoId}/categoria/{categoriaId}")
    public ResponseEntity<List<ExpenseResponseDTO>> getExpensesByPresupuestoAndCategoria(
            @PathVariable Long presupuestoId,
            @PathVariable Long categoriaId) {
        List<ExpenseResponseDTO> expenses = expenseService.getExpensesByPresupuestoAndCategoria(presupuestoId, categoriaId);
        return ResponseEntity.ok(expenses);
    }
}