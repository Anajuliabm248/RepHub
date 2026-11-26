package br.csi.rep_hub.model.extrato;

import java.time.YearMonth;
import java.util.List;
import java.util.Objects;

public record PeriodoExtrato(
        TipoPeriodoExtrato tipo,
        List<YearMonth> meses) {

    public PeriodoExtrato {
        Objects.requireNonNull(tipo, "O tipo do período é obrigatório");
        if (meses == null || meses.isEmpty()) {
            throw new IllegalArgumentException("O período deve possuir ao menos um mês");
        }
        meses = List.copyOf(meses);
    }

    public YearMonth inicio() {
        return meses.getFirst();
    }

    public YearMonth fim() {
        return meses.getLast();
    }

    public int quantidadeMeses() {
        return meses.size();
    }

    public boolean emAndamento(YearMonth mesAtual) {
        return meses.contains(mesAtual);
    }
}
