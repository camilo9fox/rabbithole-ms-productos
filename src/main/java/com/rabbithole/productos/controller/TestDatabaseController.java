package com.rabbithole.productos.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * Controlador para probar la conexión a la base de datos.
 * Este controlador proporciona endpoints para verificar que la conexión
 * a Oracle Cloud con el wallet funciona correctamente.
 */
@RestController
@RequestMapping("/test")
public class TestDatabaseController {

    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public TestDatabaseController(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * Endpoint para probar la conexión a la base de datos.
     * 
     * @return Estado de la conexión
     */
    @GetMapping("/database")
    public ResponseEntity<Map<String, Object>> testConnection() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            // Intentar ejecutar una consulta simple
            String dbTime = jdbcTemplate.queryForObject(
                "SELECT TO_CHAR(SYSDATE, 'DD-MON-YYYY HH24:MI:SS') AS DB_TIME FROM DUAL", 
                String.class
            );
            
            // Contar los colores en la base de datos
            Integer colorCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM colores", 
                Integer.class
            );
            
            // Si llegamos aquí, la conexión fue exitosa
            response.put("status", "success");
            response.put("message", "Conexión a la base de datos establecida correctamente");
            response.put("databaseTime", dbTime);
            response.put("colorCount", colorCount != null ? colorCount : 0);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            // Si hay algún error, devolver información sobre el problema
            response.put("status", "error");
            response.put("message", "Error al conectar a la base de datos");
            response.put("errorDetails", e.getMessage());
            
            return ResponseEntity.status(500).body(response);
        }
    }
}
