package br.csi.rep_hub.dto.extratos;

import br.csi.rep_hub.model.extrato.SerieMensal;

import java.math.BigDecimal;
import java.time.YearMonth;

public record SerieMensalResposta(
        YearMonth mes,
        BigDecimal custo,
        BigDecimal pagamentosConfirmados,
        BigDecimal saldoAtual,
        boolean emAndamento) {

    public static SerieMensalResposta de(SerieMensal serie) {
        return new SerieMensalResposta(
                serie.mes(), serie.custo(), serie.pagamentosConfirmados(),
                serie.saldoAtual(), serie.emAndamento());
    }
}
