package br.csi.rep_hub.dto.tarefas;

import br.csi.rep_hub.model.tarefa.AtribuicaoTarefa;
import br.csi.rep_hub.model.tarefa.StatusAtribuicao;

import java.time.Instant;
import java.util.UUID;

public record AtribuicaoTarefaResposta(
        Long id,
        UUID uuid,
        UUID tarefaUuid,
        UUID responsavelParticipacaoUuid,
        UUID responsavelUsuarioUuid,
        String responsavelNome,
        UUID autorAtribuicaoParticipacaoUuid,
        UUID autorCancelamentoParticipacaoUuid,
        StatusAtribuicao status,
        Instant dataAtribuicao,
        Instant dataConclusao,
        Instant dataCancelamento) {

    public static AtribuicaoTarefaResposta de(AtribuicaoTarefa atribuicao) {
        return new AtribuicaoTarefaResposta(
                atribuicao.getId(),
                atribuicao.getUuid(),
                atribuicao.getTarefa().getUuid(),
                atribuicao.getResponsavel().getUuid(),
                atribuicao.getResponsavel().getUsuario().getUuid(),
                atribuicao.getResponsavel().getUsuario().getNome(),
                atribuicao.getAutorAtribuicao().getUuid(),
                atribuicao.getAutorCancelamento() == null
                        ? null : atribuicao.getAutorCancelamento().getUuid(),
                atribuicao.getStatus(),
                atribuicao.getDataAtribuicao(),
                atribuicao.getDataConclusao(),
                atribuicao.getDataCancelamento());
    }
}
