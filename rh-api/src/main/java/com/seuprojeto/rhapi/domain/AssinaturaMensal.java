package com.seuprojeto.rhapi.domain;

import com.seuprojeto.rhapi.domain.enums.AssinaturaMensalStatus;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "assinaturas_mensais",
       uniqueConstraints = {
           @UniqueConstraint(name = "uk_ass_mensal", columnNames = {"colaborador_id", "competencia"})
       },
       indexes = {
           @Index(name = "idx_ass_mensal_colab", columnList = "colaborador_id"),
           @Index(name = "idx_ass_mensal_comp", columnList = "competencia"),
           @Index(name = "idx_ass_mensal_status", columnList = "status")
       }
)
public class AssinaturaMensal extends AuditableBase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Quem deve aceitar */
    @ManyToOne(optional = false)
    @JoinColumn(name = "colaborador_id", nullable = false)
    private Colaborador colaborador;

    /** Competência no formato YYYYMM (ex.: 202508) */
    @Column(nullable = false)
    private Integer competencia;

    /** Status da aceitação */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AssinaturaMensalStatus status = AssinaturaMensalStatus.PENDENTE;

    /** Quando o e-mail foi enviado (marca de controle) */
    @Column(name = "email_sent_at")
    private LocalDateTime emailSentAt;

    /** Quando o colaborador decidiu (aceitou/recusou) */
    @Column(name = "decidido_em")
    private LocalDateTime decididoEm;

    /** Metadados da decisão */
    @Column(name = "decidido_ip", length = 64)
    private String decididoIp;

    @Column(name = "decidido_ua", length = 255)
    private String decididoUa;

    // ===== Getters/Setters =====

    public Long getId() { return id; }

    public Colaborador getColaborador() { return colaborador; }
    public void setColaborador(Colaborador colaborador) { this.colaborador = colaborador; }

    public Integer getCompetencia() { return competencia; }
    public void setCompetencia(Integer competencia) { this.competencia = competencia; }

    public AssinaturaMensalStatus getStatus() { return status; }
    public void setStatus(AssinaturaMensalStatus status) { this.status = status; }

    public LocalDateTime getEmailSentAt() { return emailSentAt; }
    public void setEmailSentAt(LocalDateTime emailSentAt) { this.emailSentAt = emailSentAt; }

    public LocalDateTime getDecididoEm() { return decididoEm; }
    public void setDecididoEm(LocalDateTime decididoEm) { this.decididoEm = decididoEm; }

    public String getDecididoIp() { return decididoIp; }
    public void setDecididoIp(String decididoIp) { this.decididoIp = decididoIp; }

    public String getDecididoUa() { return decididoUa; }
    public void setDecididoUa(String decididoUa) { this.decididoUa = decididoUa; }
}
