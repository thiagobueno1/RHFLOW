package com.seuprojeto.rhapi.domain;

import com.seuprojeto.rhapi.domain.enums.OrigemRegistro;
import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Table(name = "registros_ponto",
       uniqueConstraints = @UniqueConstraint(name = "uk_ponto_colab_data", columnNames = {"colaborador_id","data"}))
public class RegistroPonto extends AuditableBase {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column private Double entradaLat;
    @Column private Double entradaLng;
    @Column private Double almocoIniLat;
    @Column private Double almocoIniLng;
    @Column private Double almocoFimLat;
    @Column private Double almocoFimLng;
    @Column private Double saidaLat;
    @Column private Double saidaLng;

    @ManyToOne(optional = false)
    @JoinColumn(name = "colaborador_id")
    private Colaborador colaborador;

    @Column(nullable = false)
    private LocalDate data;

    @Column(name = "hora_entrada", nullable = false)
    private LocalTime horaEntrada;

    @Column(name = "inicio_almoco")
    private LocalTime inicioAlmoco;

    @Column(name = "fim_almoco")
    private LocalTime fimAlmoco;

    // AGORA pode ser nulo (primeira batida do dia)
    @Column(name = "hora_saida")
    private LocalTime horaSaida;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrigemRegistro origem = OrigemRegistro.WEB;

    @Column(length = 255)
    private String observacao;

    public Long getId() { return id; }
    public Colaborador getColaborador() { return colaborador; }
    public void setColaborador(Colaborador colaborador) { this.colaborador = colaborador; }
    public LocalDate getData() { return data; }
    public void setData(LocalDate data) { this.data = data; }
    public LocalTime getHoraEntrada() { return horaEntrada; }
    public void setHoraEntrada(LocalTime v) { this.horaEntrada = v; }
    public LocalTime getInicioAlmoco() { return inicioAlmoco; }
    public void setInicioAlmoco(LocalTime v) { this.inicioAlmoco = v; }
    public LocalTime getFimAlmoco() { return fimAlmoco; }
    public void setFimAlmoco(LocalTime v) { this.fimAlmoco = v; }
    public LocalTime getHoraSaida() { return horaSaida; }
    public void setHoraSaida(LocalTime v) { this.horaSaida = v; }
    public OrigemRegistro getOrigem() { return origem; }
    public void setOrigem(OrigemRegistro origem) { this.origem = origem; }
    public String getObservacao() { return observacao; }
    public void setObservacao(String observacao) { this.observacao = observacao; }
    public Double getEntradaLat() { return entradaLat; }
    public void setEntradaLat(Double v) { this.entradaLat = v; }
    public Double getEntradaLng() { return entradaLng; }
    public void setEntradaLng(Double v) { this.entradaLng = v; }
    public Double getAlmocoIniLat() { return almocoIniLat; }
    public void setAlmocoIniLat(Double v) { this.almocoIniLat = v; }
    public Double getAlmocoIniLng() { return almocoIniLng; }
    public void setAlmocoIniLng(Double v) { this.almocoIniLng = v; }
    public Double getAlmocoFimLat() { return almocoFimLat; }
    public void setAlmocoFimLat(Double v) { this.almocoFimLat = v; }
    public Double getAlmocoFimLng() { return almocoFimLng; }
    public void setAlmocoFimLng(Double v) { this.almocoFimLng = v; }
    public Double getSaidaLat() { return saidaLat; }
    public void setSaidaLat(Double v) { this.saidaLat = v; }
    public Double getSaidaLng() { return saidaLng; }
    public void setSaidaLng(Double v) { this.saidaLng = v; }
}
