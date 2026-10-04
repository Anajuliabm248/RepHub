package br.csi.rep_hub.service;

import br.csi.rep_hub.controller.dto.RepublicaDados;
import br.csi.rep_hub.controller.dto.RepublicaResposta;
import br.csi.rep_hub.infra.AcessoRepublica;
import br.csi.rep_hub.infra.UsuarioAtual;
import br.csi.rep_hub.model.participacao.PapelRepublica;
import br.csi.rep_hub.model.participacao.ParticipacaoRepublica;
import br.csi.rep_hub.model.participacao.ParticipacaoRepublicaRepository;
import br.csi.rep_hub.model.participacao.StatusParticipacao;
import br.csi.rep_hub.model.republica.Republica;
import br.csi.rep_hub.model.republica.RepublicaRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class RepublicaService {
    private final RepublicaRepository republicas;
    private final ParticipacaoRepublicaRepository participacoes;
    private final UsuarioAtual usuarioAtual;
    private final AcessoRepublica acesso;

    public RepublicaService(RepublicaRepository republicas, ParticipacaoRepublicaRepository participacoes,
                            UsuarioAtual usuarioAtual, AcessoRepublica acesso) {
        this.republicas = republicas;
        this.participacoes = participacoes;
        this.usuarioAtual = usuarioAtual;
        this.acesso = acesso;
    }

    @Transactional
    public RepublicaResposta cadastrar(RepublicaDados dados) {
        if (!participacoes.findByUsuarioIdAndStatus(usuarioAtual.obter().getId(), StatusParticipacao.ATIVO).isEmpty()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "O usuário já participa de uma república ativa");
        }
        Republica republica = new Republica();
        republica.setNome(dados.nome().trim());
        republica.setDescricao(dados.descricao());
        republica.setAtiva(true);
        republica = republicas.saveAndFlush(republica);

        ParticipacaoRepublica fundador = new ParticipacaoRepublica();
        fundador.setRepublica(republica);
        fundador.setUsuario(usuarioAtual.obter());
        fundador.setPapel(PapelRepublica.ADMINISTRADOR);
        fundador.setStatus(StatusParticipacao.ATIVO);
        participacoes.saveAndFlush(fundador);
        return RepublicaResposta.de(republica);
    }

    @Transactional(readOnly = true)
    public List<RepublicaResposta> listarTodos() {
        return participacoes.findByUsuarioIdAndStatus(usuarioAtual.obter().getId(), StatusParticipacao.ATIVO)
                .stream().map(ParticipacaoRepublica::getRepublica)
                .filter(Republica::isAtiva).distinct().map(RepublicaResposta::de).toList();
    }

    @Transactional(readOnly = true)
    public RepublicaResposta porId(Long id) {
        Republica republica = buscar(id);
        acesso.exigirParticipante(republica);
        return RepublicaResposta.de(republica);
    }

    @Transactional(readOnly = true)
    public RepublicaResposta porUuid(UUID uuid) {
        Republica republica = buscar(uuid);
        acesso.exigirParticipante(republica);
        return RepublicaResposta.de(republica);
    }

    @Transactional
    public RepublicaResposta atualizar(Long id, RepublicaDados dados) {
        Republica republica = buscar(id);
        acesso.exigirAdministrador(republica);
        return aplicarAtualizacao(republica, dados);
    }

    @Transactional
    public RepublicaResposta atualizar(UUID uuid, RepublicaDados dados) {
        Republica republica = buscar(uuid);
        acesso.exigirAdministrador(republica);
        return aplicarAtualizacao(republica, dados);
    }

    @Transactional
    public void encerrar(Long id) {
        Republica republica = buscar(id);
        acesso.exigirAdministrador(republica);
        Instant agora = Instant.now();
        for (ParticipacaoRepublica participacao : participacoes.findByRepublicaIdAndStatus(
                republica.getId(), StatusParticipacao.ATIVO)) {
            participacao.setStatus(StatusParticipacao.INATIVO);
            participacao.setDataSaida(agora);
        }
        republica.setAtiva(false);
    }

    private RepublicaResposta aplicarAtualizacao(Republica republica, RepublicaDados dados) {
        republica.setNome(dados.nome().trim());
        republica.setDescricao(dados.descricao());
        return RepublicaResposta.de(republica);
    }

    private Republica buscar(Long id) {
        return republicas.findById(id).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "República não encontrada"));
    }

    private Republica buscar(UUID uuid) {
        return java.util.Optional.ofNullable(republicas.findRepublicaByUuid(uuid)).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "República não encontrada"));
    }
}
