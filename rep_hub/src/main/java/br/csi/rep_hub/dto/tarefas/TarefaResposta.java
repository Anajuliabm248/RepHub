package br.csi.rep_hub.dto.tarefas;

import br.csi.rep_hub.model.tarefa.AtribuicaoTarefa;
import br.csi.rep_hub.model.tarefa.StatusAtribuicao;
import br.csi.rep_hub.model.tarefa.StatusTarefa;
import br.csi.rep_hub.model.tarefa.TarefaDomestica;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record TarefaResposta(
        Long id,
        UUID uuid,
        UUID republicaUuid,
        UUID criadorParticipacaoUuid,
        UUID criadorUsuarioUuid,
        String titulo,
        String descricao,
        Instant dataCriacao,
        Instant prazo,
        Instant dataCancelamento,
        UUID autorCancelamentoParticipacaoUuid,
        StatusTarefa status,
        boolean atrasada,
        List<AtribuicaoTarefaResposta> atribuicoes) {

    public static TarefaResposta de(TarefaDomestica tarefa,
                                     List<AtribuicaoTarefa> atribuicoes,
                                     Instant agora) {
        StatusTarefa status = calcularStatus(tarefa, atribuicoes);
        boolean atrasada = status == StatusTarefa.PENDENTE
                && tarefa.getPrazo() != null
                && !agora.isBefore(tarefa.getPrazo());

        return new TarefaResposta(
                tarefa.getId(),
                tarefa.getUuid(),
                tarefa.getRepublica().getUuid(),
                tarefa.getCriador().getUuid(),
                tarefa.getCriador().getUsuario().getUuid(),
                tarefa.getTitulo(),
                tarefa.getDescricao(),
                tarefa.getDataCriacao(),
                tarefa.getPrazo(),
                tarefa.getDataCancelamento(),
                tarefa.getAutorCancelamento() == null
                        ? null : tarefa.getAutorCancelamento().getUuid(),
                status,
                atrasada,
                atribuicoes.stream().map(AtribuicaoTarefaResposta::de).toList());
    }

    public static StatusTarefa calcularStatus(TarefaDomestica tarefa,
                                               List<AtribuicaoTarefa> atribuicoes) {
        if (tarefa.getDataCancelamento() != null) {
            return StatusTarefa.CANCELADA;
        }
        if (atribuicoes.stream().anyMatch(a -> a.getStatus() == StatusAtribuicao.PENDENTE)) {
            return StatusTarefa.PENDENTE;
        }
        if (atribuicoes.stream().anyMatch(a -> a.getStatus() == StatusAtribuicao.CONCLUIDA)) {
            return StatusTarefa.CONCLUIDA;
        }
        return StatusTarefa.PENDENTE;
    }
}
