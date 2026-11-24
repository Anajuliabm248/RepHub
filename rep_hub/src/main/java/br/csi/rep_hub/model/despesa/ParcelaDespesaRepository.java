package br.csi.rep_hub.model.despesa;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ParcelaDespesaRepository extends JpaRepository<ParcelaDespesa, Long> {
    Optional<ParcelaDespesa> findByUuid(UUID uuid);
    List<ParcelaDespesa> findByDespesaIdOrderByParticipacaoId(Long despesaId);
    List<ParcelaDespesa> findByParticipacaoIdOrderByIdDesc(Long participacaoId);
    List<ParcelaDespesa> findByParticipacao_Usuario_IdAndIncluidaTrueAndDespesa_DataCancelamentoIsNull(
            Long usuarioId);
    Optional<ParcelaDespesa> findByDespesaIdAndParticipacaoId(Long despesaId, Long participacaoId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from ParcelaDespesa p where p.id = :id")
    Optional<ParcelaDespesa> bloquearPorId(@Param("id") Long id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from ParcelaDespesa p where p.uuid = :uuid")
    Optional<ParcelaDespesa> bloquearPorUuid(@Param("uuid") UUID uuid);

}
