package com.seuprojeto.rhapi.controller;

import com.seuprojeto.rhapi.domain.Colaborador;
import com.seuprojeto.rhapi.dto.LoginRequest;
import com.seuprojeto.rhapi.dto.LoginResponse;
import com.seuprojeto.rhapi.repository.ColaboradorRepository;
import com.seuprojeto.rhapi.security.JwtService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final ColaboradorRepository colabRepo;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthController(ColaboradorRepository colabRepo,
                          PasswordEncoder passwordEncoder,
                          JwtService jwtService) {
        this.colabRepo = colabRepo;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody @Valid LoginRequest req) {
        Optional<Colaborador> opt = colabRepo.findByEmailIgnoreCase(req.email());
        if (opt.isEmpty()) {
            return ResponseEntity.status(401).body("E-mail ou senha inválidos");
        }

        Colaborador c = opt.get();
        if (Boolean.FALSE.equals(c.getAtivo())) {
            return ResponseEntity.status(403).body("Colaborador inativo");
        }

        String senhaDigitada = req.senha() == null ? "" : req.senha().trim();
        String cpfNumerico = c.getCpf() == null ? "" : c.getCpf().replaceAll("\\D", "");

        boolean ok;
        if (c.getSenhaHash() != null && !c.getSenhaHash().isBlank()) {
            ok = passwordEncoder.matches(senhaDigitada, c.getSenhaHash());
        } else {
            // fallback: aceita CPF (somente números) como senha quando não houver hash definido
            ok = senhaDigitada.equals(cpfNumerico);
        }

        if (!ok) {
            return ResponseEntity.status(401).body("E-mail ou senha inválidos");
        }

        Map<String, Object> claims = new HashMap<>();
        claims.put("email", c.getEmail());
        claims.put("papel", c.getPapel().name());
        claims.put("id", c.getId());
        claims.put("nome", c.getNome());

        String token = jwtService.generate(c.getEmail(), claims);

        return ResponseEntity.ok(new LoginResponse(
                token,
                c.getId(),
                c.getNome(),
                c.getEmail(),
                c.getPapel().name() // 
        ));
    }

    @GetMapping("/me")
    public ResponseEntity<?> me() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getName() == null) {
            return ResponseEntity.status(401).body("Não autenticado");
        }
        String email = auth.getName();

        Optional<Colaborador> opt = colabRepo.findByEmailIgnoreCase(email);
        if (opt.isEmpty()) {
            return ResponseEntity.status(404).body("Usuário não encontrado");
        }
        Colaborador c = opt.get();

        return ResponseEntity.ok(new LoginResponse(
                null,               // não retorna token
                c.getId(),
                c.getNome(),
                c.getEmail(),
                c.getPapel().name()
        ));
    }
}
