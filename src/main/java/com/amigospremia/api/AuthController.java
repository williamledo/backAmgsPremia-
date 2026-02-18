package com.amigospremia.api;

import com.amigospremia.domain.Usuario;
import com.amigospremia.repository.UsuarioRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class AuthController {

    private static final Logger log = LoggerFactory.getLogger(AuthController.class);

    @Autowired
    UsuarioRepository repo;

    @Autowired
    PasswordEncoder encoder;

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody Usuario u) {
        log.info("Register attempt email={}", u != null ? u.getEmail() : null);

        if (u.getEmail() == null || u.getEmail().trim().isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("erro", "Email e obrigatorio"));
        }
        if (u.getSenha() == null || u.getSenha().trim().isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("erro", "Senha e obrigatoria"));
        }
        if (u.getNome() == null || u.getNome().trim().isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("erro", "Nome e obrigatorio"));
        }
        if (u.getCpf() == null || u.getCpf().trim().isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("erro", "CPF e obrigatorio"));
        }

        Usuario existente = repo.findByEmail(u.getEmail());
        if (existente != null) {
            log.warn("Register rejected: email already exists email={}", u.getEmail());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("erro", "Email ja cadastrado"));
        }

        try {
            u.setSenha(encoder.encode(u.getSenha()));
            u.setAtivo(true);
            Usuario salvo = repo.save(u);

            log.info("User registered successfully email={} id={}", salvo.getEmail(), salvo.getId());

            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(Map.of(
                            "mensagem", "Usuario registrado com sucesso",
                            "id", salvo.getId(),
                            "email", salvo.getEmail()
                    ));
        } catch (Exception e) {
            log.error("Register failed email={}", u.getEmail(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("erro", "Erro ao registrar usuario: " + e.getMessage()));
        }
    }

    @GetMapping("/me")
    public Map<String, Object> me(Authentication auth, HttpServletRequest request) {
        Map<String, Object> response = new HashMap<>();
        String sessionId = request.getRequestedSessionId();

        if (auth != null && auth.isAuthenticated()) {
            String email = auth.getName();
            Usuario u = repo.findByEmail(email);

            log.info("GET /api/me authenticated=true email={} sessionId={}", email, sessionId);

            response.put("email", email);
            response.put("authenticated", true);
            if (u != null) {
                response.put("role", u.getRole().name());
                response.put("nome", u.getNome());
                response.put("id", u.getId());
            }
        } else {
            log.warn("GET /api/me authenticated=false sessionId={}", sessionId);
            response.put("authenticated", false);
        }
        return response;
    }

    @GetMapping("/seguro")
    public String seguro() {
        return "Area protegida OK";
    }
}
