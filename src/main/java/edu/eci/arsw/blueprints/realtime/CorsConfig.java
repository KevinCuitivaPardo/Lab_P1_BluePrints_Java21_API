package edu.eci.arsw.blueprints.realtime;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/** Permite al front de desarrollo (Vite) consumir la API REST. */
@Configuration
public class CorsConfig implements WebMvcConfigurer {

    static final String[] ORIGINS = {"http://localhost:5173", "http://127.0.0.1:5173"};

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**").allowedOrigins(ORIGINS).allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS");
    }
}
