package cr.ac.ucr.propi.repository;

import cr.ac.ucr.propi.model.dto.ClienteDto;
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
 * Repositorio JDBC para clientes (PropiDB v2.1).
 * Invoca exclusivamente Procedimientos Almacenados (REQ 002, REQ 005, REQ 006).
 */
@Repository
public class ClienteJdbcRepository extends SessionContextAwareRepository {

    private final JdbcTemplate jdbcTemplate;
    private final SimpleJdbcCall spClienteCrear;
    private final SimpleJdbcCall spClienteActualizar;
    private final SimpleJdbcCall spClienteListar;
    private final SimpleJdbcCall spClienteEliminar;

    private final RowMapper<ClienteDto> clienteRowMapper = (rs, rowNum) -> {
        ClienteDto c = new ClienteDto();
        c.setCodCliente(rs.getLong("cod_cliente"));
        c.setNombre(rs.getString("nombre"));
        c.setTipoIdentificacion(rs.getString("tipo_identificacion"));
        c.setNumIdentificacion(rs.getString("num_identificacion"));
        c.setProvincia(rs.getString("provincia"));
        c.setCanton(rs.getString("canton"));
        c.setDistrito(rs.getString("distrito"));
        c.setBarrio(rs.getString("barrio"));
        c.setProfesionOficio(rs.getString("profesion_oficio"));
        c.setActividadEconomica(rs.getString("actividad_economica"));
        c.setCorreo(rs.getString("correo"));
        c.setTelefono(rs.getString("telefono"));
        c.setEstado(rs.getString("estado"));
        if (rs.getTimestamp("fec_registro") != null) {
            c.setFecRegistro(rs.getTimestamp("fec_registro").toLocalDateTime());
        }
        if (rs.getTimestamp("fec_modificacion") != null) {
            c.setFecModificacion(rs.getTimestamp("fec_modificacion").toLocalDateTime());
        }
        return c;
    };

    public ClienteJdbcRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        this.spClienteCrear = new SimpleJdbcCall(jdbcTemplate)
                .withProcedureName("sp_cliente_crear");
        this.spClienteActualizar = new SimpleJdbcCall(jdbcTemplate)
                .withProcedureName("sp_cliente_actualizar");
        this.spClienteListar = new SimpleJdbcCall(jdbcTemplate)
                .withProcedureName("sp_cliente_listar")
                .returningResultSet("clientes", clienteRowMapper);
        this.spClienteEliminar = new SimpleJdbcCall(jdbcTemplate)
                .withProcedureName("sp_cliente_eliminar");
    }

    @Transactional
    public Long insertarCliente(ClienteDto dto, String currentUser) {
        setSessionContext(jdbcTemplate, currentUser);

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("nombre", dto.getNombre())
                .addValue("tipo_identificacion", dto.getTipoIdentificacion())
                .addValue("num_identificacion", dto.getNumIdentificacion())
                .addValue("provincia", dto.getProvincia())
                .addValue("canton", dto.getCanton())
                .addValue("distrito", dto.getDistrito())
                .addValue("barrio", dto.getBarrio())
                .addValue("profesion_oficio", dto.getProfesionOficio())
                .addValue("actividad_economica", dto.getActividadEconomica())
                .addValue("correo", dto.getCorreo())
                .addValue("telefono", dto.getTelefono());

        Map<String, Object> out = spClienteCrear.execute(params);
        return ((Number) out.get("cod_cliente")).longValue();
    }

    @Transactional
    public void actualizarCliente(ClienteDto dto, String currentUser) {
        setSessionContext(jdbcTemplate, currentUser);

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("cod_cliente", dto.getCodCliente())
                .addValue("nombre", dto.getNombre())
                .addValue("tipo_identificacion", dto.getTipoIdentificacion())
                .addValue("num_identificacion", dto.getNumIdentificacion())
                .addValue("provincia", dto.getProvincia())
                .addValue("canton", dto.getCanton())
                .addValue("distrito", dto.getDistrito())
                .addValue("barrio", dto.getBarrio())
                .addValue("profesion_oficio", dto.getProfesionOficio())
                .addValue("actividad_economica", dto.getActividadEconomica())
                .addValue("correo", dto.getCorreo())
                .addValue("telefono", dto.getTelefono())
                .addValue("estado", dto.getEstado() != null ? dto.getEstado() : "ACTIVO");

        spClienteActualizar.execute(params);
    }

    @SuppressWarnings("unchecked")
    public List<ClienteDto> listarClientes(Boolean soloActivos) {
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("solo_activos", Boolean.TRUE.equals(soloActivos) ? 1 : 0);

        Map<String, Object> out = spClienteListar.execute(params);
        return (List<ClienteDto>) out.get("clientes");
    }

    @Transactional
    public void eliminarCliente(Long codCliente, String currentUser) {
        setSessionContext(jdbcTemplate, currentUser);

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("cod_cliente", codCliente);

        spClienteEliminar.execute(params);
    }

    public Optional<ClienteDto> buscarClientePorId(Long codCliente) {
        return listarClientes(false).stream()
                .filter(c -> c.getCodCliente().equals(codCliente))
                .findFirst();
    }
}
