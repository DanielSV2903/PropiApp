package cr.ac.ucr.propi.repository;

import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * Repositorio de auditoría que delega en el procedimiento almacenado sp_consultar_pistas_auditoria
 * garantizando cero SQL embebido.
 */
@Repository
public class BitacoraJdbcRepository {

    private final ReporteJdbcRepository reporteJdbcRepository;

    public BitacoraJdbcRepository(ReporteJdbcRepository reporteJdbcRepository) {
        this.reporteJdbcRepository = reporteJdbcRepository;
    }

    public List<Map<String, Object>> listarBitacora(LocalDate desde, LocalDate hasta) {
        return reporteJdbcRepository.consultarPistasAuditoria(desde, hasta, null);
    }

    public List<Map<String, Object>> listarBitacoraAcceso(LocalDate desde, LocalDate hasta) {
        return reporteJdbcRepository.consultarPistasAuditoria(desde, hasta, null);
    }
}
