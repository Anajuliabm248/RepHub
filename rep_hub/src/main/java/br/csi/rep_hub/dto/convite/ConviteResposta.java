package br.csi.rep_hub.dto.convite;

import br.csi.rep_hub.model.convite.Convite;
import br.csi.rep_hub.model.convite.SituacaoConvite;
import br.csi.rep_hub.model.convite.StatusConvite;

import java.time.Instant;
import java.util.UUID;

public record ConviteResposta(
        Long id,
        UUID uuid,
        String codigo,
        UUID republicaUuid,
        String republicaNome,
        UUID emissorParticipacaoUuid,
        UUID utilizadoPorUsuarioUuid,
        UUID participacaoCriadaUuid,
        UUID canceladoPorParticipacaoUuid,
        Instant dataCriacao,
        Instant dataExpiracao,
        Instant dataUtilizacao,
        Instant dataCancelamento,
        SituacaoConvite status) {

    public static ConviteResposta de(Convite convite, Instant agora) {
        SituacaoConvite situacao = convite.getStatus() == StatusConvite.DISPONIVEL
                && !agora.isBefore(convite.getDataExpiracao())
                ? SituacaoConvite.EXPIRADO
                : SituacaoConvite.valueOf(convite.getStatus().name());

        return new ConviteResposta(
                convite.getId(),
                convite.getUuid(),
                convite.getCodigo(),
                convite.getRepublica().getUuid(),
                convite.getRepublica().getNome(),
                convite.getEmissor().getUuid(),
                convite.getUtilizadoPor() == null ? null : convite.getUtilizadoPor().getUuid(),
                convite.getParticipacaoCriada() == null ? null : convite.getParticipacaoCriada().getUuid(),
                convite.getCanceladoPor() == null ? null : convite.getCanceladoPor().getUuid(),
                convite.getDataCriacao(),
                convite.getDataExpiracao(),
                convite.getDataUtilizacao(),
                convite.getDataCancelamento(),
                situacao);
    }
}
