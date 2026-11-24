package br.csi.rep_hub.dto.despesas;

import br.csi.rep_hub.model.despesa.CategoriaDespesa;
import br.csi.rep_hub.model.despesa.TipoDivisao;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record DespesaDados(
        @NotBlank(message = "O título da despesa é obrigatório")
        @Size(max = 255, message = "O título deve ter no máximo 255 caracteres")
        String titulo,

        @Size(max = 255, message = "A descrição deve ter no máximo 255 caracteres")
        String descricao,

        @NotNull(message = "O valor total é obrigatório")
        @DecimalMin(value = "1.00", message = "O valor total mínimo é R$ 1,00")
        @Digits(integer = 10, fraction = 2,
                message = "O valor total deve ter no máximo duas casas decimais")
        BigDecimal valorTotal,

        @NotNull(message = "A data de referência é obrigatória")
        LocalDate dataReferencia,

        @NotNull(message = "A data de vencimento é obrigatória")
        LocalDate dataVencimento,

        @NotNull(message = "A categoria é obrigatória")
        CategoriaDespesa categoria,

        @NotNull(message = "O tipo de divisão é obrigatório")
        TipoDivisao tipoDivisao,

        @NotEmpty(message = "Informe ao menos uma participação no rateio")
        List<@Valid ParcelaDespesaDados> parcelas) {
}
