package br.csi.rep_hub.service;

import br.csi.rep_hub.dto.tarefas.AtribuicaoTarefaDados;
import br.csi.rep_hub.dto.tarefas.AtribuicaoTarefaResposta;
import br.csi.rep_hub.dto.tarefas.TarefaResposta;
import br.csi.rep_hub.infra.AcessoRepublica;
import br.csi.rep_hub.model.participacao.PapelRepublica;
import br.csi.rep_hub.model.participacao.ParticipacaoRepublica;
import br.csi.rep_hub.model.participacao.ParticipacaoRepublicaRepository;
import br.csi.rep_hub.model.participacao.StatusParticipacao;
import br.csi.rep_hub.model.republica.Republica;
import br.csi.rep_hub.model.tarefa.AtribuicaoTarefa;
import br.csi.rep_hub.model.tarefa.AtribuicaoTarefaRepository;
import br.csi.rep_hub.model.tarefa.StatusAtribuicao;
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
public class AtribuicaoTarefaService {
    private final AtribuicaoTarefaRepository atribuicoes;
    private final TarefaDomesticaRepository tarefas;
    private final ParticipacaoRepublicaRepository participacoes;
    private final AcessoRepublica acesso;

    public AtribuicaoTarefaService(AtribuicaoTarefaRepository atribuicoes,
                                    TarefaDomesticaRepository tarefas,
                                    ParticipacaoRepublicaRepository participacoes,
                                    AcessoRepublica acesso) {
        this.atribuicoes = atribuicoes;
        this.tarefas = tarefas;
        this.participacoes = participacoes;
        this.acesso = acesso;
    }

    @Transactional
    public AtribuicaoTarefaResposta atribuir(Long tarefaId, AtribuicaoTarefaDados dados) {
        TarefaDomestica tarefa = bloquearTarefa(tarefaId);
        ParticipacaoRepublica autor = acesso.exigirParticipante(tarefa.getRepublica());
        exigirTarefaPendente(tarefa);

        ParticipacaoRepublica responsavel = participacoes.findById(dados.participacaoResponsavelId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Participação responsável não encontrada"));
        exigirResponsavelValido(tarefa, responsavel);

        if (atribuicoes.existsByTarefaIdAndResponsavelIdAndStatusNot(
                tarefa.getId(), responsavel.getId(), StatusAtribuicao.CANCELADA)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "A participação já possui uma atribuição não cancelada nesta tarefa");
        }

        AtribuicaoTarefa atribuicao = new AtribuicaoTarefa();
        atribuicao.setTarefa(tarefa);
        atribuicao.setResponsavel(responsavel);
        atribuicao.setAutorAtribuicao(autor);
        atribuicao.setStatus(StatusAtribuicao.PENDENTE);
        return AtribuicaoTarefaResposta.de(atribuicoes.saveAndFlush(atribuicao));
    }

    @Transactional(readOnly = true)
    public AtribuicaoTarefaResposta porId(Long id) {
        AtribuicaoTarefa atribuicao = buscar(id);
        acesso.exigirParticipante(atribuicao.getTarefa().getRepublica());
        return AtribuicaoTarefaResposta.de(atribuicao);
    }

    @Transactional(readOnly = true)
    public AtribuicaoTarefaResposta porUuid(UUID uuid) {
        AtribuicaoTarefa atribuicao = buscar(uuid);
        acesso.exigirParticipante(atribuicao.getTarefa().getRepublica());
        return AtribuicaoTarefaResposta.de(atribuicao);
    }

