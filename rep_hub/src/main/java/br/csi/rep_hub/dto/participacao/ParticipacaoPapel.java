package br.csi.rep_hub.dto.participacao;

import br.csi.rep_hub.model.participacao.PapelRepublica;
import jakarta.validation.constraints.NotNull;

public record ParticipacaoPapel(
        @NotNull
        PapelRepublica papel
) {}
