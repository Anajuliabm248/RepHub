package br.csi.rep_hub.dto.extratos;

import br.csi.rep_hub.model.extrato.IndicadoresFinanceiros;

import java.math.BigDecimal;

public record IndicadoresFinanceirosResposta(
        BigDecimal custoTotal,
        BigDecimal pagamentosConfirmados,
        BigDecimal saldoAtual,
        BigDecimal mediaMensal,
        BigDecimal maiorCustoMensal,
        BigDecimal menorCustoMensal) {

    public static IndicadoresFinanceirosResposta de(IndicadoresFinanceiros indicadores) {
        return new IndicadoresFinanceirosResposta(
                indicadores.custoTotal(), indicadores.pagamentosConfirmados(),
                indicadores.saldoAtual(), indicadores.mediaMensal(),
                indicadores.maiorCustoMensal(), indicadores.menorCustoMensal());
    }
}
