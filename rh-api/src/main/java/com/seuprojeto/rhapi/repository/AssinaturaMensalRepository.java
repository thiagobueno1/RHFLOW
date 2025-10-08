package com.seuprojeto.rhapi.repository;

import com.seuprojeto.rhapi.domain.AssinaturaMensal;
import com.seuprojeto.rhapi.domain.Colaborador;
import com.seuprojeto.rhapi.domain.enums.AssinaturaMensalStatus;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.List;

public interface AssinaturaMensalRepository extends JpaRepository<AssinaturaMensal, Long> {

    Optional<AssinaturaMensal> findByColaboradorAndCompetencia(Colaborador colaborador, Integer competencia);

    Optional<AssinaturaMensal> findByColaboradorIdAndCompetencia(Long colaboradorId, Integer competencia);

    /** Última competência anterior registrada para o colaborador (útil para bloquear se houver pendência anterior). */
    Optional<AssinaturaMensal> findFirstByColaboradorAndCompetenciaLessThanOrderByCompetenciaDesc(
            Colaborador colaborador, Integer competencia);

    List<AssinaturaMensal> findAllByColaboradorId(Long colaboradorId);

    @Modifying
    @Query("update AssinaturaMensal a set a.emailSentAt = CURRENT_TIMESTAMP where a.id = :id")
    int touchEmailSentAt(@Param("id") Long id);

    @Modifying
    @Query("update AssinaturaMensal a set a.status = :status, a.decididoEm = CURRENT_TIMESTAMP, a.decididoIp = :ip, a.decididoUa = :ua where a.id = :id")
    int decidir(@Param("id") Long id,
                @Param("status") AssinaturaMensalStatus status,
                @Param("ip") String ip,
                @Param("ua") String ua);
}
