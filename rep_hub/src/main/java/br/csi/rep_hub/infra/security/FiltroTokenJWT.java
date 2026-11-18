package br.csi.rep_hub.infra.security;

import com.auth0.jwt.exceptions.JWTVerificationException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;


// lê o token da requisição e registra o usuário autenticado no Spring Security
@Component
public class FiltroTokenJWT extends OncePerRequestFilter {
    private final TokenServiceJWT tokens;
    private final UsuarioDetailsService usuarios;

    public FiltroTokenJWT(TokenServiceJWT tokens, UsuarioDetailsService usuarios) {
        this.tokens = tokens;
        this.usuarios = usuarios;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String cabecalho = request.getHeader("Authorization");
        if (cabecalho == null || !cabecalho.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            autenticar(cabecalho.substring(7));
            filterChain.doFilter(request, response);
        } catch (JWTVerificationException | UsernameNotFoundException excecao) {
            SecurityContextHolder.clearContext();
            responderTokenInvalido(response);
        }
    }

    private void autenticar(String token) {
        String email = tokens.obterEmailDoToken(token);
        UserDetails usuario = usuarios.loadUserByUsername(email);

        if (!usuario.isEnabled()) {
            throw new UsernameNotFoundException("Conta inativa");
        }

        UsernamePasswordAuthenticationToken autenticacao = new UsernamePasswordAuthenticationToken(
                usuario, null, usuario.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(autenticacao);
    }

    private void responderTokenInvalido(HttpServletResponse response) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write("{\"erro\":\"Token inválido, expirado ou associado a uma conta inativa\"}");
    }
}
