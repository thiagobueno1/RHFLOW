package com.seuprojeto.rhapi.security;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
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
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.OrRequestMatcher;
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

    /**
     * CHAIN 0 — rotas públicas SEM JWT.
     * Cobre:
     *   - /public/** (sem context-path)
     *   - /rh-api/public/** (se houver context-path /rh-api no deploy)
     *   - /error e /favicon.ico para não exigir auth nesses caminhos
     */
    @Bean
    @Order(0)
    public SecurityFilterChain publicChain(HttpSecurity http) throws Exception {
        var publicMatcher = new OrRequestMatcher(
                new AntPathRequestMatcher("/public/**"),
                new AntPathRequestMatcher("/rh-api/public/**"),
                new AntPathRequestMatcher("/error"),
                new AntPathRequestMatcher("/favicon.ico")
        );

        http
            .securityMatcher(publicMatcher)
            .csrf(csrf -> csrf.disable())
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .exceptionHandling(ex -> ex.authenticationEntryPoint((req, res, e) ->
                    res.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized")))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                .anyRequest().permitAll()
            );
        // Não adicionar JwtAuthFilter aqui.
        return http.build();
    }

    /**
     * CHAIN 1 — restante da API COM JWT.
     */
    @Bean
    @Order(1)
    public SecurityFilterChain apiChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .exceptionHandling(ex -> ex.authenticationEntryPoint((req, res, e) ->
                    res.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized")))
            .authorizeHttpRequests(auth -> auth
                // Preflight CORS
                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                // Estáticos / docs
                .requestMatchers(
                        "/", "/index.html",
                        "/swagger-ui/**", "/v3/api-docs/**", "/swagger-ui.html",
                        "/assets/**", "/webjars/**"
                ).permitAll()

                // Login público
                .requestMatchers("/auth/login").permitAll()

                // Regras protegidas
                .requestMatchers(HttpMethod.POST, "/colaboradores/**").hasAnyRole("RH","ADMIN")
                .requestMatchers("/banco-horas/**").hasAnyRole("RH","ADMIN")
                .requestMatchers("/relatorios/email/**").hasAnyRole("RH","ADMIN")
                .requestMatchers("/assinaturas/**").hasAnyRole("RH","ADMIN")

                // Ponto
                .requestMatchers(HttpMethod.POST, "/pontos/bater").hasAnyRole("COLABORADOR","GESTOR","ADMIN")
                .requestMatchers(HttpMethod.GET, "/pontos/status-dia").authenticated()

                // Demais exigem auth
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
