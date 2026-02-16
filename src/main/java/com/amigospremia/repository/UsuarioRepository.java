package com.amigospremia.repository;

import com.amigospremia.domain.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UsuarioRepository
        extends JpaRepository<Usuario, Long> {

    Usuario findByEmail(String email);
}
