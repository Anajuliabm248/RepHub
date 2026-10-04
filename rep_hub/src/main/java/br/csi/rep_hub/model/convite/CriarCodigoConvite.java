package br.csi.rep_hub.model.convite;

import org.apache.commons.lang3.RandomStringUtils;

import java.util.Locale;

public class CriarCodigoConvite {

    public static String gerarCodigoConvite(){
        String codigo = RandomStringUtils.secure().nextAlphanumeric(6).toUpperCase(Locale.ROOT);
        return "REP-" + codigo;
    }
}
