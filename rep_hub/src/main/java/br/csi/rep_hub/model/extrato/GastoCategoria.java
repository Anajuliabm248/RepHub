package br.csi.rep_hub.model.extrato;

import br.csi.rep_hub.model.despesa.CategoriaDespesa;

import java.math.BigDecimal;

public record GastoCategoria(
        CategoriaDespesa categoria,
        BigDecimal valor,
        BigDecimal percentual) {
}
