package br.csi.rep_hub.model.republica;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import jakarta.persistence.LockModeType;

import java.util.Optional;
import java.util.UUID;

public interface RepublicaRepository extends JpaRepository<Republica, Long> {
    Republica findRepublicaByUuid(UUID uuid);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select r from Republica r where r.id = :id")
    Optional<Republica> bloquearPorId(@Param("id") Long id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select r from Republica r where r.uuid = :uuid")
    Optional<Republica> bloquearPorUuid(@Param("uuid") UUID uuid);
}
