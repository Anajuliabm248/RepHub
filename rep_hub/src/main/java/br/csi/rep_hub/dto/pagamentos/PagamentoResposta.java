package br.csi.rep_hub.dto.pagamentos;

import br.csi.rep_hub.model.pagamento.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record PagamentoResposta(
        Long id,
        UUID uuid,
        UUID parcelaUuid,
        UUID participacaoDevedoraUuid,
        BigDecimal valor,
        LocalDate dataPagamento,
        Instant dataRegistro,
        FormaPagamento forma,
        FinalidadePagamento finalidade,
        String destinatario,
        String observacao,
        StatusPagamento status,
        UUID confirmadoPorParticipacaoUuid,
        Instant dataConfirmacao,
        UUID canceladoPorParticipacaoUuid,
        Instant dataCancelamento,
        String motivoCancelamento) {

    public static PagamentoResposta de(Pagamento pagamento) {
        return new PagamentoResposta(
                pagamento.getId(), pagamento.getUuid(), pagamento.getParcela().getUuid(),
                pagamento.getParcela().getParticipacao().getUuid(), pagamento.getValor(),
                pagamento.getDataPagamento(), pagamento.getDataRegistro(), pagamento.getForma(),
                pagamento.getFinalidade(), pagamento.getDestinatario(), pagamento.getObservacao(),
                pagamento.getStatus(),
                pagamento.getConfirmadoPor() == null ? null : pagamento.getConfirmadoPor().getUuid(),
                pagamento.getDataConfirmacao(),
                pagamento.getCanceladoPor() == null ? null : pagamento.getCanceladoPor().getUuid(),
                pagamento.getDataCancelamento(), pagamento.getMotivoCancelamento());
    }
}
