package com.seuprojeto.rhapi.controller;

import com.seuprojeto.rhapi.domain.Colaborador;
import com.seuprojeto.rhapi.domain.JornadaTrabalho;
import com.seuprojeto.rhapi.dto.JornadaTrabalhoDTO;
import com.seuprojeto.rhapi.mapper.DtoMapper;
import com.seuprojeto.rhapi.repository.ColaboradorRepository;
import com.seuprojeto.rhapi.repository.JornadaTrabalhoRepository;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/jornadas")
public class JornadaTrabalhoController {

    private final JornadaTrabalhoRepository repo;
    private final ColaboradorRepository colabRepo;

    public JornadaTrabalhoController(JornadaTrabalhoRepository repo, ColaboradorRepository colabRepo) {
        this.repo = repo; this.colabRepo = colabRepo;
    }

    @PostMapping
    public ResponseEntity<?> upsert(@RequestBody @Valid JornadaTrabalhoDTO dto) {
        Colaborador colab = colabRepo.findById(dto.colaboradorId()).orElse(null);
        if (colab == null) return ResponseEntity.badRequest().body("Colaborador não encontrado");

        JornadaTrabalho j = repo.findByColaborador_Id(dto.colaboradorId()).orElse(new JornadaTrabalho());
        j.setColaborador(colab);
        j.setMinutosSeg(dto.minutosSeg());
        j.setMinutosTer(dto.minutosTer());
        j.setMinutosQua(dto.minutosQua());
        j.setMinutosQui(dto.minutosQui());
        j.setMinutosSex(dto.minutosSex());
        j.setMinutosSab(dto.minutosSab());
        j.setMinutosDom(dto.minutosDom());

        j = repo.save(j);
        return ResponseEntity.ok(DtoMapper.toDTO(j));
    }

    @GetMapping("/{colaboradorId}")
    public ResponseEntity<?> obter(@PathVariable Long colaboradorId) {
        return repo.findByColaborador_Id(colaboradorId)
                .<ResponseEntity<?>>map(j -> ResponseEntity.ok(DtoMapper.toDTO(j)))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}
