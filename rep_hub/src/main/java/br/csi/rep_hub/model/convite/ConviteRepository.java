package br.csi.rep_hub.model.convite;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ConviteRepository extends JpaRepository<Convite, Long> {
    Optional<Convite> findByUuid(UUID uuid);
    Optional<Convite> findByCodigoIgnoreCase(String codigo);
    boolean existsByCodigoIgnoreCase(String codigo);
    List<Convite> findByRepublicaIdOrderByIdDesc(Long republicaId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select c from Convite c where c.id = :id")
    Optional<Convite> bloquearPorId(@Param("id") Long id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select c from Convite c where c.uuid = :uuid")
    Optional<Convite> bloquearPorUuid(@Param("uuid") UUID uuid);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select c from Convite c where upper(c.codigo) = upper(:codigo)")
    Optional<Convite> bloquearPorCodigo(@Param("codigo") String codigo);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select c from Convite c where c.republica.id = :republicaId " +
            "and c.status = :status order by c.id")
    List<Convite> bloquearPorRepublicaEStatus(@Param("republicaId") Long republicaId,
                                               @Param("status") StatusConvite status);
}
