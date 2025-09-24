package com.seuprojeto.rhapi.controller;

import com.seuprojeto.rhapi.domain.Colaborador;
import com.seuprojeto.rhapi.domain.Departamento;
import com.seuprojeto.rhapi.domain.enums.Role;
import com.seuprojeto.rhapi.dto.ColaboradorCreateDTO;
import com.seuprojeto.rhapi.dto.ColaboradorDTO;
import com.seuprojeto.rhapi.mapper.DtoMapper;
import com.seuprojeto.rhapi.repository.ColaboradorRepository;
import com.seuprojeto.rhapi.repository.DepartamentoRepository;
import jakarta.validation.Valid;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/colaboradores")
public class ColaboradorController {

    private final ColaboradorRepository repo;
    private final DepartamentoRepository deptRepo;
    private final PasswordEncoder encoder;
    private final DtoMapper mapper;

    public ColaboradorController(ColaboradorRepository repo,
                                 DepartamentoRepository deptRepo,
                                 PasswordEncoder encoder,
                                 DtoMapper mapper) {
        this.repo = repo;
        this.deptRepo = deptRepo;
        this.encoder = encoder;
        this.mapper = mapper;
    }

    /**
     * Cria colaborador já com papel (ADMIN/RH/COLABORADOR) e senha inicial = CPF só dígitos (BCrypt).
     * Validações:
     *  - ADMIN pode criar ADMIN/RH/COLABORADOR
     *  - RH pode criar RH/COLABORADOR (não pode ADMIN)
     *  - CPF e e-mail chegam validados no DTO (@CPF, @Email) e aqui são normalizados
     *    (CPF só números; e-mail em minúsculas e trim)
     */
    @PostMapping
    public ResponseEntity<?> criar(@RequestBody @Valid ColaboradorCreateDTO dto) {
        // 1) Autorização de quem está chamando
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Set<String> roles = auth.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toSet());
        boolean isAdmin = roles.contains("ROLE_ADMIN");
        boolean isRH = roles.contains("ROLE_RH");

        if (!isAdmin && !isRH) {
            return ResponseEntity.status(403).body("Somente ADMIN ou RH podem criar colaboradores.");
        }

        // 2) Papel requisitado (default COLABORADOR) + regra de concessão
        Role papelRequisitado = dto.papel() == null ? Role.COLABORADOR : dto.papel();
        if (isRH && !isAdmin && papelRequisitado == Role.ADMIN) {
            return ResponseEntity.status(403).body("RH não pode criar usuário com papel ADMIN.");
        }

        // 3) Departamento
        Departamento dept = deptRepo.findById(dto.departamentoId()).orElse(null);
        if (dept == null) return ResponseEntity.badRequest().body("Departamento não encontrado");

        try {
            // 4) Normalizações: CPF só dígitos; e-mail minúsculo/sem espaços nas pontas
            String cpfDigits = dto.cpf().replaceAll("\\D", "");
            String emailNorm = dto.email().trim().toLowerCase(Locale.ROOT);

            // 5) Instanciar entidade + senha inicial (BCrypt do CPF só números)
            Colaborador c = new Colaborador();
            c.setNome(dto.nome());
            c.setCpf(cpfDigits);               // guardar somente dígitos
            c.setEmail(emailNorm);             // email normalizado
            c.setCargo(dto.cargo());
            c.setDataAdmissao(dto.dataAdmissao());
            c.setDepartamento(dept);
            c.setPapel(papelRequisitado);
            if (c.getAtivo() == null) c.setAtivo(true);

            c.setSenhaHash(encoder.encode(cpfDigits)); // senha inicial = CPF só números

            c = repo.save(c);

            ColaboradorDTO out = mapper.toColaboradorDTO(c);
            return ResponseEntity.created(URI.create("/colaboradores/" + c.getId())).body(out);

        } catch (DataIntegrityViolationException dive) {
            String msg = "Não foi possível criar colaborador (verifique CPF/e-mail únicos): "
                    + (dive.getMostSpecificCause() != null ? dive.getMostSpecificCause().getMessage() : dive.getMessage());
            return ResponseEntity.unprocessableEntity().body(msg);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Erro ao criar colaborador: " + e.getMessage());
        }
    }

    /** Lista todos os colaboradores (DTO de saída). */
    @GetMapping
    public List<ColaboradorDTO> listar() {
        return repo.findAll().stream()
                .map(mapper::toColaboradorDTO)
                .toList();
    }

    /** Detalhe por ID (DTO de saída). */
    @GetMapping("/{id}")
    public ResponseEntity<?> buscarPorId(@PathVariable Long id) {
        return repo.findById(id)
                .map(c -> ResponseEntity.ok(mapper.toColaboradorDTO(c)))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}
