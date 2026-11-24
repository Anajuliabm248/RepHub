package br.csi.rep_hub.dto.despesas;

import br.csi.rep_hub.model.despesa.ParcelaDespesa;
import br.csi.rep_hub.model.despesa.SituacaoParcela;

import java.math.BigDecimal;
import java.util.UUID;

public record ParcelaDespesaResposta(
        Long id,
        UUID uuid,
        UUID despesaUuid,
        UUID participacaoUuid,
        UUID usuarioUuid,
        BigDecimal valor,
        boolean incluida,
        BigDecimal valorPago,
        BigDecimal saldo,
        SituacaoParcela situacao) {

    public static ParcelaDespesaResposta de(ParcelaDespesa parcela, BigDecimal valorPago) {
        BigDecimal pago = valorPago.setScale(2);
        return new ParcelaDespesaResposta(
                parcela.getId(), parcela.getUuid(), parcela.getDespesa().getUuid(),
                parcela.getParticipacao().getUuid(), parcela.getParticipacao().getUsuario().getUuid(),
                parcela.getValor(), parcela.isIncluida(), pago, parcela.saldo(pago),
                parcela.situacao(pago));
    }
}
