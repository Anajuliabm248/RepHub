package br.csi.rep_hub.model.extrato;

import java.math.BigDecimal;
import java.time.YearMonth;

public record SerieMensal(
        YearMonth mes,
        BigDecimal custo,
        BigDecimal pagamentosConfirmados,
        BigDecimal saldoAtual,
        boolean emAndamento) {
}
