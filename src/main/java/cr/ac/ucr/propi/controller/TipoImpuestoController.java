package cr.ac.ucr.propi.controller;

import cr.ac.ucr.propi.model.dto.TipoImpuestoDto;
import cr.ac.ucr.propi.service.TipoImpuestoService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Controlador REST para Tipos de Impuesto (PropiDB v2.1).
 * Accesible para rol_admin.
 */
@RestController
@RequestMapping("/impuestos")
public class TipoImpuestoController {

    private final TipoImpuestoService tipoImpuestoService;

    public TipoImpuestoController(TipoImpuestoService tipoImpuestoService) {
        this.tipoImpuestoService = tipoImpuestoService;
    }

    @GetMapping
    public ResponseEntity<List<TipoImpuestoDto>> listar() {
        return ResponseEntity.ok(tipoImpuestoService.listar());
    }

    @GetMapping("/{id}")
    public ResponseEntity<TipoImpuestoDto> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(tipoImpuestoService.buscarPorId(id));
    }

    @PostMapping
    @PreAuthorize("hasRole('ROL_ADMIN')")
    public ResponseEntity<Map<String, Object>> crear(@RequestBody @Valid TipoImpuestoDto dto) {
        String usuario = getCurrentUsername();
        Long codImpuesto = tipoImpuestoService.crear(dto, usuario);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of(
                        "codImpuesto", codImpuesto,
                        "message", "Tipo de impuesto creado exitosamente"
                ));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ROL_ADMIN')")
    public ResponseEntity<Map<String, String>> actualizar(@PathVariable Long id,
                                                          @RequestBody @Valid TipoImpuestoDto dto) {
        String usuario = getCurrentUsername();
        dto.setCodImpuesto(id);
        tipoImpuestoService.actualizar(dto, usuario);

        return ResponseEntity.ok(Map.of("message", "Tipo de impuesto actualizado correctamente"));
    }

    private String getCurrentUsername() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        return auth != null ? auth.getName() : "anonymous";
    }
}
