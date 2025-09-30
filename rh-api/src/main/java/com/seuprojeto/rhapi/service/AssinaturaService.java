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

        // >>> timestamps obrigatórios pelo schema (created_at / updated_at NOT NULL)
        Instant now = Instant.now();
        t.setCreatedAt(now);
        t.setUpdatedAt(now);

        return tokenRepo.save(t);
    }

    /**
     * Decide o token: aceita (true) ou recusa (false).
     * - Marca EXPIRADO se já passou da validade e ainda não estava decidido.
     * - Idempotente: se já ASSINADO/RECUSADO, apenas retorna.
     */
    public AssinaturaToken decidir(String token, boolean aceita, String ip, String ua) {
        AssinaturaToken t = tokenRepo.findByToken(token)
                .orElseThrow(() -> new IllegalArgumentException("Token inválido"));

        // Expirado -> se ainda não decidido, marca como EXPIRADO e retorna
        if (t.getExpiresAt() != null && Instant.now().isAfter(t.getExpiresAt())) {
            if (t.getStatus() != AssinaturaStatus.ASSINADO
                    && t.getStatus() != AssinaturaStatus.RECUSADO
                    && t.getStatus() != AssinaturaStatus.EXPIRADO) {
                t.setStatus(AssinaturaStatus.EXPIRADO);
                t.setUpdatedAt(Instant.now()); // <<< manter updated_at consistente
                t = tokenRepo.save(t);
            }
            return t;
        }

        // Idempotência: se já decidiu (ASSINADO/RECUSADO), só retorna
        if (t.getStatus() == AssinaturaStatus.ASSINADO || t.getStatus() == AssinaturaStatus.RECUSADO) {
            return t;
        }

        // Grava decisão agora
        t.setStatus(aceita ? AssinaturaStatus.ASSINADO : AssinaturaStatus.RECUSADO);
        t.setSignedAt(Instant.now());
        t.setSignedIp(ip);
        t.setSignedUa(ua);
        t.setUpdatedAt(Instant.now()); // <<< atualizar updated_at em qualquer mudança
        return tokenRepo.save(t);
    }

    /**
     * Aceita/assina um token (retrocompatível com chamadas antigas).
     * Se expirado e não decidido, marca como EXPIRADO e retorna false.
     * Se já estiver ASSINADO, retorna true (idempotente).
     */
    public boolean aceitar(String token, String ip, String ua) {
        AssinaturaToken t = decidir(token, true, ip, ua);
        return t.getStatus() == AssinaturaStatus.ASSINADO;
    }

    /**
     * Retorna status do token para exibição (sem criar DTO por enquanto).
     */
    public Map<String, Object> status(String token) {
        AssinaturaToken t = tokenRepo.findByToken(token)
                .orElseThrow(() -> new IllegalArgumentException("Token inválido"));

        boolean expirado = t.getExpiresAt() != null && Instant.now().isAfter(t.getExpiresAt());

        // Se expirou e ainda não foi decidido, reporta como EXPIRADO; caso já tenha sido decidido, mantém o status.
        AssinaturaStatus statusEfetivo =
                (expirado && t.getStatus() != AssinaturaStatus.ASSINADO && t.getStatus() != AssinaturaStatus.RECUSADO)
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
