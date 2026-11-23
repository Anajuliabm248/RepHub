package br.csi.rep_hub.model.tarefa;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AtribuicaoTarefaRepository extends JpaRepository<AtribuicaoTarefa, Long> {
    Optional<AtribuicaoTarefa> findByUuid(UUID uuid);

    List<AtribuicaoTarefa> findByTarefaIdOrderById(Long tarefaId);

    boolean existsByTarefaIdAndResponsavelIdAndStatusNot(
            Long tarefaId, Long responsavelId, StatusAtribuicao status);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select a from AtribuicaoTarefa a where a.id = :id")
    Optional<AtribuicaoTarefa> bloquearPorId(@Param("id") Long id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select a from AtribuicaoTarefa a where a.uuid = :uuid")
    Optional<AtribuicaoTarefa> bloquearPorUuid(@Param("uuid") UUID uuid);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select a from AtribuicaoTarefa a where a.tarefa.id = :tarefaId " +
            "and a.status = :status order by a.id")
    List<AtribuicaoTarefa> bloquearPorTarefaEStatus(
            @Param("tarefaId") Long tarefaId, @Param("status") StatusAtribuicao status);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select a from AtribuicaoTarefa a where a.responsavel.id = :participacaoId " +
            "and a.status = :status order by a.id")
    List<AtribuicaoTarefa> bloquearPorResponsavelEStatus(
            @Param("participacaoId") Long participacaoId, @Param("status") StatusAtribuicao status);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select a from AtribuicaoTarefa a where a.tarefa.republica.id = :republicaId " +
            "and a.status = :status order by a.id")
    List<AtribuicaoTarefa> bloquearPorRepublicaEStatus(
            @Param("republicaId") Long republicaId, @Param("status") StatusAtribuicao status);
}
