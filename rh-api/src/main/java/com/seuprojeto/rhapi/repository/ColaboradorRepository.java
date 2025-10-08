package com.seuprojeto.rhapi.repository;

import com.seuprojeto.rhapi.domain.Colaborador;
import org.springframework.data.jpa.repository.JpaRepository;
import com.seuprojeto.rhapi.domain.enums.Role;
import java.util.Optional;

public interface ColaboradorRepository extends JpaRepository<Colaborador, Long> {
    Optional<Colaborador> findByCpf(String cpf);
    Optional<Colaborador> findByEmailIgnoreCase(String email);
    Optional<Colaborador> findByEmail(String email);
    boolean existsByPapel(Role papel);
    
}