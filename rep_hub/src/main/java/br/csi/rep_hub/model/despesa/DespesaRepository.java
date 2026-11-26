package br.csi.rep_hub.model.despesa;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import jakarta.persistence.LockModeType;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface DespesaRepository extends JpaRepository<Despesa, Long> {
    Optional<Despesa> findByUuid(UUID uuid);
    List<Despesa> findByRepublicaIdOrderByDataReferenciaDescIdDesc(Long republicaId);

    @Query("select d from Despesa d where d.republica.id = :republicaId " +
            "and d.dataCancelamento is null and d.dataReferencia >= :inicio " +
            "and d.dataReferencia < :fim order by d.dataReferencia, d.id")
    List<Despesa> buscarAtivasNoPeriodo(@Param("republicaId") Long republicaId,
                                         @Param("inicio") LocalDate inicio,
                                         @Param("fim") LocalDate fim);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select d from Despesa d where d.id = :id")
    Optional<Despesa> bloquearPorId(@Param("id") Long id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select d from Despesa d where d.uuid = :uuid")
    Optional<Despesa> bloquearPorUuid(@Param("uuid") UUID uuid);
}
