package com.seuprojeto.rhapi.domain;

import jakarta.persistence.*;

@Entity
@Table(name = "bancos_horas", uniqueConstraints = @UniqueConstraint(name = "uk_banco_colab_comp", columnNames = {"colaborador_id","competencia"}))
public class BancoDeHoras extends AuditableBase {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "colaborador_id")
    private Colaborador colaborador;

    @Column(nullable = false, length = 7) // YYYY-MM
    private String competencia;

    @Column(name = "saldo_minutos", nullable = false)
    private int saldoMinutos = 0;

    public Long getId() { return id; }
    public Colaborador getColaborador() { return colaborador; }
    public void setColaborador(Colaborador colaborador) { this.colaborador = colaborador; }
    public String getCompetencia() { return competencia; }
    public void setCompetencia(String competencia) { this.competencia = competencia; }
    public int getSaldoMinutos() { return saldoMinutos; }
    public void setSaldoMinutos(int saldoMinutos) { this.saldoMinutos = saldoMinutos; }
}
