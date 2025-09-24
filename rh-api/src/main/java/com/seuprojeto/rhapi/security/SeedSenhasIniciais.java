package com.seuprojeto.rhapi.security;

import com.seuprojeto.rhapi.repository.ColaboradorRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class SeedSenhasIniciais implements CommandLineRunner {
    private final ColaboradorRepository repo;
    private final PasswordEncoder encoder;

    public SeedSenhasIniciais(ColaboradorRepository repo, PasswordEncoder encoder) {
        this.repo = repo; this.encoder = encoder;
    }

    @Override
    public void run(String... args) {
        repo.findAll().forEach(c -> {
            if (c.getSenhaHash() == null || c.getSenhaHash().isBlank()) {
                String cpfDigits = c.getCpf().replaceAll("\\D", "");
                c.setSenhaHash(encoder.encode(cpfDigits));
                repo.save(c);
            }
        });
    }
}

