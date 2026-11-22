package cr.ac.ucr.propi.service;

import cr.ac.ucr.propi.exception.DatabaseOperationException;
import cr.ac.ucr.propi.model.dto.TipoImpuestoDto;
import cr.ac.ucr.propi.repository.TipoImpuestoJdbcRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Servicio de negocio para Tipos de Impuesto (PropiDB v2.1).
 */
@Service
public class TipoImpuestoService {

    private static final Logger log = LoggerFactory.getLogger(TipoImpuestoService.class);

    private final TipoImpuestoJdbcRepository tipoImpuestoJdbcRepository;

    public TipoImpuestoService(TipoImpuestoJdbcRepository tipoImpuestoJdbcRepository) {
        this.tipoImpuestoJdbcRepository = tipoImpuestoJdbcRepository;
    }

    public List<TipoImpuestoDto> listar() {
        return tipoImpuestoJdbcRepository.listar();
    }

    public Long crear(TipoImpuestoDto dto, String usuario) {
        log.info("Creando tipo de impuesto {} - usuario: {}", dto.getCodigo(), usuario);
        return tipoImpuestoJdbcRepository.crear(dto, usuario);
    }

    public void actualizar(TipoImpuestoDto dto, String usuario) {
        log.info("Actualizando tipo de impuesto {} - usuario: {}", dto.getCodImpuesto(), usuario);
        tipoImpuestoJdbcRepository.actualizar(dto, usuario);
    }

    public TipoImpuestoDto buscarPorId(Long id) {
        return tipoImpuestoJdbcRepository.buscarPorId(id)
                .orElseThrow(() -> new DatabaseOperationException("Tipo de impuesto no encontrado con ID: " + id));
    }
}
