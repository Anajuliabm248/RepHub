package br.csi.rep_hub.dto.extratos;

import br.csi.rep_hub.model.extrato.ExtratoFinanceiro;

import java.time.YearMonth;
import java.util.List;
import java.util.UUID;

public record ExtratoIndividualResposta(
        UUID republicaUuid,
        List<UUID> participacaoUuids,
        PeriodoExtratoResposta periodo,
        IndicadoresFinanceirosResposta indicadores,
        List<SerieMensalResposta> serieMensal,
        List<GastoCategoriaResposta> categorias,
        ComparacaoPeriodoResposta comparacao) {

    public static ExtratoIndividualResposta de(
            UUID republicaUuid,
            List<UUID> participacaoUuids,
            ExtratoFinanceiro extrato,
            YearMonth mesAtual) {
        return new ExtratoIndividualResposta(
                republicaUuid, List.copyOf(participacaoUuids),
                PeriodoExtratoResposta.de(extrato.periodo(), mesAtual),
                IndicadoresFinanceirosResposta.de(extrato.indicadores()),
                extrato.serieMensal().stream().map(SerieMensalResposta::de).toList(),
                extrato.categorias().stream().map(GastoCategoriaResposta::de).toList(),
                ComparacaoPeriodoResposta.de(extrato.comparacao()));
    }
}
