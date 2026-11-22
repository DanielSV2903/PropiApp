package cr.ac.ucr.propi.service;

import cr.ac.ucr.propi.repository.ReporteJdbcRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Service
public class ReporteService {

    private static final Logger log = LoggerFactory.getLogger(ReporteService.class);

    private final ReporteJdbcRepository reporteJdbcRepository;

    public ReporteService(ReporteJdbcRepository reporteJdbcRepository) {
        this.reporteJdbcRepository = reporteJdbcRepository;
    }

    /**
     * Retrieves a summarized client report for a date range and status.
     *
     * @param desde  the start date (inclusive)
     * @param hasta  the end date (inclusive)
     * @param estado the client status filter
     * @return a list of summarized client records
     */
    public List<Map<String, Object>> consultarClientesResumido(LocalDate desde, LocalDate hasta,
                                                                String estado) {
        log.debug("Consultando clientes resumido - desde: {}, hasta: {}, estado: {}", desde, hasta, estado);
        return reporteJdbcRepository.consultarClientesResumido(desde, hasta, estado);
    }

    /**
     * Retrieves an invoice report for a date range.
     *
     * @param desde the start date (inclusive)
     * @param hasta the end date (inclusive)
     * @return a list of invoice records
     */
    public List<Map<String, Object>> consultarFacturas(LocalDate desde, LocalDate hasta) {
        log.debug("Consultando facturas - desde: {}, hasta: {}", desde, hasta);
        return reporteJdbcRepository.consultarFacturas(desde, hasta);
    }

    /**
     * Retrieves audit trail records for a date range and user.
     *
     * @param desde   the start date (inclusive)
     * @param hasta   the end date (inclusive)
     * @param usuario the user to filter by
     * @return a list of audit trail records
     */
    public List<Map<String, Object>> consultarPistasAuditoria(LocalDate desde, LocalDate hasta,
                                                               String usuario) {
        log.debug("Consultando pistas de auditoría - desde: {}, hasta: {}, usuario: {}", desde, hasta, usuario);
        return reporteJdbcRepository.consultarPistasAuditoria(desde, hasta, usuario);
    }

    /**
     * Retrieves the security configuration for a user.
     *
     * @param usuario the user to query
     * @return the security configuration records
     */
    public List<Map<String, Object>> consultarConfiguracionSeguridad(String usuario) {
        log.debug("Consultando configuración de seguridad - usuario: {}", usuario);
        return reporteJdbcRepository.consultarConfiguracionSeguridad(usuario);
    }
}
