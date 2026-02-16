package com.amigospremia.security;

import com.amigospremia.domain.Usuario;
import com.amigospremia.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailsService
        implements UserDetailsService {

    @Autowired
    UsuarioRepository repo;

    @Override
    public UserDetails loadUserByUsername(String email)
            throws UsernameNotFoundException {

        System.out.println(">>> BUSCANDO USUARIO: " + email);

        Usuario u = repo.findByEmail(email);

        if (u == null)
            throw new UsernameNotFoundException("Usuário não encontrado");

        return User.builder()
            .username(u.getEmail())
            .password(u.getSenha())
            .roles(u.getRole().name())
            .build();
    }

}

