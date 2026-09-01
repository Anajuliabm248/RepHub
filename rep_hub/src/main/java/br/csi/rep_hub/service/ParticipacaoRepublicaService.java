package br.csi.rep_hub.service;

import br.csi.rep_hub.model.participacao.ParticipacaoRepublica;
import br.csi.rep_hub.model.participacao.ParticipacaoRepublicaRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class ParticipacaoRepublicaService {
    private final ParticipacaoRepublicaRepository republicaRepository;

    public ParticipacaoRepublicaService(ParticipacaoRepublicaRepository participacaoRepository) {
        this.republicaRepository = participacaoRepository;
    }

    public void salvar(ParticipacaoRepublica republica){
        this.republicaRepository.save(republica);
    }

    public List<ParticipacaoRepublica> listarTodos(){
        return this.republicaRepository.findAll();
    }

    public ParticipacaoRepublica getParticipacaoRepublica(Long id){
        return this.republicaRepository.findById(id).get();
    }

    public void excluir(Long id){
        this.republicaRepository.deleteById(id);
    }

    public void atualizar(ParticipacaoRepublica republica){
        ParticipacaoRepublica u = this.republicaRepository.getReferenceById(republica.getId());
        u.setPapel(republica.getPapel());
        u.setStatus(republica.getStatus());
        this.republicaRepository.save(u);
    }

    public void atualizarUUID(ParticipacaoRepublica republica){
        ParticipacaoRepublica u = this.republicaRepository.findParticipacaoRepublicasByUuid(republica.getUuid());
        u.setPapel(republica.getPapel());
        u.setStatus(republica.getStatus());
        this.republicaRepository.save(u);
    }

    public ParticipacaoRepublica getParticipacaoRepublicaUUID(String uuid){
        UUID uuidformatado = UUID.fromString(uuid);
        return this.republicaRepository.findParticipacaoRepublicasByUuid(uuidformatado);
    }

    public void deletarUUID(String uuid){
        this.republicaRepository.deleteParticipacaoRepublicaByUuid(UUID.fromString(uuid));
    }
}
