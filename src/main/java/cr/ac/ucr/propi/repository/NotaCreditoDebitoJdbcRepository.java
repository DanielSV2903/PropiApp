package cr.ac.ucr.propi.repository;

import cr.ac.ucr.propi.model.dto.NotaCreditoDebitoDto;
import cr.ac.ucr.propi.repository.base.SessionContextAwareRepository;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcCall;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

/**
 * Repositorio JDBC para Notas de Crédito / Débito (PropiDB v2.1).
 * Invoca exclusivamente Procedimientos Almacenados (REQ 002, REQ 005).
 */
@Repository
public class NotaCreditoDebitoJdbcRepository extends SessionContextAwareRepository {

    private final JdbcTemplate jdbcTemplate;
    private final SimpleJdbcCall spNotaCrear;
    private final SimpleJdbcCall spNotaListar;

    private final RowMapper<NotaCreditoDebitoDto> notaRowMapper = (rs, rowNum) -> {
        NotaCreditoDebitoDto dto = new NotaCreditoDebitoDto();
        dto.setCodNota(rs.getLong("cod_nota"));
        dto.setCodigo(rs.getString("codigo"));
        if (rs.getTimestamp("fec_nota") != null) {
            dto.setFecNota(rs.getTimestamp("fec_nota").toLocalDateTime());
        }
        dto.setCodFactura(rs.getLong("cod_factura"));
        dto.setTipo(rs.getString("tipo"));
        dto.setMotivo(rs.getString("motivo"));
        dto.setMontoAjuste(rs.getBigDecimal("monto_ajuste"));
        dto.setNombreCliente(rs.getString("nombre_cliente"));
        return dto;
    };

    public NotaCreditoDebitoJdbcRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        this.spNotaCrear = new SimpleJdbcCall(jdbcTemplate)
                .withProcedureName("sp_nota_crear");
        this.spNotaListar = new SimpleJdbcCall(jdbcTemplate)
                .withProcedureName("sp_nota_listar")
                .returningResultSet("notas", notaRowMapper);
    }

    @Transactional
    public Long crearNota(NotaCreditoDebitoDto dto, String currentUser) {
        setSessionContext(jdbcTemplate, currentUser);

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("codigo", dto.getCodigo())
                .addValue("cod_factura", dto.getCodFactura())
                .addValue("tipo", dto.getTipo())
                .addValue("motivo", dto.getMotivo())
                .addValue("monto_ajuste", dto.getMontoAjuste());

        Map<String, Object> out = spNotaCrear.execute(params);
        return ((Number) out.get("cod_nota")).longValue();
    }

    @SuppressWarnings("unchecked")
    public List<NotaCreditoDebitoDto> listarNotas() {
        Map<String, Object> out = spNotaListar.execute();
        return (List<NotaCreditoDebitoDto>) out.get("notas");
    }
}
