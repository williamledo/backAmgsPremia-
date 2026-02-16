package com.amigospremia.api;

import com.amigospremia.domain.Usuario;
import com.amigospremia.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
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
    public void register(@RequestBody Usuario u) {
        u.setSenha(encoder.encode(u.getSenha()));
        u.setAtivo(true);
        repo.save(u);
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
