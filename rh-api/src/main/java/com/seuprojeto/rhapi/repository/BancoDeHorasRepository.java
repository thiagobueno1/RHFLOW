package com.seuprojeto.rhapi.repository;

import com.seuprojeto.rhapi.domain.BancoDeHoras;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface BancoDeHorasRepository extends JpaRepository<BancoDeHoras, Long> {

    Optional<BancoDeHoras> findByColaborador_IdAndCompetencia(Long colaboradorId, String competencia);

    @Query("select coalesce(sum(b.saldoMinutos), 0) " +
           "from BancoDeHoras b " +
           "where b.colaborador.id = :colabId and b.competencia <= :competencia")
    Integer sumSaldoAte(@Param("colabId") Long colaboradorId,
                        @Param("competencia") String competencia);
}
