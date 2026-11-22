package cr.ac.ucr.propi.controller;

import cr.ac.ucr.propi.model.dto.ProductoDto;
import cr.ac.ucr.propi.service.ProductoService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Controlador REST para Productos / Inmuebles (PropiDB v2.1).
 */
@RestController
@RequestMapping("/productos")
public class ProductoController {

    private final ProductoService productoService;

    public ProductoController(ProductoService productoService) {
        this.productoService = productoService;
    }

    @GetMapping
    public ResponseEntity<List<ProductoDto>> listarProductos() {
        return ResponseEntity.ok(productoService.listar());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductoDto> buscarProducto(@PathVariable Long id) {
        return ResponseEntity.ok(productoService.buscarPorId(id));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ROL_ADMIN', 'ROL_DIGITADOR')")
    public ResponseEntity<Map<String, Object>> crearProducto(@RequestBody @Valid ProductoDto dto) {
        String usuario = getCurrentUsername();
        Long codProducto = productoService.crear(dto, usuario);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of(
                        "codProducto", codProducto,
                        "message", "Producto creado exitosamente"
                ));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ROL_ADMIN', 'ROL_DIGITADOR')")
    public ResponseEntity<Map<String, String>> actualizarProducto(@PathVariable Long id,
                                                                 @RequestBody @Valid ProductoDto dto) {
        String usuario = getCurrentUsername();
        dto.setCodProducto(id);
        productoService.actualizar(dto, usuario);

        return ResponseEntity.ok(Map.of("message", "Producto actualizado correctamente"));
    }

    private String getCurrentUsername() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        return auth != null ? auth.getName() : "anonymous";
    }
}
