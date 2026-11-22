package cr.ac.ucr.propi.model.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

/**
 * Item de línea de detalle de Factura para Table-Valued Parameter (dbo.tt_factura_detalle).
 */
public class FacturaDetalleItemDto {

    @NotNull(message = "El código del producto es obligatorio")
    private Long codProducto;

    @NotNull(message = "La cantidad es obligatoria")
    @DecimalMin(value = "0.01", message = "La cantidad debe ser mayor a cero")
    private BigDecimal cantidad;

    public FacturaDetalleItemDto() {
    }

    public FacturaDetalleItemDto(Long codProducto, BigDecimal cantidad) {
        this.codProducto = codProducto;
        this.cantidad = cantidad;
    }

    public Long getCodProducto() {
        return codProducto;
    }

    public void setCodProducto(Long codProducto) {
        this.codProducto = codProducto;
    }

    public BigDecimal getCantidad() {
        return cantidad;
    }

    public void setCantidad(BigDecimal cantidad) {
        this.cantidad = cantidad;
    }
}
