package br.csi.rep_hub.model.usuario;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "usuarios")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@RequiredArgsConstructor
@Schema(description = "Representa um usuário cadastrado no sistema")
public class Usuario {
    @UuidGenerator
    @Schema(description = "UUID do aluno", example = "123e4567-e89b-12d3-a456-426614174000")
    private UUID uuid;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "ID do usuário", example = "1")
    private Long id;

    @NonNull
    @Schema(description = "Nome do usuário", example = "Ana Medina")
    private String nome;

    @NonNull
    @Schema(description = "Email do usuário", example = "ana.medina@example.com")
    private String email;

    @NonNull
    @Schema(description = "Senha do usuário", example = "senha123")
    private String senha;

    @NonNull
    @Schema(description = "Data de cadastro do usuário", example = "2024-06-01T12:00:00")
    private LocalDateTime dataCadastro;

    @NonNull
    @Schema(description = "Indica se o usuário está ativo", example = "true")
    private boolean ativo;

}
