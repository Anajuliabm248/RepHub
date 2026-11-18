package br.csi.rep_hub.infra;

import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

@RestControllerAdvice
public class TratadorDeErros {
    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity tratarErro404(){
        return ResponseEntity.notFound().build();
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity tratarErro400(MethodArgumentNotValidException ex){
        List<FieldError> erros = ex.getFieldErrors();
        List<DadosErroValidacao> dados = new ArrayList<>();
        for(FieldError fe : erros){
            dados.add(new DadosErroValidacao(fe.getField(), fe.getDefaultMessage()));
        }

        return ResponseEntity.badRequest().body(dados);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<String> tratarIdentificadorInvalido() {
        return ResponseEntity.badRequest().body("Identificador inválido");
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<String> tratarConflitoDeDados() {
        return ResponseEntity.status(409).body("Conflito com os dados existentes");
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<String> tratarErroDeAutenticacao() {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("E-mail ou senha inválidos");
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<String> tratarAcessoNegado() {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Acesso não autorizado");
    }

    private record DadosErroValidacao(String campo, String mensagem) {
        public DadosErroValidacao(FieldError erro){
            this(erro.getField(), erro.getDefaultMessage());
        }
    }
}
