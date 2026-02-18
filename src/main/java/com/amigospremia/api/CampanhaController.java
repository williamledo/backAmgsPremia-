package com.amigospremia.api;

import com.amigospremia.domain.Campanha;
import com.amigospremia.domain.Usuario;
import com.amigospremia.repository.CampanhaRepository;
import com.amigospremia.repository.UsuarioRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@RestController
@RequestMapping("/api/campanhas")
public class CampanhaController {

    private static final Logger log = LoggerFactory.getLogger(CampanhaController.class);

    private static final Set<String> ALLOWED_IMAGE_HOSTS = Set.of(
            "drive.google.com",
            "drive.usercontent.google.com",
            "lh3.googleusercontent.com"
    );

    @Autowired
    private CampanhaRepository campanhaRepo;

    @Autowired
    private UsuarioRepository usuarioRepo;

    @GetMapping
    public ResponseEntity<List<Campanha>> listarCampanhas() {
        List<Campanha> campanhas = campanhaRepo.findAll();
        log.info("GET /api/campanhas -> {} campanhas", campanhas.size());
        return ResponseEntity.ok(campanhas);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Campanha> getCampanha(@PathVariable Long id) {
        Optional<Campanha> campanha = campanhaRepo.findById(id);
        log.info("GET /api/campanhas/{} -> found={}", id, campanha.isPresent());
        return campanha.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/imagem-proxy")
    public ResponseEntity<?> proxyImagem(@RequestParam("url") String imageUrl) {
        try {
            URI uri = URI.create(imageUrl);
            String host = uri.getHost();

            if (host == null || !ALLOWED_IMAGE_HOSTS.contains(host.toLowerCase())) {
                log.warn("Image proxy blocked host={} url={}", host, imageUrl);
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("Host de imagem nao permitido");
            }

            HttpClient client = HttpClient.newBuilder()
                    .followRedirects(HttpClient.Redirect.NORMAL)
                    .connectTimeout(Duration.ofSeconds(10))
                    .build();

            HttpRequest request = HttpRequest.newBuilder(uri)
                    .header("User-Agent", "Mozilla/5.0")
                    .header("Referer", "https://drive.google.com/")
                    .GET()
                    .timeout(Duration.ofSeconds(20))
                    .build();

            HttpResponse<byte[]> response = client.send(request, HttpResponse.BodyHandlers.ofByteArray());

            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                log.warn("Image proxy failed status={} url={}", response.statusCode(), imageUrl);
                return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
                        .body("Falha ao buscar imagem remota");
            }

            String contentType = response.headers()
                    .firstValue("Content-Type")
                    .orElse(MediaType.IMAGE_JPEG_VALUE);

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_TYPE, contentType)
                    .cacheControl(CacheControl.maxAge(Duration.ofHours(6)).cachePublic())
                    .body(response.body());
        } catch (Exception e) {
            log.error("Image proxy exception url={}", imageUrl, e);
            return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
                    .body("Erro ao carregar imagem");
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateCampanha(
            @PathVariable Long id,
            @RequestBody Campanha campanhaAtualizada,
            Authentication auth) {

        if (auth == null || !auth.isAuthenticated()) {
            log.warn("PUT /api/campanhas/{} unauthorized (no auth)", id);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("Usuario nao autenticado");
        }

        String email = auth.getName();
        Usuario usuarioLogado = usuarioRepo.findByEmail(email);

        if (usuarioLogado == null) {
            log.warn("PUT /api/campanhas/{} unauthorized user not found email={}", id, email);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("Usuario nao encontrado");
        }

        Optional<Campanha> campanhaOpt = campanhaRepo.findById(id);
        if (campanhaOpt.isEmpty()) {
            log.warn("PUT /api/campanhas/{} not found", id);
            return ResponseEntity.notFound().build();
        }

        Campanha campanha = campanhaOpt.get();

        if (!"ADMIN".equals(usuarioLogado.getRole().name())) {
            log.warn("PUT /api/campanhas/{} forbidden user={} role={}", id, email, usuarioLogado.getRole().name());
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("Apenas administradores podem editar campanhas");
        }

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
        log.info("PUT /api/campanhas/{} updated by={} ", id, email);
        return ResponseEntity.ok(salva);
    }

    @PostMapping
    public ResponseEntity<?> criarCampanha(
            @RequestBody Campanha campanha,
            Authentication auth) {

        if (auth == null || !auth.isAuthenticated()) {
            log.warn("POST /api/campanhas unauthorized (no auth)");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("Usuario nao autenticado");
        }

        String email = auth.getName();
        Usuario usuarioLogado = usuarioRepo.findByEmail(email);

        if (usuarioLogado == null) {
            log.warn("POST /api/campanhas unauthorized user not found email={}", email);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("Usuario nao encontrado");
        }

        if (!"ADMIN".equals(usuarioLogado.getRole().name())) {
            log.warn("POST /api/campanhas forbidden user={} role={}", email, usuarioLogado.getRole().name());
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("Apenas administradores podem criar campanhas");
        }

        campanha.setCriador(usuarioLogado);
        Campanha salva = campanhaRepo.save(campanha);
        log.info("POST /api/campanhas created id={} by={}", salva.getId(), email);
        return ResponseEntity.status(HttpStatus.CREATED).body(salva);
    }
}
