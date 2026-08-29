package br.csi.rep_hub.model.participacao;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ParticipacaoRepublicaRepository extends JpaRepository<ParticipacaoRepublica, Long> {
    public ParticipacaoRepublica findByUuid(UUID uuid);
    public void deleteParticipacaoRepublicaByUuid(UUID uuid);
}
