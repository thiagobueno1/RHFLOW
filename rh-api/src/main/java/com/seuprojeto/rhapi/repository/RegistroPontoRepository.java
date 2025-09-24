package com.seuprojeto.rhapi.repository;

import com.seuprojeto.rhapi.domain.RegistroPonto;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface RegistroPontoRepository extends JpaRepository<RegistroPonto, Long> {
    List<RegistroPonto> findByColaborador_IdAndDataBetween(Long colaboradorId, LocalDate de, LocalDate ate);
    boolean existsByColaborador_IdAndData(Long colaboradorId, LocalDate data);

    Optional<RegistroPonto> findByColaborador_IdAndData(Long colaboradorId, LocalDate data);
}
