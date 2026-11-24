package br.csi.rep_hub.controller;

import br.csi.rep_hub.dto.auth.LoginDados;
import br.csi.rep_hub.dto.auth.TokenResposta;
import br.csi.rep_hub.infra.security.TokenServiceJWT;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Tag(name = "Autenticação")
public class AutenticacaoController {
    private final AuthenticationManager authenticationManager;
    private final TokenServiceJWT tokens;

    public AutenticacaoController(AuthenticationManager authenticationManager, TokenServiceJWT tokens) {
        this.authenticationManager = authenticationManager;
        this.tokens = tokens;
    }

    @PostMapping("/login")
    @Operation(summary = "Autenticar usuário", description = "Valida e-mail e senha e devolve um token JWT")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Token gerado",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = TokenResposta.class))),
            @ApiResponse(responseCode = "400", description = "Dados inválidos"),
            @ApiResponse(responseCode = "401", description = "E-mail, senha ou situação da conta inválidos")
    })
    public ResponseEntity<TokenResposta> login(@RequestBody @Valid LoginDados dados) {
        Authentication autenticacao = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(dados.email(), dados.senha()));

        return ResponseEntity.ok(new TokenResposta(tokens.gerarToken(autenticacao.getName())));
    }
}
