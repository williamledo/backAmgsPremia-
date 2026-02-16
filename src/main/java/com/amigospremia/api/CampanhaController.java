package com.amigospremia.api;

import com.amigospremia.domain.Campanha;
import com.amigospremia.domain.Usuario;
import com.amigospremia.repository.CampanhaRepository;
import com.amigospremia.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/campanhas")
public class CampanhaController {

    @Autowired
    private CampanhaRepository campanhaRepo;

    @Autowired
    private UsuarioRepository usuarioRepo;

    @GetMapping("/{id}")
    public ResponseEntity<Campanha> getCampanha(@PathVariable Long id) {
        Optional<Campanha> campanha = campanhaRepo.findById(id);
        return campanha.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateCampanha(
            @PathVariable Long id,
            @RequestBody Campanha campanhaAtualizada,
            Authentication auth) {

        // Verificar autenticação
        if (auth == null || !auth.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("Usuário não autenticado");
        }

        // Buscar usuário logado
        String email = auth.getName();
        Usuario usuarioLogado = usuarioRepo.findByEmail(email);

        if (usuarioLogado == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("Usuário não encontrado");
        }

        // Buscar campanha
        Optional<Campanha> campanhaOpt = campanhaRepo.findById(id);
        if (!campanhaOpt.isPresent()) {
            return ResponseEntity.notFound().build();
        }

        Campanha campanha = campanhaOpt.get();

        // Verificar permissão: apenas ADMIN pode editar
        if (!"ADMIN".equals(usuarioLogado.getRole().name())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("Apenas administradores podem editar campanhas");
        }

        // Atualizar campos
        if (campanhaAtualizada.getTitulo() != null) {
            campanha.setTitulo(campanhaAtualizada.getTitulo());
        }
        if (campanhaAtualizada.getDescricao() != null) {
            campanha.setDescricao(campanhaAtualizada.getDescricao());
        }
        if (campanhaAtualizada.getImagem() != null) {
            campanha.setImagem(campanhaAtualizada.getImagem());
        }
        if (campanhaAtualizada.getDataHoraSorteio() != null) {
            campanha.setDataHoraSorteio(campanhaAtualizada.getDataHoraSorteio());
        }
        if (campanhaAtualizada.getValorCota() != null) {
            campanha.setValorCota(campanhaAtualizada.getValorCota());
        }

        Campanha salva = campanhaRepo.save(campanha);
        return ResponseEntity.ok(salva);
    }

    @PostMapping
    public ResponseEntity<?> criarCampanha(
            @RequestBody Campanha campanha,
            Authentication auth) {

        if (auth == null || !auth.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("Usuário não autenticado");
        }

        String email = auth.getName();
        Usuario usuarioLogado = usuarioRepo.findByEmail(email);

        if (usuarioLogado == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("Usuário não encontrado");
        }

        if (!"ADMIN".equals(usuarioLogado.getRole().name())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("Apenas administradores podem criar campanhas");
        }

        campanha.setCriador(usuarioLogado);
        Campanha salva = campanhaRepo.save(campanha);
        return ResponseEntity.status(HttpStatus.CREATED).body(salva);
    }
}
