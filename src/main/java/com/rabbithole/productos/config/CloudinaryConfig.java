package com.rabbithole.productos.config;

import com.cloudinary.Cloudinary;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

/**
 * Configuración para la integración con Cloudinary.
 */
@Configuration
public class CloudinaryConfig {

    private final String CLOUD_NAME = "dw2ltbjkn";
    private final String API_KEY = "263495335199337";
    private final String API_SECRET = "nIg4iDmFGZ8PsQBhzwLur0yx5rw";

    /**
     * Configura el bean de Cloudinary con las credenciales.
     * 
     * @return Instancia de Cloudinary configurada
     */
    @Bean
    public Cloudinary cloudinary() {
        Map<String, String> config = new HashMap<>();
        config.put("cloud_name", CLOUD_NAME);
        config.put("api_key", API_KEY);
        config.put("api_secret", API_SECRET);
        
        return new Cloudinary(config);
    }
}
