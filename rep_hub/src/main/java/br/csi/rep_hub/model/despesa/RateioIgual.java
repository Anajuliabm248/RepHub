package br.csi.rep_hub.model.despesa;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

/** Cálculo determinístico do rateio, sem conversão para ponto flutuante. */
public final class RateioIgual {
    private RateioIgual() {
    }

    public static List<BigDecimal> dividir(BigDecimal total, int quantidade) {
        if (total == null || quantidade <= 0) {
            throw new IllegalArgumentException("Total e quantidade devem ser positivos");
        }
        BigInteger centavos = total.movePointRight(2).toBigIntegerExact();
        BigInteger numeroDeParcelas = BigInteger.valueOf(quantidade);
        if (centavos.compareTo(numeroDeParcelas) < 0) {
            throw new IllegalArgumentException("O rateio criaria parcelas zeradas");
        }

        BigInteger[] divisao = centavos.divideAndRemainder(numeroDeParcelas);
        List<BigDecimal> valores = new ArrayList<>(quantidade);
        for (int indice = 0; indice < quantidade; indice++) {
            BigInteger valor = divisao[0].add(
                    BigInteger.valueOf(indice).compareTo(divisao[1]) < 0
                            ? BigInteger.ONE : BigInteger.ZERO);
            valores.add(new BigDecimal(valor, 2));
        }
        return List.copyOf(valores);
    }
}
