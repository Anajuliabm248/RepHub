package br.csi.rep_hub.dto.pagamentos;

import br.csi.rep_hub.model.pagamento.FinalidadePagamento;
import br.csi.rep_hub.model.pagamento.FormaPagamento;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDate;

public record PagamentoDados(
        @NotNull(message = "O valor do pagamento é obrigatório")
        @Positive(message = "O valor do pagamento deve ser positivo")
        @Digits(integer = 10, fraction = 2,
                message = "O valor do pagamento deve ter no máximo duas casas decimais")
        BigDecimal valor,

        @NotNull(message = "A data do pagamento é obrigatória")
        LocalDate dataPagamento,

        @NotNull(message = "A forma de pagamento é obrigatória")
        FormaPagamento forma,

        @NotNull(message = "A finalidade do pagamento é obrigatória")
        FinalidadePagamento finalidade,

        @NotBlank(message = "O destinatário é obrigatório")
        @Size(max = 255, message = "O destinatário deve ter no máximo 255 caracteres")
        String destinatario,

        @Size(max = 255, message = "A observação deve ter no máximo 255 caracteres")
        String observacao) {
}
