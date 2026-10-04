package br.csi.rep_hub.model.participacao;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.UUID;
import java.util.List;
import java.util.Optional;

public interface ParticipacaoRepublicaRepository extends JpaRepository<ParticipacaoRepublica, Long> {
    public ParticipacaoRepublica findParticipacaoRepublicasByUuid(UUID uuid);
    List<ParticipacaoRepublica> findByUsuarioIdAndStatus(Long usuarioId, StatusParticipacao status);
    List<ParticipacaoRepublica> findByRepublicaIdAndStatus(Long republicaId, StatusParticipacao status);
    Optional<ParticipacaoRepublica> findByRepublicaIdAndUsuarioIdAndStatus(
            Long republicaId, Long usuarioId, StatusParticipacao status);
    long countByRepublicaIdAndPapelAndStatus(Long republicaId, PapelRepublica papel,
                                             StatusParticipacao status);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from ParticipacaoRepublica p where p.republica.id = :republicaId " +
            "and p.status = :status order by p.id")
    List<ParticipacaoRepublica> bloquearParticipacoesAtivas(@Param("republicaId") Long republicaId,
                                                              @Param("status") StatusParticipacao status);
}
