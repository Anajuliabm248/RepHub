package br.csi.rep_hub.service;

import br.csi.rep_hub.dto.ConviteResposta;
import br.csi.rep_hub.dto.ParticipacaoResposta;
import br.csi.rep_hub.infra.AcessoRepublica;
import br.csi.rep_hub.infra.UsuarioAtual;
import br.csi.rep_hub.model.convite.Convite;
import br.csi.rep_hub.model.convite.ConviteRepository;
import br.csi.rep_hub.model.convite.CriarCodigoConvite;
import br.csi.rep_hub.model.convite.StatusConvite;
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

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Service
public class ConviteService {
    private static final Duration VALIDADE = Duration.ofHours(48);
    private static final int MAX_TENTATIVAS_CODIGO = 5;

    private final ConviteRepository convites;
    private final RepublicaRepository republicas;
    private final ParticipacaoRepublicaRepository participacoes;
    private final UsuarioRepository usuarios;
    private final UsuarioAtual usuarioAtual;
    private final AcessoRepublica acesso;

    public ConviteService(ConviteRepository convites, RepublicaRepository republicas,
                          ParticipacaoRepublicaRepository participacoes, UsuarioRepository usuarios,
                          UsuarioAtual usuarioAtual, AcessoRepublica acesso) {
        this.convites = convites;
        this.republicas = republicas;
        this.participacoes = participacoes;
        this.usuarios = usuarios;
        this.usuarioAtual = usuarioAtual;
        this.acesso = acesso;
    }

    @Transactional
    public ConviteResposta emitir(Long republicaId) {
        Republica republica = bloquearRepublica(republicaId);
        ParticipacaoRepublica emissor = acesso.exigirAdministrador(republica);
        Instant agora = Instant.now();

        Convite convite = new Convite();
        convite.setRepublica(republica);
        convite.setEmissor(emissor);
        convite.setDataCriacao(agora);
        convite.setDataExpiracao(agora.plus(VALIDADE));
        convite.setStatus(StatusConvite.DISPONIVEL);
        salvarComCodigoUnico(convite);

        return ConviteResposta.de(convite, agora);
    }

    @Transactional(readOnly = true)
    public List<ConviteResposta> listar(Long republicaId) {
        Republica republica = buscarRepublica(republicaId);
        acesso.exigirAdministrador(republica);
        Instant agora = Instant.now();
        return convites.findByRepublicaIdOrderByIdDesc(republicaId).stream()
                .map(convite -> resposta(convite, agora))
                .toList();
    }

    @Transactional(readOnly = true)
    public ConviteResposta porId(Long id) {
        Convite convite = buscar(id);
        acesso.exigirAdministrador(convite.getRepublica());
        return resposta(convite, Instant.now());
    }

    @Transactional(readOnly = true)
    public ConviteResposta porUuid(UUID uuid) {
        Convite convite = buscar(uuid);
        acesso.exigirAdministrador(convite.getRepublica());
        return resposta(convite, Instant.now());
    }

    @Transactional(readOnly = true)
    public ConviteResposta porCodigo(String codigo) {
        usuarioAtual.obter();
        Convite convite = validarVinculos(
                convites.findByCodigoIgnoreCase(normalizarCodigo(codigo)).orElseThrow(() ->
                        new ResponseStatusException(HttpStatus.NOT_FOUND, "Convite não encontrado")));
        if (!convite.getRepublica().isAtiva()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Convite não encontrado");
        }
        return resposta(convite, Instant.now());
    }

