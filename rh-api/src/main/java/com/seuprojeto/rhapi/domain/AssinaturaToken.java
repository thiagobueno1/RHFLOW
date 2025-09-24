package com.seuprojeto.rhapi.domain;

import com.seuprojeto.rhapi.domain.enums.AssinaturaStatus;
import jakarta.persistence.*;
import java.time.Instant;
import java.time.LocalDate;

@Entity
@Table(name = "assinatura_tokens",
       indexes = {
           @Index(name = "idx_ass_token", columnList = "token", unique = true),
           @Index(name = "idx_ass_colab", columnList = "colaborador_id")
       })
public class AssinaturaToken extends AuditableBase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Token público único (usado na URL de assinatura) */
    @Column(nullable = false, unique = true, length = 80)
    private String token;

    @ManyToOne(optional = false)
    @JoinColumn(name = "colaborador_id", nullable = false)
    private Colaborador colaborador;

    /** Período do relatório/termo que será assinado */
    @Column(name = "de_data", nullable = false)
    private LocalDate de;

    @Column(name = "ate_data", nullable = false)
    private LocalDate ate;

    /** Quando expira a possibilidade de assinar */
    @Column(name = "expires_at")
    private Instant expiresAt;

    /** Quando foi assinado (se assinado) */
    @Column(name = "signed_at")
    private Instant signedAt;

    /** Metadados da assinatura */
    @Column(name = "signed_ip", length = 64)
    private String signedIp;

    @Column(name = "signed_ua", length = 255)
    private String signedUa;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AssinaturaStatus status = AssinaturaStatus.PENDENTE;

    // ==== Getters/Setters ====

    public Long getId() { return id; }

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }

    public Colaborador getColaborador() { return colaborador; }
    public void setColaborador(Colaborador colaborador) { this.colaborador = colaborador; }

    public LocalDate getDe() { return de; }
    public void setDe(LocalDate de) { this.de = de; }

    public LocalDate getAte() { return ate; }
    public void setAte(LocalDate ate) { this.ate = ate; }

    public Instant getExpiresAt() { return expiresAt; }
    public void setExpiresAt(Instant expiresAt) { this.expiresAt = expiresAt; }

    public Instant getSignedAt() { return signedAt; }
    public void setSignedAt(Instant signedAt) { this.signedAt = signedAt; }

    public String getSignedIp() { return signedIp; }
    public void setSignedIp(String signedIp) { this.signedIp = signedIp; }

    public String getSignedUa() { return signedUa; }
    public void setSignedUa(String signedUa) { this.signedUa = signedUa; }

    public AssinaturaStatus getStatus() { return status; }
    public void setStatus(AssinaturaStatus status) { this.status = status; }
}
