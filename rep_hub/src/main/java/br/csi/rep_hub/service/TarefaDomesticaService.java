package br.csi.rep_hub.service;

import br.csi.rep_hub.dto.tarefas.TarefaDados;
import br.csi.rep_hub.dto.tarefas.TarefaResposta;
import br.csi.rep_hub.infra.AcessoRepublica;
import br.csi.rep_hub.model.participacao.PapelRepublica;
import br.csi.rep_hub.model.participacao.ParticipacaoRepublica;
import br.csi.rep_hub.model.republica.Republica;
import br.csi.rep_hub.model.republica.RepublicaRepository;
import br.csi.rep_hub.model.tarefa.StatusTarefa;
import br.csi.rep_hub.model.tarefa.TarefaDomestica;
import br.csi.rep_hub.model.tarefa.TarefaDomesticaRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class TarefaDomesticaService {
    private final TarefaDomesticaRepository tarefas;
    private final RepublicaRepository republicas;
    private final AcessoRepublica acesso;
    private final AtribuicaoTarefaService atribuicoes;

    public TarefaDomesticaService(TarefaDomesticaRepository tarefas,
                                   RepublicaRepository republicas,
                                   AcessoRepublica acesso,
                                   AtribuicaoTarefaService atribuicoes) {
        this.tarefas = tarefas;
        this.republicas = republicas;
        this.acesso = acesso;
        this.atribuicoes = atribuicoes;
    }

    @Transactional
    public TarefaResposta cadastrar(Long republicaId, TarefaDados dados) {
        Republica republica = buscarRepublica(republicaId);
        ParticipacaoRepublica criador = acesso.exigirParticipante(republica);

        TarefaDomestica tarefa = new TarefaDomestica();
        tarefa.setRepublica(republica);
        tarefa.setCriador(criador);
        aplicarDados(tarefa, dados);
        tarefa = tarefas.saveAndFlush(tarefa);
        return responder(tarefa);
    }

    @Transactional(readOnly = true)
    public List<TarefaResposta> listar(Long republicaId) {
        Republica republica = buscarRepublica(republicaId);
        acesso.exigirParticipante(republica);
        return tarefas.findByRepublicaIdAndDataCancelamentoIsNullOrderByIdDesc(republicaId)
                .stream().map(this::responder).toList();
    }

    @Transactional(readOnly = true)
    public TarefaResposta porId(Long id) {
        TarefaDomestica tarefa = buscar(id);
        acesso.exigirParticipante(tarefa.getRepublica());
        return responder(tarefa);
    }

    @Transactional(readOnly = true)
    public TarefaResposta porUuid(UUID uuid) {
        TarefaDomestica tarefa = buscar(uuid);
        acesso.exigirParticipante(tarefa.getRepublica());
        return responder(tarefa);
    }

    @Transactional
    public TarefaResposta atualizar(Long id, TarefaDados dados) {
        return atualizar(bloquear(id), dados);
    }

    @Transactional
    public TarefaResposta atualizar(UUID uuid, TarefaDados dados) {
        return atualizar(bloquear(uuid), dados);
    }

    @Transactional
    public void cancelar(Long id) {
        cancelar(bloquear(id));
    }

    @Transactional
    public void cancelar(UUID uuid) {
        cancelar(bloquear(uuid));
    }

    private TarefaResposta atualizar(TarefaDomestica tarefa, TarefaDados dados) {
        exigirCriadorOuAdministrador(tarefa);
        StatusTarefa status = status(tarefa);
        if (status == StatusTarefa.CANCELADA) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "A tarefa está cancelada");
        }
        if (status == StatusTarefa.CONCLUIDA) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Uma tarefa concluída não pode ser editada");
        }
        aplicarDados(tarefa, dados);
        return responder(tarefa);
    }

    private void cancelar(TarefaDomestica tarefa) {
        ParticipacaoRepublica autor = exigirCriadorOuAdministrador(tarefa);
        if (tarefa.getDataCancelamento() != null) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "A tarefa já está cancelada");
        }
        Instant agora = Instant.now();
        atribuicoes.cancelarPendentesDaTarefa(tarefa, autor, agora);
        tarefa.setDataCancelamento(agora);
        tarefa.setAutorCancelamento(autor);
    }

    private ParticipacaoRepublica exigirCriadorOuAdministrador(TarefaDomestica tarefa) {
        ParticipacaoRepublica ator = acesso.exigirParticipante(tarefa.getRepublica());
        if (!ator.getId().equals(tarefa.getCriador().getId())
                && ator.getPapel() != PapelRepublica.ADMINISTRADOR) {
            throw new AccessDeniedException("Ação restrita ao criador ou a um administrador");
        }
        return ator;
    }

    private void aplicarDados(TarefaDomestica tarefa, TarefaDados dados) {
        tarefa.setTitulo(dados.titulo().trim());
        tarefa.setDescricao(dados.descricao());
        tarefa.setPrazo(dados.prazo());
    }

    private StatusTarefa status(TarefaDomestica tarefa) {
        return TarefaResposta.calcularStatus(tarefa, atribuicoes.listarDaTarefa(tarefa.getId()));
    }

    private TarefaResposta responder(TarefaDomestica tarefa) {
        return TarefaResposta.de(
                validar(tarefa), atribuicoes.listarDaTarefa(tarefa.getId()), Instant.now());
    }

    private TarefaDomestica buscar(Long id) {
        return validar(tarefas.findById(id).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "Tarefa não encontrada")));
    }

    private TarefaDomestica buscar(UUID uuid) {
        return validar(tarefas.findByUuid(uuid).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "Tarefa não encontrada")));
    }

    private TarefaDomestica bloquear(Long id) {
        return validar(tarefas.bloquearPorId(id).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "Tarefa não encontrada")));
    }

    private TarefaDomestica bloquear(UUID uuid) {
        return validar(tarefas.bloquearPorUuid(uuid).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "Tarefa não encontrada")));
    }

    private Republica buscarRepublica(Long id) {
        return republicas.findById(id).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "República não encontrada"));
    }

    private TarefaDomestica validar(TarefaDomestica tarefa) {
        if (tarefa.getRepublica() == null || tarefa.getCriador() == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                    "Tarefa legada sem dados completos");
        }
        return tarefa;
    }
}
