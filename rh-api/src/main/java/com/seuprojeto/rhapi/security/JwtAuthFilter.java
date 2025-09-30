package com.seuprojeto.rhapi.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

/**
 * Autentica via JWT somente quando o header Authorization está presente.
 * NÃO retorna 401 dentro do filtro. Rotas públicas são ignoradas.
 */
@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    // Rotas que não passam por autenticação
    // Inclui a variante com context-path (/**/public/**) para funcionar mesmo se houver server.servlet.context-path.
    private static final String[] SKIP_PATHS = new String[] {
            "/", "/index.html",
            "/auth/login",
            "/swagger-ui/**", "/v3/api-docs/**", "/swagger-ui.html",
            "/webjars/**", "/assets/**",
            "/public/**",        // sem context-path
            "/**/public/**"      // com context-path, ex.: /rh-api/public/**
    };

    public JwtAuthFilter(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    protected boolean shouldNotFilter(@NonNull HttpServletRequest request) {
        final String path = request.getRequestURI(); // inclui context-path
        for (String p : SKIP_PATHS) {
            if (pathMatcher.match(p, path)) {
                return true; // pula totalmente o filtro nessas rotas
            }
        }
        // também pula pré-flight CORS
        return "OPTIONS".equalsIgnoreCase(request.getMethod());
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain)
            throws ServletException, IOException {

        final String authHeader = request.getHeader("Authorization");

        // Sem token? Não autentica e segue o fluxo (SecurityConfig decide o acesso).
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        final String jwt = authHeader.substring(7);
        try {
            Jws<Claims> jws = jwtService.parse(jwt);
            Claims claims = jws.getBody();
            String subject = claims.getSubject(); // normalmente e-mail/username

            if (subject != null && SecurityContextHolder.getContext().getAuthentication() == null) {

                // tenta obter o papel do token. Aceita "role" ou "papel".
                String role = null;
                Object roleObj = claims.get("role");
                if (roleObj == null) roleObj = claims.get("papel");
                if (roleObj != null) role = String.valueOf(roleObj);

                List<SimpleGrantedAuthority> auths;
                if (role != null) {
                    // garante prefixo ROLE_
                    String granted = role.startsWith("ROLE_") ? role : "ROLE_" + role;
                    auths = List.of(new SimpleGrantedAuthority(granted));
                } else {
                    auths = Collections.emptyList();
                }

                var authentication =
                        new UsernamePasswordAuthenticationToken(subject, null, auths);
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        } catch (Exception ex) {
            // Token inválido/expirado: limpa contexto e segue sem autenticar.
            SecurityContextHolder.clearContext();
        }

        filterChain.doFilter(request, response);
    }
}
