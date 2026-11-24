package br.csi.rep_hub.service;

import br.csi.rep_hub.dto.auth.UsuarioAtualizacao;
import br.csi.rep_hub.dto.auth.UsuarioCadastro;
import br.csi.rep_hub.dto.auth.UsuarioResposta;
import br.csi.rep_hub.infra.UsuarioAtual;
import br.csi.rep_hub.model.participacao.PapelRepublica;
import br.csi.rep_hub.model.participacao.ParticipacaoRepublica;
import br.csi.rep_hub.model.participacao.ParticipacaoRepublicaRepository;
import br.csi.rep_hub.model.participacao.StatusParticipacao;
import br.csi.rep_hub.model.usuario.Usuario;
import br.csi.rep_hub.model.usuario.UsuarioRepository;
import br.csi.rep_hub.model.despesa.ParcelaDespesaRepository;
import br.csi.rep_hub.model.pagamento.PagamentoRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.math.BigDecimal;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Service
public class UsuarioService {
    private final UsuarioRepository usuarios;
    private final ParticipacaoRepublicaRepository participacoes;
    private final PasswordEncoder senhas;
    private final UsuarioAtual usuarioAtual;
    private final AtribuicaoTarefaService atribuicoes;
    private final ParcelaDespesaRepository parcelas;
    private final PagamentoRepository pagamentos;

    public UsuarioService(UsuarioRepository usuarios, ParticipacaoRepublicaRepository participacoes,
                          PasswordEncoder senhas, UsuarioAtual usuarioAtual,
                          AtribuicaoTarefaService atribuicoes,
                          ParcelaDespesaRepository parcelas,
                          PagamentoRepository pagamentos) {
        this.usuarios = usuarios;
        this.participacoes = participacoes;
        this.senhas = senhas;
        this.usuarioAtual = usuarioAtual;
        this.atribuicoes = atribuicoes;
        this.parcelas = parcelas;
        this.pagamentos = pagamentos;
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
        Usuario usuarioBloqueado = usuarios.bloquearPorId(usuario.getId()).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuário não encontrado"));
        List<ParticipacaoRepublica> participacoesAtivas =
                participacoes.bloquearParticipacoesAtivasDoUsuario(
                        usuarioBloqueado.getId(), StatusParticipacao.ATIVO);

        boolean parcelaEmAberto = parcelas
                .findByParticipacao_Usuario_IdAndIncluidaTrueAndDespesa_DataCancelamentoIsNull(
                        usuarioBloqueado.getId())
                .stream()
                .anyMatch(parcela -> {
                    BigDecimal pago = pagamentos.somarConfirmados(parcela.getId());
                    return parcela.getValor().compareTo(
                            pago == null ? BigDecimal.ZERO : pago) > 0;
                });
        if (parcelaEmAberto
                || pagamentos.contarPendentesDoUsuario(usuarioBloqueado.getId()) > 0) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Quite as parcelas e resolva os pagamentos pendentes antes de desativar a conta");
        }

        for (ParticipacaoRepublica participacao : participacoesAtivas) {
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
        }

        Instant agora = Instant.now();
        for (ParticipacaoRepublica participacao : participacoesAtivas) {
            atribuicoes.cancelarPendentesDaParticipacao(participacao, participacao, agora);
            participacao.setStatus(StatusParticipacao.INATIVO);
            participacao.setDataSaida(agora);
        }
        usuarioBloqueado.setAtivo(false);
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
