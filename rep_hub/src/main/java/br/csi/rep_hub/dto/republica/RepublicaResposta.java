package br.csi.rep_hub.dto.republica;

import br.csi.rep_hub.model.republica.Republica;
import java.time.Instant;
import java.util.UUID;

public record RepublicaResposta(Long id, UUID uuid, String nome, String descricao,
                                Instant dataCriacao, boolean ativa) {
    public static RepublicaResposta de(Republica republica) {
        return new RepublicaResposta(republica.getId(), republica.getUuid(), republica.getNome(),
                republica.getDescricao(), republica.getDataCriacao(), republica.isAtiva());
    }
}
