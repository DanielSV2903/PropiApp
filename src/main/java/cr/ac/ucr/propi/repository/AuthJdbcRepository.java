package cr.ac.ucr.propi.repository;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcCall;
import org.springframework.stereotype.Repository;

import java.util.Map;

/**
 * Repositorio de autenticación y accesos.
 * Invoca exclusivamente los procedimientos almacenados sp_registrar_ingreso y sp_registrar_salida (REQ 002, REQ 004).
 */
@Repository
public class AuthJdbcRepository {

    private final JdbcTemplate jdbcTemplate;
    private final SimpleJdbcCall spRegistrarIngreso;
    private final SimpleJdbcCall spRegistrarSalida;

    public AuthJdbcRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        this.spRegistrarIngreso = new SimpleJdbcCall(jdbcTemplate)
                .withProcedureName("sp_registrar_ingreso");
        this.spRegistrarSalida = new SimpleJdbcCall(jdbcTemplate)
                .withProcedureName("sp_registrar_salida");
    }

    /**
     * Registra el ingreso en tbl_bitacora_acceso mediante sp_registrar_ingreso.
     * Retorna el cod_acceso generado como parámetro de salida.
     */
    public Long registrarIngreso(String usuario, String terminal, String ip) {
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("nom_usuario", usuario)
                .addValue("nom_terminal", terminal != null && !terminal.isBlank() ? terminal : "DESCONOCIDO")
                .addValue("dir_ip", ip != null && !ip.isBlank() ? ip : "127.0.0.1");

        Map<String, Object> out = spRegistrarIngreso.execute(params);
        Number codAcceso = (Number) out.get("cod_acceso");
        return codAcceso != null ? codAcceso.longValue() : null;
    }

    /**
     * Registra la salida en tbl_bitacora_acceso mediante sp_registrar_salida.
     */
    public void registrarSalida(Long codAcceso) {
        if (codAcceso != null) {
            MapSqlParameterSource params = new MapSqlParameterSource()
                    .addValue("cod_acceso", codAcceso);
            spRegistrarSalida.execute(params);
        }
    }
}
