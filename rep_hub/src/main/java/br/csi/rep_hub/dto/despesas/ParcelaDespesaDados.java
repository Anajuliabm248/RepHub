package br.csi.rep_hub.dto.despesas;

import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.util.UUID;

public record ParcelaDespesaDados(
        @NotNull(message = "A participação é obrigatória")
        UUID participacaoUuid,

        @Positive(message = "O valor da parcela deve ser positivo")
        @Digits(integer = 10, fraction = 2,
                message = "O valor da parcela deve ter no máximo duas casas decimais")
        BigDecimal valor) {
}
