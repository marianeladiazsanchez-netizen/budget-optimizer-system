package com.budgetoptimizer.budget_optimizer_backend.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Controlador mínimo para la raíz del servidor.
 * Devuelve un JSON simple para comprobar que el backend responde en /
 */
@RestController
public class RootController {

    @GetMapping("/")
    public ResponseEntity<Map<String, String>> root() {
        return ResponseEntity.ok(Map.of(
                "status", "ok",
                "service", "budget-optimizer-backend"
        ));
    }

}
