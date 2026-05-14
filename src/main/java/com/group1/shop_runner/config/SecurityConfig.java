package com.group1.shop_runner.config;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
public class SecurityConfig {
    @Autowired
    private JwtFilter jwtFilter;
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(csrf -> csrf.disable())
                .httpBasic(httpBasic -> httpBasic.disable())
                .formLogin(form -> form.disable())
                .authorizeHttpRequests(auth -> auth

                        // Public
                        .requestMatchers("/auth/**").permitAll()

                        .requestMatchers(HttpMethod.GET, "/api/v1/products/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/categories/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/brands/**").permitAll()

                        // Cart
                        .requestMatchers("/api/v1/cart/**")
                        .hasAnyRole("USER", "ADMIN")

                        // Customer profile
                        .requestMatchers("/api/v1/customer-profiles/**")
                        .hasAnyRole("USER", "ADMIN")

                        // Orders
                        .requestMatchers(HttpMethod.POST, "/api/v1/orders/**")
                        .hasAnyRole("USER", "ADMIN")

                        .requestMatchers(HttpMethod.GET, "/api/v1/orders/**")
                        .hasAnyRole("USER", "ADMIN")

                        // Product management
                        .requestMatchers(HttpMethod.POST, "/api/v1/products/**")
                        .hasRole("ADMIN")

                        .requestMatchers(HttpMethod.PUT, "/api/v1/products/**")
                        .hasRole("ADMIN")

                        .requestMatchers(HttpMethod.DELETE, "/api/v1/products/**")
                        .hasRole("ADMIN")

                        // Category management
                        .requestMatchers(HttpMethod.POST, "/api/v1/categories/**")
                        .hasRole("ADMIN")

                        .requestMatchers(HttpMethod.PUT, "/api/v1/categories/**")
                        .hasRole("ADMIN")

                        // Admin APIs
                        .requestMatchers("/api/v1/admin/**")
                        .hasRole("ADMIN")

                        .requestMatchers(HttpMethod.OPTIONS, "/**")
                        .permitAll()

                        .anyRequest().authenticated()

                ).exceptionHandling(ex -> ex
                        .authenticationEntryPoint((request, response, authException) -> {
                            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                        })
                )
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);


        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of("http://localhost:3000", "http://localhost:5173"));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

}