package br.csi.rep_hub.service;

import br.csi.rep_hub.dto.republica.RepublicaDados;
import br.csi.rep_hub.dto.republica.RepublicaResposta;
import br.csi.rep_hub.infra.AcessoRepublica;
import br.csi.rep_hub.infra.UsuarioAtual;
import br.csi.rep_hub.model.participacao.PapelRepublica;
import br.csi.rep_hub.model.participacao.ParticipacaoRepublica;
import br.csi.rep_hub.model.participacao.ParticipacaoRepublicaRepository;
import br.csi.rep_hub.model.participacao.StatusParticipacao;
import br.csi.rep_hub.model.republica.Republica;
import br.csi.rep_hub.model.republica.RepublicaRepository;
import br.csi.rep_hub.model.usuario.Usuario;
import br.csi.rep_hub.model.usuario.UsuarioRepository;
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
    private final UsuarioRepository usuarios;
    private final UsuarioAtual usuarioAtual;
    private final AcessoRepublica acesso;
    private final ConviteService convites;
    private final AtribuicaoTarefaService atribuicoes;

    public RepublicaService(RepublicaRepository republicas, ParticipacaoRepublicaRepository participacoes,
                            UsuarioRepository usuarios, UsuarioAtual usuarioAtual, AcessoRepublica acesso,
                            ConviteService convites, AtribuicaoTarefaService atribuicoes) {
        this.republicas = republicas;
        this.participacoes = participacoes;
        this.usuarios = usuarios;
        this.usuarioAtual = usuarioAtual;
        this.acesso = acesso;
        this.convites = convites;
        this.atribuicoes = atribuicoes;
    }

    @Transactional
    public RepublicaResposta cadastrar(RepublicaDados dados) {
        Usuario usuario = usuarioAtual.obter();
        Usuario usuarioFundador = usuarios.bloquearPorId(usuario.getId()).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuário não encontrado"));
        if (!participacoes.findByUsuarioIdAndStatus(
                usuarioFundador.getId(), StatusParticipacao.ATIVO).isEmpty()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "O usuário já participa de uma república ativa");
        }
        Republica republica = new Republica();
        republica.setNome(dados.nome().trim());
        republica.setDescricao(dados.descricao());
        republica.setAtiva(true);
        republica = republicas.saveAndFlush(republica);

        ParticipacaoRepublica participacaoFundador = new ParticipacaoRepublica();
        participacaoFundador.setRepublica(republica);
        participacaoFundador.setUsuario(usuarioFundador);
        participacaoFundador.setPapel(PapelRepublica.ADMINISTRADOR);
        participacaoFundador.setStatus(StatusParticipacao.ATIVO);
        participacoes.saveAndFlush(participacaoFundador);
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
        Republica republica = buscarParaAtualizacao(id);
        acesso.exigirAdministrador(republica);
        return aplicarAtualizacao(republica, dados);
    }

    @Transactional
    public RepublicaResposta atualizar(UUID uuid, RepublicaDados dados) {
        Republica republica = buscarParaAtualizacao(uuid);
        acesso.exigirAdministrador(republica);
        return aplicarAtualizacao(republica, dados);
    }

    @Transactional
    public void encerrar(Long id) {
        Republica republica = buscarParaAtualizacao(id);
        List<ParticipacaoRepublica> participacoesAtivas = participacoes.bloquearParticipacoesAtivas(
                republica.getId(), StatusParticipacao.ATIVO);
        ParticipacaoRepublica administrador = acesso.exigirAdministrador(republica);
        Instant agora = Instant.now();

        convites.cancelarUtilizaveisDaRepublica(republica, administrador, agora);
        atribuicoes.cancelarPendentesDaRepublica(republica, administrador, agora);

        for (ParticipacaoRepublica participacao : participacoesAtivas) {
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

    private Republica buscarParaAtualizacao(Long id) {
        return republicas.bloquearPorId(id).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "República não encontrada"));
    }

    private Republica buscarParaAtualizacao(UUID uuid) {
        return republicas.bloquearPorUuid(uuid).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "República não encontrada"));
    }
}
