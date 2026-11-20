package br.csi.rep_hub.model.convite;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import jakarta.persistence.LockModeType;
import java.util.List;

public interface ConviteRepository extends JpaRepository<Convite, Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select c from Convite c where c.republica.id = :republicaId " +
            "and c.status = :status order by c.id")
    List<Convite> bloquearPorRepublicaEStatus(@Param("republicaId") Long republicaId,
                                               @Param("status") StatusConvite status);
}
