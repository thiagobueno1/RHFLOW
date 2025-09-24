package com.seuprojeto.rhapi.service;

import com.seuprojeto.rhapi.domain.AssinaturaToken;
import com.seuprojeto.rhapi.domain.Colaborador;
import com.seuprojeto.rhapi.domain.enums.AssinaturaStatus;
import com.seuprojeto.rhapi.repository.AssinaturaTokenRepository;
import com.seuprojeto.rhapi.repository.ColaboradorRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
public class AssinaturaService {

    private final AssinaturaTokenRepository tokenRepo;
    private final ColaboradorRepository colabRepo;

    public AssinaturaService(AssinaturaTokenRepository tokenRepo,
                             ColaboradorRepository colabRepo) {
        this.tokenRepo = tokenRepo;
        this.colabRepo = colabRepo;
    }

    /**
     * Cria um token de assinatura para o colaborador e período informados.
     * @param colaboradorId ID do colaborador
     * @param de data inicial do relatório
     * @param ate data final do relatório
     * @param validadeHoras validade do token em horas
     * @param hostBase ignorado aqui (link é montado no controller/email), mantido para compatibilidade
     */
    public AssinaturaToken criarToken(Long colaboradorId,
                                      LocalDate de,
                                      LocalDate ate,
                                      int validadeHoras,
                                      String hostBase) {
        Colaborador c = colabRepo.findById(colaboradorId)
                .orElseThrow(() -> new IllegalArgumentException("Colaborador não encontrado"));

        AssinaturaToken t = new AssinaturaToken();
        t.setToken(gerarToken());
        t.setColaborador(c);
        t.setDe(de);
        t.setAte(ate);
        t.setStatus(AssinaturaStatus.PENDENTE);
        t.setExpiresAt(Instant.now().plus(validadeHoras, ChronoUnit.HOURS));

        return tokenRepo.save(t);
    }

    /**
     * Aceita/assina um token. Se estiver expirado, marca como EXPIRADO e retorna false.
     * Se já estiver ASSINADO, retorna true (idempotente).
     */
    public boolean aceitar(String token, String ip, String ua) {
        AssinaturaToken t = tokenRepo.findByToken(token)
                .orElseThrow(() -> new IllegalArgumentException("Token inválido"));

        // Expirado?
        if (t.getExpiresAt() != null && Instant.now().isAfter(t.getExpiresAt())) {
            if (t.getStatus() != AssinaturaStatus.EXPIRADO) {
                t.setStatus(AssinaturaStatus.EXPIRADO);
                tokenRepo.save(t);
            }
            return false;
        }

        // Já assinado? idempotente
        if (t.getStatus() == AssinaturaStatus.ASSINADO) {
            return true;
        }

        // Assina agora
        t.setStatus(AssinaturaStatus.ASSINADO);
        t.setSignedAt(Instant.now());
        t.setSignedIp(ip);
        t.setSignedUa(ua);
        tokenRepo.save(t);
        return true;
    }

    /**
     * Retorna status do token para exibição no front/retorno do controller.
     * Mapa simples para evitar criar novo DTO agora.
     */
    public Map<String, Object> status(String token) {
        AssinaturaToken t = tokenRepo.findByToken(token)
                .orElseThrow(() -> new IllegalArgumentException("Token inválido"));

        boolean expirado = t.getExpiresAt() != null && Instant.now().isAfter(t.getExpiresAt());
        AssinaturaStatus statusEfetivo = expirado && t.getStatus() != AssinaturaStatus.ASSINADO
                ? AssinaturaStatus.EXPIRADO
                : t.getStatus();

        return Map.of(
                "token", t.getToken(),
                "colaboradorId", Optional.ofNullable(t.getColaborador()).map(Colaborador::getId).orElse(null),
                "colaboradorNome", Optional.ofNullable(t.getColaborador()).map(Colaborador::getNome).orElse(null),
                "de", t.getDe(),
                "ate", t.getAte(),
                "status", statusEfetivo.name(),
                "expiresAt", t.getExpiresAt(),
                "signedAt", t.getSignedAt(),
                "signedIp", t.getSignedIp(),
                "signedUa", t.getSignedUa()
        );
    }

    // ===== util =====

    private String gerarToken() {
        // token curto, amigável para link
        return UUID.randomUUID().toString().replace("-", "");
    }
}
