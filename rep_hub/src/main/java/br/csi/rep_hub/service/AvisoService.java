package br.csi.rep_hub.service;

import br.csi.rep_hub.dto.AvisoDados;
import br.csi.rep_hub.dto.AvisoResposta;
import br.csi.rep_hub.infra.AcessoRepublica;
import br.csi.rep_hub.model.aviso.Aviso;
import br.csi.rep_hub.model.aviso.AvisoRepository;
import br.csi.rep_hub.model.participacao.PapelRepublica;
import br.csi.rep_hub.model.participacao.ParticipacaoRepublica;
import br.csi.rep_hub.model.republica.Republica;
import br.csi.rep_hub.model.republica.RepublicaRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class AvisoService {
    private final AvisoRepository avisos;
    private final RepublicaRepository republicas;
    private final AcessoRepublica acesso;

    public AvisoService(AvisoRepository avisos, RepublicaRepository republicas,
                        AcessoRepublica acesso) {
        this.avisos = avisos;
        this.republicas = republicas;
        this.acesso = acesso;
    }

    @Transactional
    public AvisoResposta publicar(Long republicaId, AvisoDados dados) {
        Republica republica = buscarRepublica(republicaId);
        ParticipacaoRepublica autor = acesso.exigirParticipante(republica);

        Aviso aviso = new Aviso();
        aviso.setRepublica(republica);
        aviso.setAutor(autor);
        aviso.setDataPublicacao(Instant.now());
        aplicarDados(aviso, dados);

        return responderVisivel(avisos.saveAndFlush(aviso));
    }

    @Transactional(readOnly = true)
    public List<AvisoResposta> listar(Long republicaId) {
        Republica republica = buscarRepublica(republicaId);
        acesso.exigirParticipante(republica);

        return avisos.findByRepublicaIdAndDataRemocaoIsNullOrderByIdDesc(republicaId)
                .stream()
                .map(this::responderVisivel)
                .toList();
    }

    @Transactional(readOnly = true)
    public AvisoResposta porId(Long id) {
        Aviso aviso = buscar(id);
        acesso.exigirParticipante(aviso.getRepublica());
        return responderVisivel(aviso);
    }

    @Transactional(readOnly = true)
    public AvisoResposta porUuid(UUID uuid) {
        Aviso aviso = buscar(uuid);
        acesso.exigirParticipante(aviso.getRepublica());
        return responderVisivel(aviso);
    }

    @Transactional
    public AvisoResposta editar(Long id, AvisoDados dados) {
        return editar(bloquear(id), dados);
    }

    @Transactional
    public AvisoResposta editar(UUID uuid, AvisoDados dados) {
        return editar(bloquear(uuid), dados);
    }

    @Transactional
    public void ocultar(Long id) {
        ocultar(bloquear(id));
    }

    @Transactional
    public void ocultar(UUID uuid) {
        ocultar(bloquear(uuid));
    }

    @Transactional
    public AvisoResposta restaurar(Long id) {
        return restaurar(bloquear(id));
    }

    @Transactional
    public AvisoResposta restaurar(UUID uuid) {
        return restaurar(bloquear(uuid));
    }

    private AvisoResposta editar(Aviso aviso, AvisoDados dados) {
        exigirAutorAtivo(aviso);
        if (aviso.getDataRemocao() != null) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Um aviso oculto não pode ser editado");
        }

        aplicarDados(aviso, dados);
        aviso.setDataEdicao(Instant.now());
        return responderVisivel(aviso);
    }

    private void ocultar(Aviso aviso) {
        ParticipacaoRepublica ator = exigirAutorOuAdministrador(aviso);
        if (aviso.getDataRemocao() != null) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "O aviso já está oculto");
        }

        aviso.setDataRemocao(Instant.now());
        aviso.setOcultadoPor(ator);
    }

    private AvisoResposta restaurar(Aviso aviso) {
        exigirAutorOuAdministrador(aviso);
        if (aviso.getDataRemocao() == null) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "O aviso já está visível");
        }

        aviso.setDataRemocao(null);
        aviso.setOcultadoPor(null);
        return responderVisivel(aviso);
    }

    private void exigirAutorAtivo(Aviso aviso) {
        ParticipacaoRepublica ator = acesso.exigirParticipante(aviso.getRepublica());
        if (!ator.getId().equals(aviso.getAutor().getId())) {
            throw new AccessDeniedException("Apenas o autor pode editar o aviso");
        }
    }

    private ParticipacaoRepublica exigirAutorOuAdministrador(Aviso aviso) {
        ParticipacaoRepublica ator = acesso.exigirParticipante(aviso.getRepublica());
        boolean autor = ator.getId().equals(aviso.getAutor().getId());
        boolean administrador = ator.getPapel() == PapelRepublica.ADMINISTRADOR;
        if (!autor && !administrador) {
            throw new AccessDeniedException(
                    "Ação restrita ao autor ou a um administrador da república");
        }
        return ator;
    }

    private void aplicarDados(Aviso aviso, AvisoDados dados) {
        aviso.setTitulo(dados.titulo().trim());
        aviso.setMensagem(dados.mensagem().trim());
    }

    private Aviso buscar(Long id) {
        return validar(avisos.findById(id).orElseThrow(() -> avisoNaoEncontrado()));
    }

    private Aviso buscar(UUID uuid) {
        return validar(avisos.findByUuid(uuid).orElseThrow(() -> avisoNaoEncontrado()));
    }

    private Aviso bloquear(Long id) {
        return validar(avisos.bloquearPorId(id).orElseThrow(() -> avisoNaoEncontrado()));
    }

    private Aviso bloquear(UUID uuid) {
        return validar(avisos.bloquearPorUuid(uuid).orElseThrow(() -> avisoNaoEncontrado()));
    }

    private Republica buscarRepublica(Long id) {
        return republicas.findById(id).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "República não encontrada"));
    }

    private AvisoResposta responderVisivel(Aviso aviso) {
        validar(aviso);
        if (aviso.getDataRemocao() != null) {
            throw avisoNaoEncontrado();
        }
        return AvisoResposta.de(aviso);
    }

    private Aviso validar(Aviso aviso) {
        boolean ocultacaoIncompleta = (aviso.getDataRemocao() == null)
                != (aviso.getOcultadoPor() == null);
        if (aviso.getRepublica() == null || aviso.getAutor() == null
                || aviso.getDataPublicacao() == null || ocultacaoIncompleta) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                    "Aviso legado sem dados completos");
        }
        return aviso;
    }

    private ResponseStatusException avisoNaoEncontrado() {
        return new ResponseStatusException(HttpStatus.NOT_FOUND, "Aviso não encontrado");
    }
}
