package br.csi.rep_hub.infra.security;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;

// centraliza a criação e a validação dos tokens usados pela API

@Service
public class TokenServiceJWT {
    private static final String ISSUER = "rep-hub";

    private final Algorithm algoritmo;
    private final Duration validade;

    public TokenServiceJWT(@Value("${api.security.token.secret}") String segredo,
                           @Value("${api.security.token.expiration-hours}") long horasDeValidade) {
        this.algoritmo = Algorithm.HMAC256(segredo);
        this.validade = Duration.ofHours(horasDeValidade);
    }

    public String gerarToken(String email) {
        Instant agora = Instant.now();
        return JWT.create()
                .withIssuer(ISSUER)
                .withSubject(email)
                .withIssuedAt(agora)
                .withExpiresAt(agora.plus(validade))
                .sign(algoritmo);
    }

    public String obterEmailDoToken(String token) {
        return JWT.require(algoritmo)
                .withIssuer(ISSUER)
                .build()
                .verify(token)
                .getSubject();
    }
}
