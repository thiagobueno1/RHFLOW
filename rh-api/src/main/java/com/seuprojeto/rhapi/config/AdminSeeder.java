package com.seuprojeto.rhapi.config;

import com.seuprojeto.rhapi.domain.Colaborador;
import com.seuprojeto.rhapi.domain.Departamento;
import com.seuprojeto.rhapi.domain.enums.Role;
import com.seuprojeto.rhapi.repository.ColaboradorRepository;
import com.seuprojeto.rhapi.repository.DepartamentoRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;
import java.util.Locale;

@Configuration
public class AdminSeeder {

    @Bean
    public CommandLineRunner seedAdmin(ColaboradorRepository colabRepo,
                                       DepartamentoRepository deptRepo,
                                       PasswordEncoder encoder) {
        return args -> {
            // Se já existe pelo menos 1 ADMIN, não faz nada
            if (colabRepo.existsByPapel(Role.ADMIN)) return;

            // Dados 
            String nome  = firstNonEmpty(
                    System.getProperty("app.admin.nome"),
                    System.getenv("APP_ADMIN_NOME"),
                    "Administrador"
            );

            String cpfDigits = firstNonEmpty(
                    System.getProperty("app.admin.cpf"),
                    System.getenv("APP_ADMIN_CPF"),
                    "52998224725" // exemplo válido
            ).replaceAll("\\D", "");

            String email = firstNonEmpty(
                    System.getProperty("app.admin.email"),
                    System.getenv("APP_ADMIN_EMAIL"),
                    "admin@empresa.com"
            ).trim().toLowerCase(Locale.ROOT);

            Long deptId = parseLongOrDefault(
                    firstNonEmpty(System.getProperty("app.admin.departamentoId"),
                                  System.getenv("APP_ADMIN_DEPARTAMENTO_ID"),
                                  "1"),
                    1L
            );

            // 1) Tentar PROMOVER por CPF
            var byCpf = colabRepo.findByCpf(cpfDigits);
            if (byCpf.isPresent()) {
                var u = byCpf.get();
                if (u.getPapel() != Role.ADMIN) {
                    u.setPapel(Role.ADMIN);
                    if (u.getAtivo() == null || !u.getAtivo()) u.setAtivo(true);
                    if (u.getSenhaHash() == null || u.getSenhaHash().isBlank()) {
                        u.setSenhaHash(encoder.encode(cpfDigits)); // senha = CPF dígitos
                    }
                    colabRepo.save(u);
                    System.out.println("[SEED] Usuário com CPF " + cpfDigits + " promovido a ADMIN.");
                } else {
                    System.out.println("[SEED] Já era ADMIN por CPF.");
                }
                return; // terminou
            }

            // 2) Tentar PROMOVER por e-mail
            var byEmail = colabRepo.findByEmailIgnoreCase(email);
            if (byEmail.isPresent()) {
                var u = byEmail.get();
                if (u.getPapel() != Role.ADMIN) {
                    u.setPapel(Role.ADMIN);
                    if (u.getAtivo() == null || !u.getAtivo()) u.setAtivo(true);
                    if (u.getSenhaHash() == null || u.getSenhaHash().isBlank()) {
                        u.setSenhaHash(encoder.encode(cpfDigits));
                    }
                    colabRepo.save(u);
                    System.out.println("[SEED] Usuário com e-mail " + email + " promovido a ADMIN.");
                } else {
                    System.out.println("[SEED] Já era ADMIN por e-mail.");
                }
                return;
            }

            // 3) Se não achou ninguém, CRIA um novo ADMIN
            Departamento dept = deptRepo.findById(deptId).orElseGet(() -> {
                Departamento d = new Departamento();
                d.setNome("Administração");
                return deptRepo.save(d);
            });

            Colaborador admin = new Colaborador();
            admin.setNome(nome);
            admin.setCpf(cpfDigits); // só dígitos
            admin.setEmail(email);
            admin.setCargo("Administrador");
            admin.setDataAdmissao(LocalDate.now());
            admin.setDepartamento(dept);
            admin.setPapel(Role.ADMIN);
            admin.setAtivo(true);
            admin.setSenhaHash(encoder.encode(cpfDigits)); // senha = CPF dígitos

            try {
                colabRepo.save(admin);
                System.out.println("[SEED] ADMIN criado: " + email + " (senha = CPF só dígitos)");
            } catch (DataIntegrityViolationException e) {
                System.out.println("[SEED] Falha ao criar ADMIN (duplicidade de CPF/e-mail?): " + e.getMessage());
            }
        };
    }

    private static String firstNonEmpty(String... vals) {
        for (String v : vals) {
            if (v != null && !v.trim().isEmpty()) return v;
        }
        return "";
    }

    private static Long parseLongOrDefault(String s, Long def) {
        try { return Long.parseLong(s); } catch (Exception e) { return def; }
    }
}
