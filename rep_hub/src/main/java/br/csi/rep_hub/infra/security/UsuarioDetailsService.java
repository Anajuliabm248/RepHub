package br.csi.rep_hub.infra.security;

import br.csi.rep_hub.model.usuario.UsuarioRepository;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

// ponte entre a conta persistida pela aplicação e o mecanismo de autenticação do Spring Security
@Service
public class UsuarioDetailsService implements UserDetailsService {
    private final UsuarioRepository usuarios;

    public UsuarioDetailsService(UsuarioRepository usuarios) {
        this.usuarios = usuarios;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        return usuarios.findByEmailIgnoreCase(email)
                .map(usuario -> User.withUsername(usuario.getEmail())
                        .password(usuario.getSenha())
                        .disabled(!usuario.isAtivo())
                        .roles("USER")
                        .build())
                .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado"));
    }
}
