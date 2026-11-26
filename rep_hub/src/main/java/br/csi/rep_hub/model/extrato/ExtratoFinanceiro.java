package br.csi.rep_hub.model.extrato;

import java.util.List;

public record ExtratoFinanceiro(
        PeriodoExtrato periodo,
        IndicadoresFinanceiros indicadores,
        List<SerieMensal> serieMensal,
        List<GastoCategoria> categorias,
        ComparacaoPeriodo comparacao) {

    public ExtratoFinanceiro {
        serieMensal = List.copyOf(serieMensal);
        categorias = List.copyOf(categorias);
    }
}
