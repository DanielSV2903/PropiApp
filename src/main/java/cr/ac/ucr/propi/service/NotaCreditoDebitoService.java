package cr.ac.ucr.propi.service;

import cr.ac.ucr.propi.model.dto.NotaCreditoDebitoDto;
import cr.ac.ucr.propi.repository.NotaCreditoDebitoJdbcRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Servicio de negocio para Notas de Crédito / Débito (PropiDB v2.1).
 */
@Service
public class NotaCreditoDebitoService {

    private static final Logger log = LoggerFactory.getLogger(NotaCreditoDebitoService.class);

    private final NotaCreditoDebitoJdbcRepository notaCreditoDebitoJdbcRepository;

    public NotaCreditoDebitoService(NotaCreditoDebitoJdbcRepository notaCreditoDebitoJdbcRepository) {
        this.notaCreditoDebitoJdbcRepository = notaCreditoDebitoJdbcRepository;
    }

    public Long crearNota(NotaCreditoDebitoDto dto, String usuario) {
        log.info("Creando nota de tipo {} para factura {} - usuario: {}", dto.getTipo(), dto.getCodFactura(), usuario);
        return notaCreditoDebitoJdbcRepository.crearNota(dto, usuario);
    }

    public List<NotaCreditoDebitoDto> listarNotas() {
        return notaCreditoDebitoJdbcRepository.listarNotas();
    }
}
