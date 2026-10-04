package br.csi.rep_hub.model.convite;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Status do convite")
public enum StatusConvite {
    DISPONIVEL,
    UTILIZADO,
    CANCELADO
}
