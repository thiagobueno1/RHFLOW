package com.seuprojeto.rhapi.domain;

import jakarta.persistence.*;
import java.util.List;

@Entity
@Table(name = "departamentos")
public class Departamento extends AuditableBase {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 120, unique = true)
    private String nome;

    @OneToMany(mappedBy = "departamento")
    private List<Colaborador> colaboradores;

    public Long getId() { return id; }
    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
}