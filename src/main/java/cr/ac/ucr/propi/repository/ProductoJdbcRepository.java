package cr.ac.ucr.propi.repository;

import cr.ac.ucr.propi.model.dto.ProductoDto;
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
 * Repositorio JDBC para Productos / Inmuebles (PropiDB v2.1).
 * Invoca exclusivamente Procedimientos Almacenados (REQ 002, REQ 005).
 */
@Repository
public class ProductoJdbcRepository extends SessionContextAwareRepository {

    private final JdbcTemplate jdbcTemplate;
    private final SimpleJdbcCall spProductoCrear;
    private final SimpleJdbcCall spProductoActualizar;
    private final SimpleJdbcCall spProductoListar;

    private final RowMapper<ProductoDto> productoRowMapper = (rs, rowNum) -> {
        ProductoDto p = new ProductoDto();
        p.setCodProducto(rs.getLong("cod_producto"));
        p.setCodigoCabys(rs.getString("codigo_cabys"));
        p.setDescripcion(rs.getString("descripcion"));
        p.setUnidadMedida(rs.getString("unidad_medida"));
        p.setPrecio(rs.getBigDecimal("precio"));
        p.setCodImpuesto(rs.getLong("cod_impuesto"));
        p.setNombreImpuesto(rs.getString("nombre_impuesto"));
        p.setPorcentaje(rs.getBigDecimal("porcentaje"));
        p.setEstado(rs.getString("estado"));
        return p;
    };

    public ProductoJdbcRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        this.spProductoCrear = new SimpleJdbcCall(jdbcTemplate)
                .withProcedureName("sp_producto_crear");
        this.spProductoActualizar = new SimpleJdbcCall(jdbcTemplate)
                .withProcedureName("sp_producto_actualizar");
        this.spProductoListar = new SimpleJdbcCall(jdbcTemplate)
                .withProcedureName("sp_producto_listar")
                .returningResultSet("productos", productoRowMapper);
    }

    @Transactional
    public Long crear(ProductoDto dto, String currentUser) {
        setSessionContext(jdbcTemplate, currentUser);

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("codigo_cabys", dto.getCodigoCabys())
                .addValue("descripcion", dto.getDescripcion())
                .addValue("unidad_medida", dto.getUnidadMedida() != null ? dto.getUnidadMedida() : "Unidad")
                .addValue("precio", dto.getPrecio())
                .addValue("cod_impuesto", dto.getCodImpuesto());

        Map<String, Object> out = spProductoCrear.execute(params);
        return ((Number) out.get("cod_producto")).longValue();
    }

    @Transactional
    public void actualizar(ProductoDto dto, String currentUser) {
        setSessionContext(jdbcTemplate, currentUser);

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("cod_producto", dto.getCodProducto())
                .addValue("codigo_cabys", dto.getCodigoCabys())
                .addValue("descripcion", dto.getDescripcion())
                .addValue("unidad_medida", dto.getUnidadMedida() != null ? dto.getUnidadMedida() : "Unidad")
                .addValue("precio", dto.getPrecio())
                .addValue("cod_impuesto", dto.getCodImpuesto())
                .addValue("estado", dto.getEstado() != null ? dto.getEstado() : "ACTIVO");

        spProductoActualizar.execute(params);
    }

    @SuppressWarnings("unchecked")
    public List<ProductoDto> listar() {
        Map<String, Object> out = spProductoListar.execute();
        return (List<ProductoDto>) out.get("productos");
    }

    public Optional<ProductoDto> buscarPorId(Long id) {
        return listar().stream()
                .filter(p -> p.getCodProducto().equals(id))
                .findFirst();
    }
}
