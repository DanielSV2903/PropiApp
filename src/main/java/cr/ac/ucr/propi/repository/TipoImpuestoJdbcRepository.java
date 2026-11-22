package cr.ac.ucr.propi.repository;

import cr.ac.ucr.propi.model.dto.TipoImpuestoDto;
import cr.ac.ucr.propi.repository.base.SessionContextAwareRepository;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcCall;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Repositorio JDBC para Tipos de Impuesto (PropiDB v2.1).
 * Invoca exclusivamente Procedimientos Almacenados (REQ 002, REQ 005).
 */
@Repository
public class TipoImpuestoJdbcRepository extends SessionContextAwareRepository {

    private final JdbcTemplate jdbcTemplate;
    private final SimpleJdbcCall spTipoImpuestoCrear;
    private final SimpleJdbcCall spTipoImpuestoActualizar;
    private final SimpleJdbcCall spTipoImpuestoListar;

    private final RowMapper<TipoImpuestoDto> impuestoRowMapper = (rs, rowNum) -> {
        TipoImpuestoDto dto = new TipoImpuestoDto();
        dto.setCodImpuesto(rs.getLong("cod_impuesto"));
        dto.setCodigo(rs.getString("codigo"));
        dto.setDescripcion(rs.getString("descripcion"));
        dto.setPorcentaje(rs.getBigDecimal("porcentaje"));
        dto.setTarifa(rs.getString("tarifa"));
        dto.setEstado(rs.getString("estado"));
        return dto;
    };

    public TipoImpuestoJdbcRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        this.spTipoImpuestoCrear = new SimpleJdbcCall(jdbcTemplate)
                .withProcedureName("sp_tipo_impuesto_crear");
        this.spTipoImpuestoActualizar = new SimpleJdbcCall(jdbcTemplate)
                .withProcedureName("sp_tipo_impuesto_actualizar");
        this.spTipoImpuestoListar = new SimpleJdbcCall(jdbcTemplate)
                .withProcedureName("sp_tipo_impuesto_listar")
                .returningResultSet("impuestos", impuestoRowMapper);
    }

    @Transactional
    public Long crear(TipoImpuestoDto dto, String currentUser) {
        setSessionContext(jdbcTemplate, currentUser);

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("codigo", dto.getCodigo())
                .addValue("descripcion", dto.getDescripcion())
                .addValue("porcentaje", dto.getPorcentaje())
                .addValue("tarifa", dto.getTarifa());

        Map<String, Object> out = spTipoImpuestoCrear.execute(params);
        return ((Number) out.get("cod_impuesto")).longValue();
    }

    @Transactional
    public void actualizar(TipoImpuestoDto dto, String currentUser) {
        setSessionContext(jdbcTemplate, currentUser);

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("cod_impuesto", dto.getCodImpuesto())
                .addValue("codigo", dto.getCodigo())
                .addValue("descripcion", dto.getDescripcion())
                .addValue("porcentaje", dto.getPorcentaje())
                .addValue("tarifa", dto.getTarifa())
                .addValue("estado", dto.getEstado() != null ? dto.getEstado() : "ACTIVO");

        spTipoImpuestoActualizar.execute(params);
    }

    @SuppressWarnings("unchecked")
    public List<TipoImpuestoDto> listar() {
        Map<String, Object> out = spTipoImpuestoListar.execute();
        return (List<TipoImpuestoDto>) out.get("impuestos");
    }

    public Optional<TipoImpuestoDto> buscarPorId(Long id) {
        return listar().stream()
                .filter(i -> i.getCodImpuesto().equals(id))
                .findFirst();
    }
}
