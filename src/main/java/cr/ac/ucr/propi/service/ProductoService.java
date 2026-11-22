package cr.ac.ucr.propi.service;

import cr.ac.ucr.propi.exception.DatabaseOperationException;
import cr.ac.ucr.propi.model.dto.ProductoDto;
import cr.ac.ucr.propi.repository.ProductoJdbcRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Servicio de negocio para Productos / Inmuebles (PropiDB v2.1).
 */
@Service
public class ProductoService {

    private static final Logger log = LoggerFactory.getLogger(ProductoService.class);

    private final ProductoJdbcRepository productoJdbcRepository;

    public ProductoService(ProductoJdbcRepository productoJdbcRepository) {
        this.productoJdbcRepository = productoJdbcRepository;
    }

    public List<ProductoDto> listar() {
        return productoJdbcRepository.listar();
    }

    public Long crear(ProductoDto dto, String usuario) {
        log.info("Creando producto {} - usuario: {}", dto.getCodigoCabys(), usuario);
        return productoJdbcRepository.crear(dto, usuario);
    }

    public void actualizar(ProductoDto dto, String usuario) {
        log.info("Actualizando producto {} - usuario: {}", dto.getCodProducto(), usuario);
        productoJdbcRepository.actualizar(dto, usuario);
    }

    public ProductoDto buscarPorId(Long id) {
        return productoJdbcRepository.buscarPorId(id)
                .orElseThrow(() -> new DatabaseOperationException("Producto no encontrado con ID: " + id));
    }
}
