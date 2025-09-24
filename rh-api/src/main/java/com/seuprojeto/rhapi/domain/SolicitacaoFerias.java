package com.seuprojeto.rhapi.domain;

import com.seuprojeto.rhapi.domain.enums.StatusFerias;
import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "solicitacoes_ferias")
public class SolicitacaoFerias extends AuditableBase {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "colaborador_id")
    private Colaborador colaborador;

    @Column(name = "data_inicio", nullable = false)
    private LocalDate dataInicio;

    @Column(name = "data_fim", nullable = false)
    private LocalDate dataFim;

    @Column(nullable = false)
    private Integer dias;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatusFerias status = StatusFerias.CRIADA;

    @Column(length = 255)
    private String motivo;

    public Long getId() { return id; }
    public Colaborador getColaborador() { return colaborador; }
    public void setColaborador(Colaborador colaborador) { this.colaborador = colaborador; }
    public LocalDate getDataInicio() { return dataInicio; }
    public void setDataInicio(LocalDate dataInicio) { this.dataInicio = dataInicio; }
    public LocalDate getDataFim() { return dataFim; }
    public void setDataFim(LocalDate dataFim) { this.dataFim = dataFim; }
    public Integer getDias() { return dias; }
    public void setDias(Integer dias) { this.dias = dias; }
    public StatusFerias getStatus() { return status; }
    public void setStatus(StatusFerias status) { this.status = status; }
    public String getMotivo() { return motivo; }
    public void setMotivo(String motivo) { this.motivo = motivo; }
}
