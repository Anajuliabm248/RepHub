package br.csi.rep_hub.dto.extratos;

import br.csi.rep_hub.model.extrato.PeriodoExtrato;
import br.csi.rep_hub.model.extrato.TipoPeriodoExtrato;

import java.time.YearMonth;

public record PeriodoExtratoResposta(
        TipoPeriodoExtrato tipo,
        YearMonth inicio,
        YearMonth fim,
        int quantidadeMeses,
        boolean emAndamento) {

    public static PeriodoExtratoResposta de(PeriodoExtrato periodo, YearMonth mesAtual) {
        return new PeriodoExtratoResposta(
                periodo.tipo(), periodo.inicio(), periodo.fim(),
                periodo.quantidadeMeses(), periodo.emAndamento(mesAtual));
    }
}
