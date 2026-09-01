package br.csi.rep_hub.service;

import br.csi.rep_hub.model.republica.Republica;
import br.csi.rep_hub.model.republica.RepublicaRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class RepublicaService {
    private final RepublicaRepository republicaRepository;

    public RepublicaService(RepublicaRepository republicaRepository) {
        this.republicaRepository = republicaRepository;
    }

    public void salvar(Republica republica){
        this.republicaRepository.save(republica);
    }

    public List<Republica> listarTodos(){
        return this.republicaRepository.findAll();
    }

    public Republica getRepublica(Long id){
        return this.republicaRepository.findById(id).get();
    }

    public void excluir(Long id){
        this.republicaRepository.deleteById(id);
    }

    public void atualizar(Republica republica){
        Republica u = this.republicaRepository.getReferenceById(republica.getId());
        u.setNome(republica.getNome());
        u.setDescricao(republica.getDescricao());
        this.republicaRepository.save(u);
    }

    public void atualizarUUID(Republica republica){
        Republica u = this.republicaRepository.findRepublicaByUuid(republica.getUuid());
        u.setNome(republica.getNome());
        u.setDescricao(republica.getDescricao());
        this.republicaRepository.save(u);
    }

    public Republica getRepublicaUUID(String uuid){
        UUID uuidformatado = UUID.fromString(uuid);
        return this.republicaRepository.findRepublicaByUuid(uuidformatado);
    }

    public void deletarUUID(String uuid){
        this.republicaRepository.deleteRepublicaByUuid(UUID.fromString(uuid));
    }
}
