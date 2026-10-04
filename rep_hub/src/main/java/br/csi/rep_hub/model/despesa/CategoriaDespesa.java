package br.csi.rep_hub.model.despesa;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Categoria da despesa")
public enum CategoriaDespesa {
    ALUGUEL,
    AGUA,
    ENERGIA,
    INTERNET,
    MERCADO,
    LIMPEZA,
    MANUTENCAO,
    LAZER,
    OUTROS
}
