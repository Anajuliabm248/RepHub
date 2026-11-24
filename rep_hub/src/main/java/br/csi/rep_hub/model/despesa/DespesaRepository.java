package br.csi.rep_hub.model.despesa;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface DespesaRepository extends JpaRepository<Despesa, Long> {
    Optional<Despesa> findByUuid(UUID uuid);
    List<Despesa> findByRepublicaIdOrderByDataReferenciaDescIdDesc(Long republicaId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select d from Despesa d where d.id = :id")
    Optional<Despesa> bloquearPorId(@Param("id") Long id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select d from Despesa d where d.uuid = :uuid")
    Optional<Despesa> bloquearPorUuid(@Param("uuid") UUID uuid);
}
