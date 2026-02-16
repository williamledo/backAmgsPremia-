package com.amigospremia.api;

import com.amigospremia.domain.Usuario;
import com.amigospremia.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class AuthController {

    @Autowired
    UsuarioRepository repo;

    @Autowired
    PasswordEncoder encoder;

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody Usuario u) {
        // Validar campos obrigatórios
        if (u.getEmail() == null || u.getEmail().trim().isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("erro", "Email é obrigatório"));
        }
        if (u.getSenha() == null || u.getSenha().trim().isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("erro", "Senha é obrigatória"));
        }
        if (u.getNome() == null || u.getNome().trim().isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("erro", "Nome é obrigatório"));
        }
        if (u.getCpf() == null || u.getCpf().trim().isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("erro", "CPF é obrigatório"));
        }

        // Validar se email já existe
        Usuario existente = repo.findByEmail(u.getEmail());
        if (existente != null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("erro", "Email já cadastrado"));
        }

        try {
            u.setSenha(encoder.encode(u.getSenha()));
            u.setAtivo(true);
            Usuario salvo = repo.save(u);
            
            System.out.println(">>> USUARIO REGISTRADO: " + salvo.getEmail() + " (ID: " + salvo.getId() + ")");
            
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(Map.of(
                        "mensagem", "Usuário registrado com sucesso",
                        "id", salvo.getId(),
                        "email", salvo.getEmail()
                    ));
        } catch (Exception e) {
            System.err.println(">>> ERRO AO REGISTRAR: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("erro", "Erro ao registrar usuário: " + e.getMessage()));
        }
    }

    @GetMapping("/me")
    public Map<String, Object> me(org.springframework.security.core.Authentication auth) {
        Map<String, Object> response = new HashMap<>();
        if (auth != null && auth.isAuthenticated()) {
            String email = auth.getName();
            Usuario u = repo.findByEmail(email);
            
            response.put("email", email);
            response.put("authenticated", true);
            if (u != null) {
                response.put("role", u.getRole().name());
                response.put("nome", u.getNome());
                response.put("id", u.getId());
            }
        } else {
            response.put("authenticated", false);
        }
        return response;
    }
    
    @GetMapping("/seguro")
    public String seguro() {
        return "Área protegida OK";
    }

}
