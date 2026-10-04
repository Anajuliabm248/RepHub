package br.csi.rep_hub.service;

import br.csi.rep_hub.controller.dto.UsuarioAtualizacao;
import br.csi.rep_hub.controller.dto.UsuarioCadastro;
import br.csi.rep_hub.controller.dto.UsuarioResposta;
import br.csi.rep_hub.infra.UsuarioAtual;
import br.csi.rep_hub.model.participacao.PapelRepublica;
import br.csi.rep_hub.model.participacao.ParticipacaoRepublica;
import br.csi.rep_hub.model.participacao.ParticipacaoRepublicaRepository;
import br.csi.rep_hub.model.participacao.StatusParticipacao;
import br.csi.rep_hub.model.usuario.Usuario;
import br.csi.rep_hub.model.usuario.UsuarioRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.Locale;
import java.util.UUID;

@Service
public class UsuarioService {
    private final UsuarioRepository usuarios;
    private final ParticipacaoRepublicaRepository participacoes;
    private final PasswordEncoder senhas;
    private final UsuarioAtual usuarioAtual;

    public UsuarioService(UsuarioRepository usuarios, ParticipacaoRepublicaRepository participacoes,
                          PasswordEncoder senhas, UsuarioAtual usuarioAtual) {
        this.usuarios = usuarios;
        this.participacoes = participacoes;
        this.senhas = senhas;
        this.usuarioAtual = usuarioAtual;
    }

    @Transactional
    public UsuarioResposta cadastrar(UsuarioCadastro dados) {
        String email = dados.email().trim().toLowerCase(Locale.ROOT);
        if (usuarios.existsByEmailIgnoreCase(email)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "E-mail já cadastrado");
        }
        Usuario usuario = new Usuario();
        usuario.setNome(dados.nome().trim());
        usuario.setEmail(email);
        usuario.setSenha(senhas.encode(dados.senha()));
        usuario.setAtivo(true);
        return UsuarioResposta.de(usuarios.saveAndFlush(usuario));
    }

    @Transactional(readOnly = true)
    public UsuarioResposta atual() {
        return UsuarioResposta.de(usuarioAtual.obter());
    }

    @Transactional(readOnly = true)
    public UsuarioResposta porId(Long id) {
        Usuario usuario = buscar(id);
        usuarioAtual.exigirTitular(usuario);
        return UsuarioResposta.de(usuario);
    }

    @Transactional(readOnly = true)
    public UsuarioResposta porUuid(UUID uuid) {
        Usuario usuario = buscar(uuid);
        usuarioAtual.exigirTitular(usuario);
        return UsuarioResposta.de(usuario);
    }

    @Transactional
    public UsuarioResposta atualizar(Long id, UsuarioAtualizacao dados) {
        Usuario usuario = buscar(id);
        usuarioAtual.exigirTitular(usuario);
        return aplicarAtualizacao(usuario, dados);
    }

    @Transactional
    public UsuarioResposta atualizar(UUID uuid, UsuarioAtualizacao dados) {
        Usuario usuario = buscar(uuid);
        usuarioAtual.exigirTitular(usuario);
        return aplicarAtualizacao(usuario, dados);
    }

    @Transactional
    public void desativar(Long id) {
        Usuario usuario = buscar(id);
        usuarioAtual.exigirTitular(usuario);
        desativar(usuario);
    }

    @Transactional
    public void desativar(UUID uuid) {
        Usuario usuario = buscar(uuid);
        usuarioAtual.exigirTitular(usuario);
        desativar(usuario);
    }

    private UsuarioResposta aplicarAtualizacao(Usuario usuario, UsuarioAtualizacao dados) {
        String email = dados.email().trim().toLowerCase(Locale.ROOT);
        if (!usuario.getEmail().equalsIgnoreCase(email) && usuarios.existsByEmailIgnoreCase(email)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "E-mail já cadastrado");
        }
        usuario.setNome(dados.nome().trim());
        usuario.setEmail(email);
        if (dados.senha() != null) {
            usuario.setSenha(senhas.encode(dados.senha()));
        }
        return UsuarioResposta.de(usuarios.save(usuario));
    }

    private void desativar(Usuario usuario) {
        for (ParticipacaoRepublica participacao : participacoes.findByUsuarioIdAndStatus(
                usuario.getId(), StatusParticipacao.ATIVO)) {
            if (participacao.getPapel() == PapelRepublica.ADMINISTRADOR
                    && participacao.getRepublica().isAtiva()) {
                Long republicaId = participacao.getRepublica().getId();
                participacoes.bloquearParticipacoesAtivas(republicaId, StatusParticipacao.ATIVO);
                if (participacoes.countByRepublicaIdAndPapelAndStatus(republicaId,
                        PapelRepublica.ADMINISTRADOR, StatusParticipacao.ATIVO) <= 1) {
                    throw new ResponseStatusException(HttpStatus.CONFLICT,
                            "Transfira a administração antes de desativar a conta");
                }
            }
            participacao.setStatus(StatusParticipacao.INATIVO);
            participacao.setDataSaida(Instant.now());
        }
        usuario.setAtivo(false);
    }

    private Usuario buscar(Long id) {
        return usuarios.findById(id).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuário não encontrado"));
    }

    private Usuario buscar(UUID uuid) {
        return java.util.Optional.ofNullable(usuarios.findUsuariosByUuid(uuid)).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuário não encontrado"));
    }
}
