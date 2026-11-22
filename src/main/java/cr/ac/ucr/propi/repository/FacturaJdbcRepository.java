package cr.ac.ucr.propi.repository;

import com.microsoft.sqlserver.jdbc.SQLServerDataTable;
import cr.ac.ucr.propi.model.dto.FacturaDetalleItemDto;
import cr.ac.ucr.propi.model.dto.FacturaRequestDto;
import cr.ac.ucr.propi.repository.base.SessionContextAwareRepository;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcCall;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Date;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Repositorio JDBC para Facturas (PropiDB v2.1).
 * Implementa Table-Valued Parameter (dbo.tt_factura_detalle) e invoca exclusivamente
 * procedimientos almacenados (sp_factura_crear, sp_factura_obtener_detalle, sp_consultar_facturas).
 */
@Repository
public class FacturaJdbcRepository extends SessionContextAwareRepository {

    private final JdbcTemplate jdbcTemplate;
    private final SimpleJdbcCall spFacturaCrear;
    private final SimpleJdbcCall spFacturaObtenerDetalle;
    private final SimpleJdbcCall spConsultarFacturas;

    public FacturaJdbcRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        this.spFacturaCrear = new SimpleJdbcCall(jdbcTemplate)
                .withProcedureName("sp_factura_crear");
        this.spFacturaObtenerDetalle = new SimpleJdbcCall(jdbcTemplate)
                .withProcedureName("sp_factura_obtener_detalle");
        this.spConsultarFacturas = new SimpleJdbcCall(jdbcTemplate)
                .withProcedureName("sp_consultar_facturas");
    }

    @Transactional
    public Long crearFactura(FacturaRequestDto dto, String currentUser) throws SQLException {
        setSessionContext(jdbcTemplate, currentUser);

        SQLServerDataTable tvpDetalle = new SQLServerDataTable();
        tvpDetalle.addColumnMetadata("cod_producto", java.sql.Types.BIGINT);
        tvpDetalle.addColumnMetadata("cantidad", java.sql.Types.DECIMAL);

        if (dto.getDetalle() != null) {
            for (FacturaDetalleItemDto item : dto.getDetalle()) {
                tvpDetalle.addRow(item.getCodProducto(), item.getCantidad());
            }
        }

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("cod_cliente", dto.getCodCliente())
                .addValue("condicion_venta", dto.getCondicionVenta())
                .addValue("medio_pago", dto.getMedioPago())
                .addValue("moneda", dto.getMoneda())
                .addValue("tipo_cambio", dto.getTipoCambio())
                .addValue("observaciones", dto.getObservaciones())
                .addValue("detalle", tvpDetalle);

        Map<String, Object> out = spFacturaCrear.execute(params);
        return ((Number) out.get("cod_factura")).longValue();
    }

    @SuppressWarnings("unchecked")
    public Map<String, Object> obtenerDetalle(Long codFactura) {
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("cod_factura", codFactura);

        Map<String, Object> out = spFacturaObtenerDetalle.execute(params);
        Map<String, Object> resultado = new HashMap<>();

        for (Map.Entry<String, Object> entry : out.entrySet()) {
            if (entry.getKey().startsWith("#result-set-1") && entry.getValue() instanceof List<?> list) {
                if (!list.isEmpty()) {
                    resultado.put("cabecera", list.get(0));
                }
            } else if (entry.getKey().startsWith("#result-set-2") && entry.getValue() instanceof List<?> list) {
                resultado.put("detalles", list);
            }
        }

        return resultado;
    }

    @SuppressWarnings("unchecked")
    public List<Map<String, Object>> listarFacturas(LocalDate desde, LocalDate hasta) {
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("fec_desde", desde != null ? Date.valueOf(desde) : null)
                .addValue("fec_hasta", hasta != null ? Date.valueOf(hasta) : null);

        Map<String, Object> out = spConsultarFacturas.execute(params);

        for (Map.Entry<String, Object> entry : out.entrySet()) {
            if (entry.getKey().startsWith("#result-set") && entry.getValue() instanceof List) {
                return (List<Map<String, Object>>) entry.getValue();
            }
        }
        return List.of();
    }
}
