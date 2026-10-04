package br.csi.rep_hub.service;

import br.csi.rep_hub.model.convite.Convite;
import br.csi.rep_hub.model.convite.ConviteRepository;
import br.csi.rep_hub.model.convite.CriarCodigoConvite;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

@Service
public class ConviteService {
    private static final int MAX_TENTATIVAS = 5;
    private static final String RESTRICAO_CODIGO = "uc_convite_codigo";

    private final ConviteRepository repository;

    public ConviteService(ConviteRepository repository) {
        this.repository = repository;
    }

    public void salvarCodigo(Convite convite) {
        DataIntegrityViolationException ultimaColisao = null;

        for (int tentativa = 0; tentativa < MAX_TENTATIVAS; tentativa++) {
            convite.setCodigo(CriarCodigoConvite.gerarCodigoConvite());

            try {
                repository.saveAndFlush(convite);
                return;
            } catch (DataIntegrityViolationException erro) {
                if (!codigoDuplicado(erro)) {
                    throw erro;
                }
                ultimaColisao = erro;
            }
        }

        throw new IllegalStateException("Não foi possível gerar um código de convite único", ultimaColisao);
    }

    private boolean codigoDuplicado(Throwable erro) {
        for (Throwable causa = erro; causa != null; causa = causa.getCause()) {
            if (causa instanceof ConstraintViolationException violacao
                    && RESTRICAO_CODIGO.equalsIgnoreCase(violacao.getConstraintName())) {
                return true;
            }
        }
        return false;
    }
}
