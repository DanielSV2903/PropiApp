package cr.ac.ucr.propi.repository;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcCall;
import org.springframework.stereotype.Repository;

import java.sql.Date;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * Repositorio JDBC para ejecución de procedimientos almacenados de reportes (REQ 007).
 */
@Repository
public class ReporteJdbcRepository {

    private final SimpleJdbcCall spConsultarClientesResumido;
    private final SimpleJdbcCall spConsultarFacturas;
    private final SimpleJdbcCall spConsultarPistasAuditoria;
    private final SimpleJdbcCall spConsultarConfiguracionSeguridad;

    public ReporteJdbcRepository(JdbcTemplate jdbcTemplate) {
        this.spConsultarClientesResumido = new SimpleJdbcCall(jdbcTemplate)
                .withProcedureName("sp_consultar_clientes_resumido");

        this.spConsultarFacturas = new SimpleJdbcCall(jdbcTemplate)
                .withProcedureName("sp_consultar_facturas");

        this.spConsultarPistasAuditoria = new SimpleJdbcCall(jdbcTemplate)
                .withProcedureName("sp_consultar_pistas_auditoria");

        this.spConsultarConfiguracionSeguridad = new SimpleJdbcCall(jdbcTemplate)
                .withProcedureName("sp_consultar_configuracion_seguridad");
    }

    /**
     * 1. sp_consultar_clientes_resumido: @fec_desde, @fec_hasta, @estado
     */
    public List<Map<String, Object>> consultarClientesResumido(LocalDate desde, LocalDate hasta, String estado) {
        MapSqlParameterSource in = new MapSqlParameterSource()
                .addValue("fec_desde", desde != null ? Date.valueOf(desde) : null)
                .addValue("fec_hasta", hasta != null ? Date.valueOf(hasta) : null)
                .addValue("estado", estado != null && !estado.isBlank() ? estado : "TODOS");

        Map<String, Object> out = spConsultarClientesResumido.execute(in);
        return extractResultSet(out);
    }

    /**
     * 2. sp_consultar_facturas: @fec_desde, @fec_hasta
     */
    public List<Map<String, Object>> consultarFacturas(LocalDate desde, LocalDate hasta) {
        MapSqlParameterSource in = new MapSqlParameterSource()
                .addValue("fec_desde", desde != null ? Date.valueOf(desde) : null)
                .addValue("fec_hasta", hasta != null ? Date.valueOf(hasta) : null);

        Map<String, Object> out = spConsultarFacturas.execute(in);
        return extractResultSet(out);
    }

    /**
     * 3. sp_consultar_pistas_auditoria: @fec_desde, @fec_hasta, @cod_usuario
     */
    public List<Map<String, Object>> consultarPistasAuditoria(LocalDate desde, LocalDate hasta, String usuario) {
        MapSqlParameterSource in = new MapSqlParameterSource()
                .addValue("fec_desde", desde != null ? Date.valueOf(desde) : null)
                .addValue("fec_hasta", hasta != null ? Date.valueOf(hasta) : null)
                .addValue("cod_usuario", usuario != null && !usuario.isBlank() ? usuario : null);

        Map<String, Object> out = spConsultarPistasAuditoria.execute(in);
        return extractResultSet(out);
    }

    /**
     * 4. sp_consultar_configuracion_seguridad: @nom_usuario
     */
    public List<Map<String, Object>> consultarConfiguracionSeguridad(String usuario) {
        MapSqlParameterSource in = new MapSqlParameterSource()
                .addValue("nom_usuario", usuario != null && !usuario.isBlank() ? usuario : null);

        Map<String, Object> out = spConsultarConfiguracionSeguridad.execute(in);
        return extractResultSet(out);
    }

    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> extractResultSet(Map<String, Object> result) {
        for (Map.Entry<String, Object> entry : result.entrySet()) {
            if (entry.getKey().startsWith("#result-set") && entry.getValue() instanceof List) {
                return (List<Map<String, Object>>) entry.getValue();
            }
        }
        return List.of();
    }
}
