package cr.ac.ucr.propi.controller;

import cr.ac.ucr.propi.model.dto.FacturaRequestDto;
import cr.ac.ucr.propi.service.FacturaService;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * Controlador REST para Facturas (PropiDB v2.1).
 * Implementa emisión transaccional y consulta de detalle.
 */
@RestController
@RequestMapping("/facturas")
public class FacturaController {

    private final FacturaService facturaService;

    public FacturaController(FacturaService facturaService) {
        this.facturaService = facturaService;
    }

    @GetMapping
    public ResponseEntity<List<Map<String, Object>>> listarFacturas(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta) {

        List<Map<String, Object>> facturas = facturaService.listarFacturas(desde, hasta);
        return ResponseEntity.ok(facturas);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> obtenerDetalle(@PathVariable Long id) {
        Map<String, Object> detalle = facturaService.obtenerDetalle(id);
        if (detalle.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(detalle);
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> crearFactura(
            @RequestBody @Valid FacturaRequestDto facturaRequest) throws SQLException {

        String usuario = getCurrentUsername();
        Long codFactura = facturaService.crearFactura(facturaRequest, usuario);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of(
                        "codFactura", codFactura,
                        "message", "Factura emitida exitosamente"
                ));
    }

    private String getCurrentUsername() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        return auth != null ? auth.getName() : "anonymous";
    }
}
