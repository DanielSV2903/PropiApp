package cr.ac.ucr.propi.controller;

import cr.ac.ucr.propi.model.dto.NotaCreditoDebitoDto;
import cr.ac.ucr.propi.service.NotaCreditoDebitoService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Controlador REST para Notas de Crédito / Débito (PropiDB v2.1).
 * Accesible para rol_admin y rol_digitador.
 */
@RestController
@RequestMapping("/notas")
public class NotaCreditoDebitoController {

    private final NotaCreditoDebitoService notaService;

    public NotaCreditoDebitoController(NotaCreditoDebitoService notaService) {
        this.notaService = notaService;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ROL_ADMIN', 'ROL_DIGITADOR')")
    public ResponseEntity<List<NotaCreditoDebitoDto>> listarNotas() {
        return ResponseEntity.ok(notaService.listarNotas());
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ROL_ADMIN', 'ROL_DIGITADOR')")
    public ResponseEntity<Map<String, Object>> crearNota(@RequestBody @Valid NotaCreditoDebitoDto dto) {
        String usuario = getCurrentUsername();
        Long codNota = notaService.crearNota(dto, usuario);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of(
                        "codNota", codNota,
                        "message", "Nota de " + dto.getTipo() + " registrada exitosamente"
                ));
    }

    private String getCurrentUsername() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        return auth != null ? auth.getName() : "anonymous";
    }
}
