package br.csi.rep_hub.model.usuario;

import io.swagger.v3.oas.annotations.media.Schema;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UuidGenerator;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "usuarios")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Representa um usuário cadastrado no sistema")
public class Usuario {
    @UuidGenerator
    @Column(nullable = false, unique = true, updatable = false)
    @Schema(description = "UUID do aluno", example = "123e4567-e89b-12d3-a456-426614174000")
    private UUID uuid;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "ID do usuário", example = "1")
    private Long id;

    @NotBlank
    @Schema(description = "Nome do usuário", example = "Ana Medina")
    private String nome;

    @NotBlank
    @Email(message = "Email inválido")
    @Column(nullable = false, unique = true)
    @Schema(description = "Email do usuário", example = "ana.medina@example.com")
    private String email;

    @NotBlank
    @Size(min = 8, message = "A senha deve ter pelo menos 8 caracteres")
    @JsonIgnore
    @Column(nullable = false)
    @Schema(description = "Senha do usuário", example = "senha123")
    private String senha;

    @Schema(description = "Data de cadastro do usuário", example = "2024-06-01T12:00:00")
    @CreationTimestamp
    private Instant dataCadastro;

    @Schema(description = "Indica se o usuário está ativo", example = "true")
    private boolean ativo;

}
