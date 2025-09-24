package com.seuprojeto.rhapi.controller;

import com.seuprojeto.rhapi.domain.Colaborador;
import com.seuprojeto.rhapi.domain.RegistroPonto;
import com.seuprojeto.rhapi.domain.enums.OrigemRegistro;
import com.seuprojeto.rhapi.dto.RegistroPontoCreateDTO;
import com.seuprojeto.rhapi.repository.ColaboradorRepository;
import com.seuprojeto.rhapi.repository.RegistroPontoRepository;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/pontos")
public class RegistroPontoController {

    private static final ZoneId ZONE = ZoneId.of("America/Sao_Paulo");

    private final RegistroPontoRepository repo;
    private final ColaboradorRepository colabRepo;

    public RegistroPontoController(RegistroPontoRepository repo, ColaboradorRepository colabRepo) {
        this.repo = repo;
        this.colabRepo = colabRepo;
    }

    
     //  1) REGISTRO MANUAL (ADMIN/GESTOR)

    @PostMapping
    public ResponseEntity<?> criarManual(@RequestBody @Valid RegistroPontoCreateDTO dto) {
        Colaborador colab = colabRepo.findById(dto.colaboradorId()).orElse(null);
        if (colab == null) return ResponseEntity.badRequest().body("Colaborador não encontrado");

        // validações básicas
        if (dto.inicioAlmoco() != null && dto.fimAlmoco() != null && !dto.inicioAlmoco().isBefore(dto.fimAlmoco())) {
            return ResponseEntity.badRequest().body("Início do almoço deve ser antes do fim do almoço");
        }
        if (!dto.horaEntrada().isBefore(dto.horaSaida())) {
            return ResponseEntity.badRequest().body("Hora de entrada deve ser antes da hora de saída");
        }
        if (repo.existsByColaborador_IdAndData(dto.colaboradorId(), dto.data())) {
            return ResponseEntity.unprocessableEntity().body("Já existe registro para esta data");
        }

        RegistroPonto r = new RegistroPonto();
        r.setColaborador(colab);
        r.setData(dto.data());
        r.setHoraEntrada(dto.horaEntrada());
        r.setInicioAlmoco(dto.inicioAlmoco());
        r.setFimAlmoco(dto.fimAlmoco());
        r.setHoraSaida(dto.horaSaida());
        r.setOrigem(dto.origem() != null ? dto.origem() : OrigemRegistro.MANUAL);
        r.setObservacao(dto.observacao());

        r = repo.save(r);
        return ResponseEntity.created(URI.create("/pontos/" + r.getId())).body(r);
    }

 
    //   2) LISTAR POR PERÍODO
     
    @GetMapping
    public List<RegistroPonto> listar(@RequestParam Long colaboradorId,
                                      @RequestParam LocalDate de,
                                      @RequestParam LocalDate ate) {
        return repo.findByColaborador_IdAndDataBetween(colaboradorId, de, ate);
    }

    
     //  3) BOTÃO "BATER PONTO" (usa usuário logado)
     
