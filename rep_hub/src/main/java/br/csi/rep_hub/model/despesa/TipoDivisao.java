package br.csi.rep_hub.model.despesa;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Tipo de divisao da despesa")
public enum TipoDivisao {
    IGUAL,
    PERSONALIZADA
}
