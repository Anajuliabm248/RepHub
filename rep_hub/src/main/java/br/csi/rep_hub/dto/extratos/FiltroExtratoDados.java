package br.csi.rep_hub.dto.extratos;

import br.csi.rep_hub.model.extrato.TipoPeriodoExtrato;

import java.time.YearMonth;

public record FiltroExtratoDados(
        TipoPeriodoExtrato periodo,
        YearMonth mes,
        YearMonth inicio,
        YearMonth fim,
        Integer ano) {
}
