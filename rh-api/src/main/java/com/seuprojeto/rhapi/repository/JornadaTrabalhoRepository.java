package com.seuprojeto.rhapi.repository;

import com.seuprojeto.rhapi.domain.JornadaTrabalho;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface JornadaTrabalhoRepository extends JpaRepository<JornadaTrabalho, Long> {
    Optional<JornadaTrabalho> findByColaborador_Id(Long colaboradorId);
}