    @PostMapping("/bater")
    public ResponseEntity<?> bater(@RequestBody(required = false) BaterPontoReq req) {
        // data padrão = hoje no fuso de São Paulo
        LocalDate data = (req != null && req.getData() != null) ? req.getData() : LocalDate.now(ZONE);
        LocalTime agora = LocalTime.now(ZONE);

        Colaborador eu = getColaboradorAutenticado();
        if (eu == null) return ResponseEntity.status(401).body("Usuário não autenticado");

        // busca (ou cria) o registro do dia
        Optional<RegistroPonto> opt = repo.findByColaborador_IdAndData(eu.getId(), data);
        RegistroPonto r = opt.orElseGet(() -> {
            RegistroPonto n = new RegistroPonto();
            n.setColaborador(eu);
            n.setData(data);
            n.setOrigem(OrigemRegistro.WEB);
            return n;
        });

        // decide a próxima batida
        if (r.getHoraEntrada() == null) {
            // 1ª batida: entrada
            r.setHoraEntrada(agora);
            if (req != null) { r.setEntradaLat(req.getLat()); r.setEntradaLng(req.getLng()); }
            r.setObservacao(joinObs(r.getObservacao(), "Entrada via botão"));
            RegistroPonto salvo = repo.save(r);
            return ResponseEntity.ok(Map.of(
                    "mensagem", "Entrada registrada",
                    "batidas", 1,
                    "faltam", 3,
                    "registroId", salvo.getId()
            ));
        }

        if (r.getInicioAlmoco() == null) {
            // 2ª batida: início almoço (deve ser depois da entrada)
            if (!agora.isAfter(r.getHoraEntrada())) {
                return ResponseEntity.badRequest().body("Horário atual deve ser após a entrada.");
            }
            r.setInicioAlmoco(agora);
            if (req != null) { r.setAlmocoIniLat(req.getLat()); r.setAlmocoIniLng(req.getLng()); }
            r.setObservacao(joinObs(r.getObservacao(), "Início almoço via botão"));
            RegistroPonto salvo = repo.save(r);
            return ResponseEntity.ok(Map.of(
                    "mensagem", "Início do almoço registrado",
                    "batidas", 2,
                    "faltam", 2,
                    "registroId", salvo.getId()
            ));
        }

        if (r.getFimAlmoco() == null) {
            // 3ª batida: fim almoço (deve ser depois do início do almoço)
            if (r.getInicioAlmoco() == null) {
                return ResponseEntity.status(409).body("Início do almoço não registrado.");
            }
            if (!agora.isAfter(r.getInicioAlmoco())) {
                return ResponseEntity.badRequest().body("Horário atual deve ser após o início do almoço.");
            }
            r.setFimAlmoco(agora);
            if (req != null) { r.setAlmocoFimLat(req.getLat()); r.setAlmocoFimLng(req.getLng()); }
            r.setObservacao(joinObs(r.getObservacao(), "Fim almoço via botão"));
            RegistroPonto salvo = repo.save(r);
            return ResponseEntity.ok(Map.of(
                    "mensagem", "Fim do almoço registrado",
                    "batidas", 3,
                    "faltam", 1,
                    "registroId", salvo.getId()
            ));
        }

        if (r.getHoraSaida() == null) {
            // 4ª batida: saída (deve ser depois do último marco)
            LocalTime referencia = r.getFimAlmoco() != null ? r.getFimAlmoco()
                    : (r.getInicioAlmoco() != null ? r.getInicioAlmoco() : r.getHoraEntrada());
            if (!agora.isAfter(referencia)) {
                return ResponseEntity.badRequest().body("Horário atual deve ser após o último marco do dia.");
            }
            r.setHoraSaida(agora);
            if (req != null) { r.setSaidaLat(req.getLat()); r.setSaidaLng(req.getLng()); }
            r.setObservacao(joinObs(r.getObservacao(), "Saída via botão"));
            RegistroPonto salvo = repo.save(r);
            return ResponseEntity.ok(Map.of(
                    "mensagem", "Saída registrada (dia completo)",
                    "batidas", 4,
                    "faltam", 0,
                    "registroId", salvo.getId()
            ));
        }

        // já tem as 4 batidas
        return ResponseEntity.unprocessableEntity().body("As 4 batidas do dia já foram registradas.");
    }

    
    //   4) STATUS DO DIA (usa usuário logado)
    
    @GetMapping("/status-dia")
    public ResponseEntity<?> statusDia(@RequestParam(required = false) LocalDate data) {
        LocalDate dia = (data != null) ? data : LocalDate.now(ZONE);

        Colaborador eu = getColaboradorAutenticado();
        if (eu == null) return ResponseEntity.status(401).body("Usuário não autenticado");

        Optional<RegistroPonto> opt = repo.findByColaborador_IdAndData(eu.getId(), dia);
        if (opt.isEmpty()) {
            return ResponseEntity.ok(new StatusDiaResp(dia, 0, 4, null, null, null, null));
        }
        RegistroPonto r = opt.get();

        int batidas = 0;
        if (r.getHoraEntrada() != null) batidas++;
        if (r.getInicioAlmoco() != null) batidas++;
        if (r.getFimAlmoco() != null) batidas++;
        if (r.getHoraSaida() != null) batidas++;
        int faltam = Math.max(0, 4 - batidas);

        return ResponseEntity.ok(new StatusDiaResp(
                dia,
                batidas,
                faltam,
                r.getHoraEntrada(),
                r.getInicioAlmoco(),
                r.getFimAlmoco(),
                r.getHoraSaida()
        ));
    }

    
     //  Helpers / DTOs internos
       

    private Colaborador getColaboradorAutenticado() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getName() == null) return null;
        String email = auth.getName();

        
        return colabRepo.findByEmailIgnoreCase(email).orElse(null);
    }

    private static String joinObs(String atual, String novo) {
        if (atual == null || atual.isBlank()) return novo;
        return atual + " | " + novo;
    }

    // DTO do botão "BATER PONTO"
    public static class BaterPontoReq {
        private LocalDate data; // opcional (default = hoje)
        private Double lat;     // opcional
        private Double lng;     // opcional

        public LocalDate getData() { return data; }
        public void setData(LocalDate data) { this.data = data; }
        public Double getLat() { return lat; }
        public void setLat(Double lat) { this.lat = lat; }
        public Double getLng() { return lng; }
        public void setLng(Double lng) { this.lng = lng; }
    }

    // Resposta do status do dia
    public record StatusDiaResp(
            LocalDate data,
            int batidas,
            int faltam,
            LocalTime entrada,
            LocalTime almocoIni,
            LocalTime almocoFim,
            LocalTime saida
    ) {}
}
