package cr.ac.ucr.propi.controller;

import cr.ac.ucr.propi.model.dto.ClienteDto;
import cr.ac.ucr.propi.service.ClienteService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Controlador REST para gestión de Clientes (PropiDB v2.1).
 * Invoca procedimientos almacenados para altas, bajas, modificaciones y listados.
 */
@RestController
@RequestMapping("/clientes")
public class ClienteController {

    private final ClienteService clienteService;

    public ClienteController(ClienteService clienteService) {
        this.clienteService = clienteService;
    }

    @GetMapping
    public ResponseEntity<List<ClienteDto>> listarClientes(
            @RequestParam(required = false, defaultValue = "false") Boolean soloActivos) {

        List<ClienteDto> clientes = clienteService.listarClientes(soloActivos);
        return ResponseEntity.ok(clientes);
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> crearCliente(
            @RequestBody @Valid ClienteDto clienteRequest) {

        String usuario = getCurrentUsername();
        Long codCliente = clienteService.crearCliente(clienteRequest, usuario);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of(
                        "codCliente", codCliente,
                        "message", "Cliente creado exitosamente"
                ));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Map<String, String>> actualizarCliente(
            @PathVariable Long id,
            @RequestBody @Valid ClienteDto clienteRequest) {

        String usuario = getCurrentUsername();
        clienteRequest.setCodCliente(id);
        clienteService.actualizarCliente(clienteRequest, usuario);

        return ResponseEntity.ok(Map.of("message", "Cliente actualizado correctamente"));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ClienteDto> buscarCliente(@PathVariable Long id) {
        ClienteDto cliente = clienteService.buscarCliente(id);
        return ResponseEntity.ok(cliente);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> eliminarCliente(@PathVariable Long id) {
        String usuario = getCurrentUsername();
        clienteService.eliminarCliente(id, usuario);

        return ResponseEntity.ok(Map.of("message", "Cliente eliminado correctamente"));
    }

    private String getCurrentUsername() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        return auth != null ? auth.getName() : "anonymous";
    }
}
