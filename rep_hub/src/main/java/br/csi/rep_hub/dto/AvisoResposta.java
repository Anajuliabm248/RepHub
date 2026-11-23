package br.csi.rep_hub.dto;

import br.csi.rep_hub.model.aviso.Aviso;

import java.time.Instant;
import java.util.UUID;

public record AvisoResposta(
        Long id,
        UUID uuid,
        UUID republicaUuid,
        UUID autorParticipacaoUuid,
        UUID autorUsuarioUuid,
        String titulo,
        String mensagem,
        Instant dataPublicacao,
        Instant dataEdicao) {

    public static AvisoResposta de(Aviso aviso) {
        return new AvisoResposta(
                aviso.getId(),
                aviso.getUuid(),
                aviso.getRepublica().getUuid(),
                aviso.getAutor().getUuid(),
                aviso.getAutor().getUsuario().getUuid(),
                aviso.getTitulo(),
                aviso.getMensagem(),
                aviso.getDataPublicacao(),
                aviso.getDataEdicao());
    }
}
