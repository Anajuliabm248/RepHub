package br.csi.rep_hub.infra;

import br.csi.rep_hub.model.participacao.PapelRepublica;
import br.csi.rep_hub.model.participacao.ParticipacaoRepublica;
import br.csi.rep_hub.model.participacao.ParticipacaoRepublicaRepository;
import br.csi.rep_hub.model.participacao.StatusParticipacao;
import br.csi.rep_hub.model.republica.Republica;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;

// regras de participante e administrador da república

@Component
public class AcessoRepublica {
    private final UsuarioAtual usuarioAtual;
    private final ParticipacaoRepublicaRepository participacoes;

    public AcessoRepublica(UsuarioAtual usuarioAtual, ParticipacaoRepublicaRepository participacoes) {
        this.usuarioAtual = usuarioAtual;
        this.participacoes = participacoes;
    }

    // garante que o usuário autenticado participa ativamente da república
    public ParticipacaoRepublica exigirParticipante(Republica republica) {
        if (!republica.isAtiva()) {
            throw new AccessDeniedException("República encerrada");
        }
        return participacoes.findByRepublicaIdAndUsuarioIdAndStatus(
                republica.getId(), usuarioAtual.obter().getId(), StatusParticipacao.ATIVO)
                .orElseThrow(() -> new AccessDeniedException("Participação ativa necessária"));
    }

    // garante que o participante ativo também é administrador
    public ParticipacaoRepublica exigirAdministrador(Republica republica) {
        ParticipacaoRepublica participacao = exigirParticipante(republica);
        if (participacao.getPapel() != PapelRepublica.ADMINISTRADOR) {
            throw new AccessDeniedException("Ação restrita a administradores da república");
        }
        return participacao;
    }
}
