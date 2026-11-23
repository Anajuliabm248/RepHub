package br.csi.rep_hub.model.tarefa;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TarefaDomesticaRepository extends JpaRepository<TarefaDomestica, Long> {
    Optional<TarefaDomestica> findByUuid(UUID uuid);

    List<TarefaDomestica> findByRepublicaIdAndDataCancelamentoIsNullOrderByIdDesc(Long republicaId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select t from TarefaDomestica t where t.id = :id")
    Optional<TarefaDomestica> bloquearPorId(@Param("id") Long id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select t from TarefaDomestica t where t.uuid = :uuid")
    Optional<TarefaDomestica> bloquearPorUuid(@Param("uuid") UUID uuid);
}
