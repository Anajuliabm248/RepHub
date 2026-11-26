package br.csi.rep_hub.dto.extratos;

import br.csi.rep_hub.model.despesa.CategoriaDespesa;
import br.csi.rep_hub.model.extrato.GastoCategoria;

import java.math.BigDecimal;

public record GastoCategoriaResposta(
        CategoriaDespesa categoria,
        BigDecimal valor,
        BigDecimal percentual) {

    public static GastoCategoriaResposta de(GastoCategoria gasto) {
        return new GastoCategoriaResposta(
                gasto.categoria(), gasto.valor(), gasto.percentual());
    }
}
