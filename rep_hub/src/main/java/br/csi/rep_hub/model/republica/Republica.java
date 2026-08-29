package br.csi.rep_hub.model.republica;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.UuidGenerator;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "republica")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@RequiredArgsConstructor
@Schema(description = "Representa uma república cadastrada no sistema")
public class Republica {
    @UuidGenerator
    @Schema(description = "UUID da república", example = "123e4567-e89b-12d3-a456-426614174000")
    private UUID uuid;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "ID da república", example = "1")
    private Long id;

    @NonNull
    @Schema(description = "Nome da república", example = "República dos Estudantes")
    private String nome;

    @Schema(description = "Descrição da república, opcional", example = "República localizada no centro da cidade")
    private String descricao;

    @NonNull
    @Schema(description = "Data de criação da república", example = "2023-01-01T00:00:00")
    private LocalDateTime dataCriacao;

    @Schema(description = "Indica se a república está ativa", example = "true")
    private boolean ativa;
}
