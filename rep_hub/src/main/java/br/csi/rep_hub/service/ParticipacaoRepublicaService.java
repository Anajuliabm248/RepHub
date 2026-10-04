package br.csi.rep_hub.service;

import br.csi.rep_hub.controller.dto.ParticipacaoPapel;
import br.csi.rep_hub.controller.dto.ParticipacaoResposta;
import br.csi.rep_hub.infra.AcessoRepublica;
import br.csi.rep_hub.infra.UsuarioAtual;
import br.csi.rep_hub.model.participacao.PapelRepublica;
import br.csi.rep_hub.model.participacao.ParticipacaoRepublica;
import br.csi.rep_hub.model.participacao.ParticipacaoRepublicaRepository;
import br.csi.rep_hub.model.participacao.StatusParticipacao;
import br.csi.rep_hub.model.republica.Republica;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class ParticipacaoRepublicaService {
    private final ParticipacaoRepublicaRepository participacoes;
    private final UsuarioAtual usuarioAtual;
    private final AcessoRepublica acesso;

    public ParticipacaoRepublicaService(ParticipacaoRepublicaRepository participacoes,
                                        UsuarioAtual usuarioAtual, AcessoRepublica acesso) {
        this.participacoes = participacoes;
        this.usuarioAtual = usuarioAtual;
        this.acesso = acesso;
    }

    @Transactional(readOnly = true)
    public List<ParticipacaoResposta> listarTodos() {
        return participacoes.findByUsuarioIdAndStatus(usuarioAtual.obter().getId(), StatusParticipacao.ATIVO)
                .stream().map(ParticipacaoRepublica::getRepublica)
                .filter(Republica::isAtiva)
                .flatMap(republica -> participacoes.findByRepublicaIdAndStatus(
                        republica.getId(), StatusParticipacao.ATIVO).stream())
                .distinct().map(ParticipacaoResposta::de).toList();
    }

    @Transactional(readOnly = true)
    public ParticipacaoResposta porId(Long id) {
        ParticipacaoRepublica participacao = buscar(id);
        acesso.exigirParticipante(participacao.getRepublica());
        return ParticipacaoResposta.de(participacao);
    }

    @Transactional(readOnly = true)
    public ParticipacaoResposta porUuid(UUID uuid) {
        ParticipacaoRepublica participacao = buscar(uuid);
        acesso.exigirParticipante(participacao.getRepublica());
        return ParticipacaoResposta.de(participacao);
    }

    @Transactional
    public ParticipacaoResposta alterarPapel(Long id, ParticipacaoPapel dados) {
        ParticipacaoRepublica participacao = buscar(id);
        return alterarPapel(participacao, dados);
    }

    @Transactional
    public ParticipacaoResposta alterarPapel(UUID uuid, ParticipacaoPapel dados) {
        ParticipacaoRepublica participacao = buscar(uuid);
        return alterarPapel(participacao, dados);
    }

    @Transactional
    public void encerrar(Long id) {
        ParticipacaoRepublica participacao = buscar(id);
        ParticipacaoRepublica ator = acesso.exigirParticipante(participacao.getRepublica());
        if (!ator.getUsuario().getId().equals(participacao.getUsuario().getId())
                && ator.getPapel() != PapelRepublica.ADMINISTRADOR) {
            throw new AccessDeniedException("Ação restrita ao titular ou a um administrador");
        }
        if (participacao.getStatus() != StatusParticipacao.ATIVO) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Participação já encerrada");
        }
        exigirOutroAdministradorSeNecessario(participacao);
        participacao.setStatus(StatusParticipacao.INATIVO);
        participacao.setDataSaida(Instant.now());
    }

    private ParticipacaoResposta alterarPapel(ParticipacaoRepublica participacao, ParticipacaoPapel dados) {
        acesso.exigirAdministrador(participacao.getRepublica());
        if (participacao.getStatus() != StatusParticipacao.ATIVO) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Participação já encerrada");
        }
        if (participacao.getPapel() == PapelRepublica.ADMINISTRADOR
                && dados.papel() != PapelRepublica.ADMINISTRADOR) {
            exigirOutroAdministradorSeNecessario(participacao);
        }
        participacao.setPapel(dados.papel());
        return ParticipacaoResposta.de(participacao);
    }

    private void exigirOutroAdministradorSeNecessario(ParticipacaoRepublica participacao) {
        if (participacao.getPapel() == PapelRepublica.ADMINISTRADOR) {
            Long republicaId = participacao.getRepublica().getId();
            participacoes.bloquearParticipacoesAtivas(republicaId, StatusParticipacao.ATIVO);
            if (participacoes.countByRepublicaIdAndPapelAndStatus(republicaId,
                    PapelRepublica.ADMINISTRADOR, StatusParticipacao.ATIVO) <= 1) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "A república precisa de um administrador ativo");
            }
        }
    }

    private ParticipacaoRepublica buscar(Long id) {
        return exigirVinculos(participacoes.findById(id).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "Participação não encontrada")));
    }

    private ParticipacaoRepublica buscar(UUID uuid) {
        return exigirVinculos(java.util.Optional.ofNullable(participacoes.findParticipacaoRepublicasByUuid(uuid))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Participação não encontrada")));
    }

    private ParticipacaoRepublica exigirVinculos(ParticipacaoRepublica participacao) {
        if (participacao.getUsuario() == null || participacao.getRepublica() == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Participação legada sem vínculos");
        }
        return participacao;
    }
}
