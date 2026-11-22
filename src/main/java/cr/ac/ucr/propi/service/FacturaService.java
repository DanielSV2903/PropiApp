package cr.ac.ucr.propi.service;

import cr.ac.ucr.propi.model.dto.FacturaRequestDto;
import cr.ac.ucr.propi.repository.FacturaJdbcRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * Servicio de facturación (PropiDB v2.1).
 */
@Service
public class FacturaService {

    private static final Logger log = LoggerFactory.getLogger(FacturaService.class);

    private final FacturaJdbcRepository facturaJdbcRepository;

    public FacturaService(FacturaJdbcRepository facturaJdbcRepository) {
        this.facturaJdbcRepository = facturaJdbcRepository;
    }

    public Long crearFactura(FacturaRequestDto dto, String usuario) throws SQLException {
        log.info("Creando factura para cliente {} - usuario: {}", dto.getCodCliente(), usuario);
        return facturaJdbcRepository.crearFactura(dto, usuario);
    }

    public Map<String, Object> obtenerDetalle(Long codFactura) {
        log.debug("Obteniendo detalle de factura {}", codFactura);
        return facturaJdbcRepository.obtenerDetalle(codFactura);
    }

    public List<Map<String, Object>> listarFacturas(LocalDate desde, LocalDate hasta) {
        log.debug("Listando facturas desde {} hasta {}", desde, hasta);
        return facturaJdbcRepository.listarFacturas(desde, hasta);
    }
}
