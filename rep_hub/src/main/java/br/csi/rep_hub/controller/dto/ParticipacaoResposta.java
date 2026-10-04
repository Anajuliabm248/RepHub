package br.csi.rep_hub.controller.dto;

import br.csi.rep_hub.model.participacao.PapelRepublica;
import br.csi.rep_hub.model.participacao.ParticipacaoRepublica;
import br.csi.rep_hub.model.participacao.StatusParticipacao;
import java.time.Instant;
import java.util.UUID;

public record ParticipacaoResposta(Long id, UUID uuid, UUID usuarioUuid, UUID republicaUuid,
                                   PapelRepublica papel, StatusParticipacao status,
                                   Instant dataEntrada, Instant dataSaida) {
    public static ParticipacaoResposta de(ParticipacaoRepublica participacao) {
        return new ParticipacaoResposta(participacao.getId(), participacao.getUuid(),
                participacao.getUsuario().getUuid(), participacao.getRepublica().getUuid(),
                participacao.getPapel(), participacao.getStatus(), participacao.getDataEntrada(),
                participacao.getDataSaida());
    }
}
