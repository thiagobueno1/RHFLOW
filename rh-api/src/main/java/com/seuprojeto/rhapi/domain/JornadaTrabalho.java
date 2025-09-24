package com.seuprojeto.rhapi.domain;

import jakarta.persistence.*;

@Entity
@Table(name = "jornadas_trabalho", uniqueConstraints = @UniqueConstraint(name = "uk_jornada_colab", columnNames = {"colaborador_id"}))
public class JornadaTrabalho extends AuditableBase {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(optional = false)
    @JoinColumn(name = "colaborador_id", unique = true)
    private Colaborador colaborador;

    private int minutosSeg; // segunda
    private int minutosTer; // terça
    private int minutosQua; // quarta
    private int minutosQui; // quinta
    private int minutosSex; // sexta
    private int minutosSab; // sábado
    private int minutosDom; // domingo

    public Long getId() { return id; }
    public Colaborador getColaborador() { return colaborador; }
    public void setColaborador(Colaborador colaborador) { this.colaborador = colaborador; }
    public int getMinutosSeg() { return minutosSeg; }
    public void setMinutosSeg(int minutosSeg) { this.minutosSeg = minutosSeg; }
    public int getMinutosTer() { return minutosTer; }
    public void setMinutosTer(int minutosTer) { this.minutosTer = minutosTer; }
    public int getMinutosQua() { return minutosQua; }
    public void setMinutosQua(int minutosQua) { this.minutosQua = minutosQua; }
    public int getMinutosQui() { return minutosQui; }
    public void setMinutosQui(int minutosQui) { this.minutosQui = minutosQui; }
    public int getMinutosSex() { return minutosSex; }
    public void setMinutosSex(int minutosSex) { this.minutosSex = minutosSex; }
    public int getMinutosSab() { return minutosSab; }
    public void setMinutosSab(int minutosSab) { this.minutosSab = minutosSab; }
    public int getMinutosDom() { return minutosDom; }
    public void setMinutosDom(int minutosDom) { this.minutosDom = minutosDom; }
}
