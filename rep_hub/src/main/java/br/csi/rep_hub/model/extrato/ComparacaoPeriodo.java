package br.csi.rep_hub.model.extrato;

import java.math.BigDecimal;
import java.time.YearMonth;

public record ComparacaoPeriodo(
        YearMonth inicio,
        YearMonth fim,
        BigDecimal custoPeriodoAnterior,
        BigDecimal diferencaAbsoluta,
        BigDecimal variacaoPercentual,
        TendenciaFinanceira tendencia) {
}
