package com.steamthreat.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

/**
 * 跨域配置。默认仅允许本地 Vue 开发服务器，可通过环境变量显式覆盖。
 */
@Configuration
public class CorsConfig {

    @Bean
    public CorsFilter corsFilter(
            @Value("${app.cors.allowed-origin:http://localhost:5173}") String allowedOrigin) {
        CorsConfiguration config = new CorsConfiguration();
        config.addAllowedOrigin(allowedOrigin);
        config.setAllowedMethods(java.util.List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(java.util.List.of("Content-Type", "Authorization"));
        config.setAllowCredentials(false);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return new CorsFilter(source);
    }
}
