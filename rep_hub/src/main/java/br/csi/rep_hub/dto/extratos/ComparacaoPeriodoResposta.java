package br.csi.rep_hub.dto.extratos;

import br.csi.rep_hub.model.extrato.ComparacaoPeriodo;
import br.csi.rep_hub.model.extrato.TendenciaFinanceira;

import java.math.BigDecimal;
import java.time.YearMonth;

public record ComparacaoPeriodoResposta(
        YearMonth inicio,
        YearMonth fim,
        BigDecimal custoPeriodoAnterior,
        BigDecimal diferencaAbsoluta,
        BigDecimal variacaoPercentual,
        TendenciaFinanceira tendencia) {

    public static ComparacaoPeriodoResposta de(ComparacaoPeriodo comparacao) {
        return new ComparacaoPeriodoResposta(
                comparacao.inicio(), comparacao.fim(),
                comparacao.custoPeriodoAnterior(), comparacao.diferencaAbsoluta(),
                comparacao.variacaoPercentual(), comparacao.tendencia());
    }
}
