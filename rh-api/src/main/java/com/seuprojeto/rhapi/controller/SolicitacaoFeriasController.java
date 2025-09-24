package com.seuprojeto.rhapi.controller;

import com.seuprojeto.rhapi.domain.Colaborador;
import com.seuprojeto.rhapi.domain.SolicitacaoFerias;
import com.seuprojeto.rhapi.domain.enums.StatusFerias;
import com.seuprojeto.rhapi.dto.SolicitacaoFeriasCreateDTO;
import com.seuprojeto.rhapi.dto.SolicitacaoFeriasDTO;
import com.seuprojeto.rhapi.mapper.DtoMapper;
import com.seuprojeto.rhapi.repository.ColaboradorRepository;
import com.seuprojeto.rhapi.repository.SolicitacaoFeriasRepository;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.time.temporal.ChronoUnit;
import java.util.List;

@RestController
@RequestMapping("/ferias")
public class SolicitacaoFeriasController {

    private final SolicitacaoFeriasRepository repo;
    private final ColaboradorRepository colabRepo;

    public SolicitacaoFeriasController(SolicitacaoFeriasRepository repo,
                                       ColaboradorRepository colabRepo) {
        this.repo = repo; this.colabRepo = colabRepo;
    }

    @PostMapping
    public ResponseEntity<?> criar(@RequestBody @Valid SolicitacaoFeriasCreateDTO dto) {
        Colaborador colab = colabRepo.findById(dto.colaboradorId()).orElse(null);
        if (colab == null) return ResponseEntity.badRequest().body("Colaborador não encontrado");
        if (!dto.dataInicio().isBefore(dto.dataFim()))
            return ResponseEntity.badRequest().body("dataInicio deve ser antes de dataFim");

        var statusAtivos = List.of(StatusFerias.CRIADA, StatusFerias.APROVADA);
        boolean overlap = repo.hasOverlap(dto.colaboradorId(), statusAtivos, dto.dataInicio(), dto.dataFim());
        if (overlap) {
            return ResponseEntity.unprocessableEntity()
                    .body("Já existe solicitação de férias sobreposta para esse período");
        }

        long diasCalc = ChronoUnit.DAYS.between(dto.dataInicio(), dto.dataFim()) + 1;

        SolicitacaoFerias s = new SolicitacaoFerias();
        s.setColaborador(colab);
        s.setDataInicio(dto.dataInicio());
        s.setDataFim(dto.dataFim());
        s.setDias((int) diasCalc);
        s.setMotivo(dto.motivo());

        s = repo.save(s);
        return ResponseEntity.created(URI.create("/ferias/" + s.getId()))
                .body(DtoMapper.toDTO(s));
    }

    @GetMapping
    public List<SolicitacaoFeriasDTO> listar(@RequestParam(required = false) Long colaboradorId) {
        return (colaboradorId == null ? repo.findAll() : repo.findByColaborador_Id(colaboradorId))
                .stream().map(DtoMapper::toDTO).toList();
    }

    @PatchMapping("/{id}/aprovar")
    public ResponseEntity<?> aprovar(@PathVariable Long id) {
        return alterarStatus(id, StatusFerias.APROVADA);
    }

    @PatchMapping("/{id}/reprovar")
    public ResponseEntity<?> reprovar(@PathVariable Long id) {
        return alterarStatus(id, StatusFerias.REPROVADA);
    }

    private ResponseEntity<?> alterarStatus(Long id, StatusFerias novo) {
        return repo.findById(id).map(s -> {
            s.setStatus(novo);
            return ResponseEntity.ok(DtoMapper.toDTO(repo.save(s)));
        }).orElseGet(() -> ResponseEntity.notFound().build());
    }
}
