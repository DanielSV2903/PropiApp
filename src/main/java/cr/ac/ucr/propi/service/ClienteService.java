package cr.ac.ucr.propi.service;

import cr.ac.ucr.propi.exception.DatabaseOperationException;
import cr.ac.ucr.propi.model.dto.ClienteDto;
import cr.ac.ucr.propi.repository.ClienteJdbcRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Servicio de negocio para gestión de clientes (PropiDB v2.1).
 */
@Service
public class ClienteService {

    private static final Logger log = LoggerFactory.getLogger(ClienteService.class);

    private final ClienteJdbcRepository clienteJdbcRepository;

    public ClienteService(ClienteJdbcRepository clienteJdbcRepository) {
        this.clienteJdbcRepository = clienteJdbcRepository;
    }

    public List<ClienteDto> listarClientes(Boolean soloActivos) {
        log.debug("Listando clientes - soloActivos: {}", soloActivos);
        return clienteJdbcRepository.listarClientes(soloActivos);
    }

    public Long crearCliente(ClienteDto dto, String usuario) {
        log.info("Creando cliente - usuario: {}", usuario);
        return clienteJdbcRepository.insertarCliente(dto, usuario);
    }

    public void actualizarCliente(ClienteDto dto, String usuario) {
        log.info("Actualizando cliente {} - usuario: {}", dto.getCodCliente(), usuario);
        clienteJdbcRepository.actualizarCliente(dto, usuario);
    }

    public void eliminarCliente(Long codCliente, String usuario) {
        log.info("Eliminando cliente {} - usuario: {}", codCliente, usuario);
        clienteJdbcRepository.eliminarCliente(codCliente, usuario);
    }

    public ClienteDto buscarCliente(Long codCliente) {
        return buscarClientePorId(codCliente);
    }

    public ClienteDto buscarClientePorId(Long codCliente) {
        log.debug("Buscando cliente por ID: {}", codCliente);
        return clienteJdbcRepository.buscarClientePorId(codCliente)
                .orElseThrow(() -> new DatabaseOperationException("Cliente no encontrado con ID: " + codCliente));
    }
}
