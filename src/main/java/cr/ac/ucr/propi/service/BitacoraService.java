package cr.ac.ucr.propi.service;

import cr.ac.ucr.propi.repository.BitacoraJdbcRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Service
public class BitacoraService {

    private static final Logger log = LoggerFactory.getLogger(BitacoraService.class);

    private final BitacoraJdbcRepository bitacoraJdbcRepository;

    public BitacoraService(BitacoraJdbcRepository bitacoraJdbcRepository) {
        this.bitacoraJdbcRepository = bitacoraJdbcRepository;
    }

    /**
     * Lists audit log (bitácora) entries within a date range.
     *
     * @param desde the start date (inclusive)
     * @param hasta the end date (inclusive)
     * @return a list of audit log records as maps
     */
    public List<Map<String, Object>> listarBitacora(LocalDate desde, LocalDate hasta) {
        log.debug("Listando bitácora desde {} hasta {}", desde, hasta);
        return bitacoraJdbcRepository.listarBitacora(desde, hasta);
    }
}
