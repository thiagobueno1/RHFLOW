package com.seuprojeto.rhapi.security;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;

    public SecurityConfig(JwtAuthFilter jwtAuthFilter) {
        this.jwtAuthFilter = jwtAuthFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .exceptionHandling(ex -> ex.authenticationEntryPoint((req, res, e) ->
                    res.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized")))
            .authorizeHttpRequests(auth -> auth
                    // 1) Sempre liberar OPTIONS (CORS pré-flight)
                    .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                    // 2) Páginas 100% públicas
                    .requestMatchers(
                            "/", "/index.html",
                            "/swagger-ui/**", "/v3/api-docs/**", "/swagger-ui.html",
                            "/assets/**", "/webjars/**",
                            // assinatura pública e quaisquer outras rotas abertas
                            "/public/**"
                    ).permitAll()

                    // 3) Login é público
                    .requestMatchers("/auth/login").permitAll()

                    // 4) Regras de negócio protegidas
                    .requestMatchers(HttpMethod.POST, "/colaboradores/**").hasAnyRole("RH","ADMIN")
                    .requestMatchers("/banco-horas/**").hasAnyRole("RH","ADMIN")
                    .requestMatchers("/relatorios/email/**").hasAnyRole("RH","ADMIN")

                    // (somente rotas administrativas de assinatura; a página pública está em /public/**)
                    .requestMatchers("/assinaturas/**").hasAnyRole("RH","ADMIN")

                    // Batida de ponto rápida + status do dia
                    .requestMatchers(HttpMethod.POST, "/pontos/bater").hasAnyRole("COLABORADOR","GESTOR","ADMIN")
                    .requestMatchers(HttpMethod.GET, "/pontos/status-dia").authenticated()

                    // 5) Todo o resto precisa estar autenticado
                    .anyRequest().authenticated()
            )
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() { return new BCryptPasswordEncoder(); }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration cfg = new CorsConfiguration();
        cfg.setAllowedOriginPatterns(List.of("*"));
        cfg.setAllowedMethods(List.of("GET","POST","PUT","PATCH","DELETE","OPTIONS"));
        cfg.setAllowedHeaders(List.of("*"));
        cfg.setExposedHeaders(List.of("Authorization"));
        cfg.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", cfg);
        return source;
    }
}
