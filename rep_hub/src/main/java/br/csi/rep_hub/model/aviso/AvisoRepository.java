package br.csi.rep_hub.model.aviso;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AvisoRepository extends JpaRepository<Aviso, Long> {
    Optional<Aviso> findByUuid(UUID uuid);

    List<Aviso> findByRepublicaIdAndDataRemocaoIsNullOrderByIdDesc(Long republicaId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select a from Aviso a where a.id = :id")
    Optional<Aviso> bloquearPorId(@Param("id") Long id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select a from Aviso a where a.uuid = :uuid")
    Optional<Aviso> bloquearPorUuid(@Param("uuid") UUID uuid);
}
