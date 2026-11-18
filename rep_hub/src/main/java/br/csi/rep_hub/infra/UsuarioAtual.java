package br.csi.rep_hub.infra;

import br.csi.rep_hub.model.usuario.Usuario;
import br.csi.rep_hub.model.usuario.UsuarioRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

// fonte para conta autenticada e ativa

@Component
public class UsuarioAtual {
    private final UsuarioRepository usuarios;

    public UsuarioAtual(UsuarioRepository usuarios) {
        this.usuarios = usuarios;
    }

    // retorna a conta ativa associada ao token da requisição atual
    public Usuario obter() {
        Authentication autenticacao = SecurityContextHolder.getContext().getAuthentication();
        if (autenticacao == null || !autenticacao.isAuthenticated()) {
            throw new AccessDeniedException("Autenticação necessária");
        }
        return usuarios.findByEmailIgnoreCase(autenticacao.getName())
                .filter(Usuario::isAtivo)
                .orElseThrow(() -> new AccessDeniedException("Conta inativa ou inexistente"));
    }

    // garante que a operação é feita pelo próprio titular da conta
    public void exigirTitular(Usuario usuario) {
        if (!obter().getId().equals(usuario.getId())) {
            throw new AccessDeniedException("Acesso restrito ao titular da conta");
        }
    }
}