    @Transactional
    public AtribuicaoTarefaResposta concluir(Long id) {
        AtribuicaoTarefa atribuicaoInicial = buscar(id);
        TarefaDomestica tarefa = bloquearTarefa(atribuicaoInicial.getTarefa().getId());
        AtribuicaoTarefa atribuicao = atribuicoes.bloquearPorId(id).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "Atribuição não encontrada"));
        return concluir(tarefa, atribuicao);
    }

    @Transactional
    public AtribuicaoTarefaResposta concluir(UUID uuid) {
        AtribuicaoTarefa atribuicaoInicial = buscar(uuid);
        TarefaDomestica tarefa = bloquearTarefa(atribuicaoInicial.getTarefa().getId());
        AtribuicaoTarefa atribuicao = atribuicoes.bloquearPorUuid(uuid).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "Atribuição não encontrada"));
        return concluir(tarefa, atribuicao);
    }

    @Transactional
    public AtribuicaoTarefaResposta reabrir(Long id) {
        AtribuicaoTarefa atribuicaoInicial = buscar(id);
        TarefaDomestica tarefa = bloquearTarefa(atribuicaoInicial.getTarefa().getId());
        AtribuicaoTarefa atribuicao = atribuicoes.bloquearPorId(id).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "Atribuição não encontrada"));
        return reabrir(tarefa, atribuicao);
    }

    @Transactional
    public AtribuicaoTarefaResposta reabrir(UUID uuid) {
        AtribuicaoTarefa atribuicaoInicial = buscar(uuid);
        TarefaDomestica tarefa = bloquearTarefa(atribuicaoInicial.getTarefa().getId());
        AtribuicaoTarefa atribuicao = atribuicoes.bloquearPorUuid(uuid).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "Atribuição não encontrada"));
        return reabrir(tarefa, atribuicao);
    }

    @Transactional
    public void cancelar(Long id) {
        AtribuicaoTarefa atribuicaoInicial = buscar(id);
        TarefaDomestica tarefa = bloquearTarefa(atribuicaoInicial.getTarefa().getId());
        AtribuicaoTarefa atribuicao = atribuicoes.bloquearPorId(id).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "Atribuição não encontrada"));
        cancelar(tarefa, atribuicao);
    }

    @Transactional
    public void cancelar(UUID uuid) {
        AtribuicaoTarefa atribuicaoInicial = buscar(uuid);
        TarefaDomestica tarefa = bloquearTarefa(atribuicaoInicial.getTarefa().getId());
        AtribuicaoTarefa atribuicao = atribuicoes.bloquearPorUuid(uuid).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "Atribuição não encontrada"));
        cancelar(tarefa, atribuicao);
    }

    public List<AtribuicaoTarefa> listarDaTarefa(Long tarefaId) {
        return atribuicoes.findByTarefaIdOrderById(tarefaId);
    }

    public void cancelarPendentesDaTarefa(TarefaDomestica tarefa,
                                           ParticipacaoRepublica autor,
                                           Instant dataCancelamento) {
        cancelar(atribuicoes.bloquearPorTarefaEStatus(
                tarefa.getId(), StatusAtribuicao.PENDENTE), autor, dataCancelamento);
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

    private AtribuicaoTarefaResposta concluir(TarefaDomestica tarefa, AtribuicaoTarefa atribuicao) {
        exigirResponsavelAtivo(tarefa, atribuicao);
        exigirTarefaAtiva(tarefa);
        if (atribuicao.getStatus() != StatusAtribuicao.PENDENTE) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "A atribuição não está pendente");
        }
        atribuicao.concluir(Instant.now());
        return AtribuicaoTarefaResposta.de(atribuicao);
    }

    private AtribuicaoTarefaResposta reabrir(TarefaDomestica tarefa, AtribuicaoTarefa atribuicao) {
        exigirResponsavelAtivo(tarefa, atribuicao);
        exigirTarefaAtiva(tarefa);
        if (atribuicao.getStatus() != StatusAtribuicao.CONCLUIDA) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "A atribuição não está concluída");
        }
        atribuicao.reabrir();
        return AtribuicaoTarefaResposta.de(atribuicao);
    }

    private void cancelar(TarefaDomestica tarefa, AtribuicaoTarefa atribuicao) {
        ParticipacaoRepublica ator = acesso.exigirParticipante(tarefa.getRepublica());
        exigirTarefaAtiva(tarefa);
        if (atribuicao.getStatus() != StatusAtribuicao.PENDENTE) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Somente uma atribuição pendente pode ser cancelada");
        }
        boolean autorizado = ator.getId().equals(atribuicao.getResponsavel().getId())
                || ator.getId().equals(atribuicao.getAutorAtribuicao().getId())
                || ator.getPapel() == PapelRepublica.ADMINISTRADOR;
        if (!autorizado) {
            throw new AccessDeniedException(
                    "Ação restrita ao responsável, autor da atribuição ou administrador");
        }
        atribuicao.cancelar(ator, Instant.now());
    }

    private void exigirResponsavelAtivo(TarefaDomestica tarefa,
                                         AtribuicaoTarefa atribuicao) {
        ParticipacaoRepublica ator = acesso.exigirParticipante(tarefa.getRepublica());
        if (!ator.getId().equals(atribuicao.getResponsavel().getId())) {
            throw new AccessDeniedException("Ação restrita ao responsável pela atribuição");
        }
    }

    private void exigirResponsavelValido(TarefaDomestica tarefa,
                                          ParticipacaoRepublica responsavel) {
        if (responsavel.getRepublica() == null
                || !responsavel.getRepublica().getId().equals(tarefa.getRepublica().getId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "A participação responsável pertence a outra república");
        }
        if (responsavel.getStatus() != StatusParticipacao.ATIVO) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "A participação responsável está encerrada");
        }
    }

    private void exigirTarefaPendente(TarefaDomestica tarefa) {
        exigirTarefaAtiva(tarefa);
        StatusTarefa status = TarefaResposta.calcularStatus(tarefa, listarDaTarefa(tarefa.getId()));
        if (status != StatusTarefa.PENDENTE) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Uma tarefa concluída não recebe novas atribuições");
        }
    }

    private void exigirTarefaAtiva(TarefaDomestica tarefa) {
        if (tarefa.getDataCancelamento() != null) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "A tarefa está cancelada");
        }
    }

    private TarefaDomestica bloquearTarefa(Long id) {
        return validarTarefa(tarefas.bloquearPorId(id).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "Tarefa não encontrada")));
    }

    private AtribuicaoTarefa buscar(Long id) {
        return validarAtribuicao(atribuicoes.findById(id).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "Atribuição não encontrada")));
    }

    private AtribuicaoTarefa buscar(UUID uuid) {
        return validarAtribuicao(atribuicoes.findByUuid(uuid).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "Atribuição não encontrada")));
    }

    private AtribuicaoTarefa validarAtribuicao(AtribuicaoTarefa atribuicao) {
        if (atribuicao.getTarefa() == null || atribuicao.getResponsavel() == null
                || atribuicao.getAutorAtribuicao() == null || atribuicao.getStatus() == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                    "Atribuição legada sem dados completos");
        }
        validarTarefa(atribuicao.getTarefa());
        return atribuicao;
    }

    private TarefaDomestica validarTarefa(TarefaDomestica tarefa) {
        if (tarefa.getRepublica() == null || tarefa.getCriador() == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                    "Tarefa legada sem dados completos");
        }
        return tarefa;
    }

    private void cancelar(List<AtribuicaoTarefa> pendentes, ParticipacaoRepublica autor,
                          Instant dataCancelamento) {
        pendentes.forEach(atribuicao -> atribuicao.cancelar(autor, dataCancelamento));
    }
}
