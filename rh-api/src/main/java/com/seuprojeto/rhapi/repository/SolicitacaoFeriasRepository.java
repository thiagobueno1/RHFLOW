// src/main/java/com/seuprojeto/rhapi/repository/SolicitacaoFeriasRepository.java
package com.seuprojeto.rhapi.repository;

import com.seuprojeto.rhapi.domain.SolicitacaoFerias;
import com.seuprojeto.rhapi.domain.enums.StatusFerias;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface SolicitacaoFeriasRepository extends JpaRepository<SolicitacaoFerias, Long> {

    List<SolicitacaoFerias> findByColaborador_Id(Long colaboradorId);

    @Query("""
           select count(s) > 0
             from SolicitacaoFerias s
            where s.colaborador.id = :colabId
              and s.status in :status
              and s.dataInicio <= :dataFim
              and s.dataFim   >= :dataInicio
           """)
    boolean hasOverlap(@Param("colabId") Long colaboradorId,
                       @Param("status") List<StatusFerias> status,
                       @Param("dataInicio") LocalDate dataInicio,
                       @Param("dataFim") LocalDate dataFim);

    @Query("""
           select coalesce(sum(s.dias), 0)
             from SolicitacaoFerias s
            where s.colaborador.id = :colabId
              and s.status = :status
           """)
    Integer sumDiasByStatus(@Param("colabId") Long colaboradorId,
                            @Param("status") StatusFerias status);

    @Query("""
           select coalesce(sum(s.dias), 0)
             from SolicitacaoFerias s
            where s.colaborador.id = :colabId
              and s.status in :statuses
           """)
    Integer sumDiasByStatuses(@Param("colabId") Long colaboradorId,
                              @Param("statuses") List<StatusFerias> statuses);
}
