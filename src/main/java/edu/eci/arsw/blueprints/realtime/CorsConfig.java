package edu.eci.arsw.blueprints.realtime;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/** Permite al front de desarrollo (Vite) consumir la API REST, desde localhost o la red local. */
@Configuration
public class CorsConfig implements WebMvcConfigurer {

    // En producción: reemplazar por el dominio real del front.
    static final String[] ORIGIN_PATTERNS = {
            "http://localhost:*", "http://127.0.0.1:*",
            "http://192.168.*:*", "http://10.*:*", "http://172.*:*"
    };

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**").allowedOriginPatterns(ORIGIN_PATTERNS)
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS");
    }
}
