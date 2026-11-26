package br.csi.rep_hub.model.extrato;

import java.math.BigDecimal;

public record IndicadoresFinanceiros(
        BigDecimal custoTotal,
        BigDecimal pagamentosConfirmados,
        BigDecimal saldoAtual,
        BigDecimal mediaMensal,
        BigDecimal maiorCustoMensal,
        BigDecimal menorCustoMensal) {
}
