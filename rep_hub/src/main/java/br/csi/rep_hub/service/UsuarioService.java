package br.csi.rep_hub.service;

import br.csi.rep_hub.model.usuario.Usuario;
import br.csi.rep_hub.model.usuario.UsuarioRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class UsuarioService {
    private final UsuarioRepository usuarioRepository;

    public UsuarioService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    public void salvar(Usuario usuario){
        this.usuarioRepository.save(usuario);
    }

    public List<Usuario> listarTodos(){
        return this.usuarioRepository.findAll();
    }

    public Usuario getUsuario(Long id){
        return this.usuarioRepository.findById(id).get();
    }

    public void excluir(Long id){
        this.usuarioRepository.deleteById(id);
    }

    public void atualizar(Usuario usuario){
        Usuario u = this.usuarioRepository.getReferenceById(usuario.getId());
        u.setNome(usuario.getNome());
        u.setEmail(usuario.getEmail());
        u.setSenha(usuario.getSenha());
        this.usuarioRepository.save(u);
    }

    public void atualizarUUID(Usuario usuario){
        Usuario u = this.usuarioRepository.findUsuariosByUuid(usuario.getUuid());
        u.setNome(usuario.getNome());
        u.setEmail(usuario.getEmail());
        u.setSenha(usuario.getSenha());
        this.usuarioRepository.save(u);
    }

    public Usuario getUsuarioUUID(String uuid){
        UUID uuidformatado = UUID.fromString(uuid);
        return this.usuarioRepository.findUsuariosByUuid(uuidformatado);
    }

    public void deletarUUID(String uuid){
        this.usuarioRepository.deleteUsuariosByUuid(UUID.fromString(uuid));
    }
}
