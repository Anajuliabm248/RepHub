package br.csi.rep_hub.service;

import br.csi.rep_hub.model.participacao.ParticipacaoRepublica;
import br.csi.rep_hub.model.republica.Republica;
import br.csi.rep_hub.model.tarefa.AtribuicaoTarefa;
import br.csi.rep_hub.model.tarefa.AtribuicaoTarefaRepository;
import br.csi.rep_hub.model.tarefa.StatusAtribuicao;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
public class AtribuicaoTarefaService {
    private final AtribuicaoTarefaRepository atribuicoes;

    public AtribuicaoTarefaService(AtribuicaoTarefaRepository atribuicoes) {
        this.atribuicoes = atribuicoes;
    }

    public void cancelarPendentesDaParticipacao(ParticipacaoRepublica participacao,
                                                 ParticipacaoRepublica autor,
                                                 Instant dataCancelamento) {
        cancelar(atribuicoes.bloquearPorResponsavelEStatus(
                participacao.getId(), StatusAtribuicao.PENDENTE), autor, dataCancelamento);
    }

    public void cancelarPendentesDaRepublica(Republica republica,
                                              ParticipacaoRepublica autor,
                                              Instant dataCancelamento) {
        cancelar(atribuicoes.bloquearPorRepublicaEStatus(
                republica.getId(), StatusAtribuicao.PENDENTE), autor, dataCancelamento);
    }

    private void cancelar(List<AtribuicaoTarefa> pendentes, ParticipacaoRepublica autor,
                          Instant dataCancelamento) {
        pendentes.forEach(atribuicao -> {
            atribuicao.setStatus(StatusAtribuicao.CANCELADA);
            atribuicao.setDataCancelamento(dataCancelamento);
            atribuicao.setAutorCancelamento(autor);
        });
    }
}
