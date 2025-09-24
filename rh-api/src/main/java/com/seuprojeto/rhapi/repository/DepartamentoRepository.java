package com.seuprojeto.rhapi.repository;

import com.seuprojeto.rhapi.domain.Departamento;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface DepartamentoRepository extends JpaRepository<Departamento, Long> {
    Optional<Departamento> findByNomeIgnoreCase(String nome);
}