    @Transactional
    public void cancelar(Long id) {
        Convite conviteInicial = buscar(id);
        bloquearRepublica(conviteInicial.getRepublica().getId());
        Convite convite = convites.bloquearPorId(id).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "Convite não encontrado"));
        cancelar(convite);
    }

    @Transactional
    public void cancelar(UUID uuid) {
        Convite conviteInicial = buscar(uuid);
        bloquearRepublica(conviteInicial.getRepublica().getId());
        Convite convite = convites.bloquearPorUuid(uuid).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "Convite não encontrado"));
        cancelar(convite);
    }

    @Transactional
    public ParticipacaoResposta consumir(String codigo) {
        Usuario contaAutenticada = usuarioAtual.obter();
        Usuario usuario = usuarios.bloquearPorId(contaAutenticada.getId()).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuário não encontrado"));

        String codigoNormalizado = normalizarCodigo(codigo);
        Convite conviteInicial = validarVinculos(
                convites.findByCodigoIgnoreCase(codigoNormalizado).orElseThrow(() ->
                        new ResponseStatusException(HttpStatus.NOT_FOUND, "Convite não encontrado")));
        Republica republica = bloquearRepublica(conviteInicial.getRepublica().getId());
        Convite convite = convites.bloquearPorCodigo(codigoNormalizado).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "Convite não encontrado"));

        Instant agora = Instant.now();
        exigirDisponivel(convite, agora);
        if (!republica.isAtiva()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "A república do convite está encerrada");
        }
        if (!usuario.isAtivo()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "A conta está inativa");
        }
        if (!participacoes.findByUsuarioIdAndStatus(usuario.getId(), StatusParticipacao.ATIVO).isEmpty()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "O usuário já participa de uma república ativa");
        }

        ParticipacaoRepublica participacao = new ParticipacaoRepublica();
        participacao.setUsuario(usuario);
        participacao.setRepublica(republica);
        participacao.setPapel(PapelRepublica.MORADOR);
        participacao.setStatus(StatusParticipacao.ATIVO);
        participacao = participacoes.saveAndFlush(participacao);

        convite.setStatus(StatusConvite.UTILIZADO);
        convite.setDataUtilizacao(agora);
        convite.setUtilizadoPor(usuario);
        convite.setParticipacaoCriada(participacao);
        convites.saveAndFlush(convite);

        return ParticipacaoResposta.de(participacao);
    }

    public void cancelarUtilizaveisDaRepublica(Republica republica,
                                                ParticipacaoRepublica autor,
                                                Instant dataCancelamento) {
        convites.bloquearPorRepublicaEStatus(republica.getId(), StatusConvite.DISPONIVEL).stream()
                .filter(convite -> convite.getDataExpiracao() != null)
                .filter(convite -> dataCancelamento.isBefore(convite.getDataExpiracao()))
                .forEach(convite -> {
                    convite.setStatus(StatusConvite.CANCELADO);
                    convite.setDataCancelamento(dataCancelamento);
                    convite.setCanceladoPor(autor);
                });
    }

    private void cancelar(Convite convite) {
        ParticipacaoRepublica autor = acesso.exigirAdministrador(convite.getRepublica());
        Instant agora = Instant.now();
        exigirDisponivel(convite, agora);
        convite.setStatus(StatusConvite.CANCELADO);
        convite.setDataCancelamento(agora);
        convite.setCanceladoPor(autor);
    }

    private void exigirDisponivel(Convite convite, Instant agora) {
        if (convite.getStatus() != StatusConvite.DISPONIVEL) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Convite não está disponível");
        }
        if (convite.getDataExpiracao() == null || !agora.isBefore(convite.getDataExpiracao())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Convite expirado");
        }
    }

    private void salvarComCodigoUnico(Convite convite) {
        for (int tentativa = 0; tentativa < MAX_TENTATIVAS_CODIGO; tentativa++) {
            String codigo = CriarCodigoConvite.gerarCodigoConvite();
            if (!convites.existsByCodigoIgnoreCase(codigo)) {
                convite.setCodigo(codigo);
                convites.saveAndFlush(convite);
                return;
            }
        }
        throw new IllegalStateException("Não foi possível gerar um código de convite único");
    }

    private Convite buscar(Long id) {
        return validarVinculos(convites.findById(id).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "Convite não encontrado")));
    }

    private Convite buscar(UUID uuid) {
        return validarVinculos(convites.findByUuid(uuid).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "Convite não encontrado")));
    }

    private Republica buscarRepublica(Long id) {
        return republicas.findById(id).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "República não encontrada"));
    }

    private Republica bloquearRepublica(Long id) {
        return republicas.bloquearPorId(id).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "República não encontrada"));
    }

    private ConviteResposta resposta(Convite convite, Instant agora) {
        return ConviteResposta.de(validarVinculos(convite), agora);
    }

    private Convite validarVinculos(Convite convite) {
        if (convite.getRepublica() == null || convite.getEmissor() == null
                || convite.getDataCriacao() == null || convite.getDataExpiracao() == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Convite legado sem dados completos");
        }
        return convite;
    }

    private String normalizarCodigo(String codigo) {
        if (codigo == null || codigo.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Código do convite é obrigatório");
        }
        return codigo.trim().toUpperCase(Locale.ROOT);
    }
}
