package com.seuprojeto.rhapi.domain;

import jakarta.persistence.*;
import java.time.LocalDate;
import com.seuprojeto.rhapi.domain.enums.Role;


@Entity
@Table(name = "colaboradores")
public class Colaborador extends AuditableBase {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 150)
    private String nome;

    @Column(nullable = false, length = 14, unique = true)
    private String cpf;

    @Column(nullable = false, length = 180, unique = true)
    private String email;

    @Column(nullable = false)
    private Boolean ativo = true;

    @Column(length = 120)
    private String cargo;

    @Column(name = "data_admissao", nullable = false)
    private LocalDate dataAdmissao;

    @ManyToOne(optional = false)
    @JoinColumn(name = "departamento_id")
    private Departamento departamento;

    @Column(name = "senha_hash", length = 100)
private String senhaHash;

@Enumerated(EnumType.STRING)
@Column(name = "papel", nullable = false)
private Role papel = Role.COLABORADOR;

public String getSenhaHash() { return senhaHash; }
public void setSenhaHash(String senhaHash) { this.senhaHash = senhaHash; }
public Role getPapel() { return papel; }
public void setPapel(Role papel) { this.papel = papel; }

    public Long getId() { return id; }
    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
    public String getCpf() { return cpf; }
    public void setCpf(String cpf) { this.cpf = cpf; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public Boolean getAtivo() { return ativo; }
    public void setAtivo(Boolean ativo) { this.ativo = ativo; }
    public String getCargo() { return cargo; }
    public void setCargo(String cargo) { this.cargo = cargo; }
    public LocalDate getDataAdmissao() { return dataAdmissao; }
    public void setDataAdmissao(LocalDate dataAdmissao) { this.dataAdmissao = dataAdmissao; }
    public Departamento getDepartamento() { return departamento; }
    public void setDepartamento(Departamento departamento) { this.departamento = departamento; }
}
