package br.csi.rep_hub.controller.dto;

import br.csi.rep_hub.model.participacao.PapelRepublica;
import jakarta.validation.constraints.NotNull;

public record ParticipacaoPapel(
        @NotNull
        PapelRepublica papel
) {}
