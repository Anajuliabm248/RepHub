package br.csi.rep_hub.dto.despesas;

import br.csi.rep_hub.model.despesa.CategoriaDespesa;
import br.csi.rep_hub.model.despesa.Despesa;
import br.csi.rep_hub.model.despesa.TipoDivisao;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record DespesaResposta(
        Long id,
        UUID uuid,
        UUID republicaUuid,
        UUID criadorParticipacaoUuid,
        String titulo,
        String descricao,
        BigDecimal valorTotal,
        LocalDate dataReferencia,
        LocalDate dataVencimento,
        Instant dataCriacao,
        CategoriaDespesa categoria,
        TipoDivisao tipoDivisao,
        boolean cancelada,
        Instant dataCancelamento,
        UUID canceladaPorParticipacaoUuid,
        List<ParcelaDespesaResposta> parcelas) {

    public static DespesaResposta de(Despesa despesa, List<ParcelaDespesaResposta> parcelas) {
        return new DespesaResposta(
                despesa.getId(), despesa.getUuid(), despesa.getRepublica().getUuid(),
                despesa.getCriador().getUuid(), despesa.getTitulo(), despesa.getDescricao(),
                despesa.getValorTotal(), despesa.getDataReferencia(), despesa.getDataVencimento(),
                despesa.getDataCriacao(), despesa.getCategoria(), despesa.getTipoDivisao(),
                despesa.getDataCancelamento() != null, despesa.getDataCancelamento(),
                despesa.getCanceladaPor() == null ? null : despesa.getCanceladaPor().getUuid(),
                parcelas);
    }
}
