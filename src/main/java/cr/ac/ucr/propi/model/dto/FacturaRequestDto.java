package cr.ac.ucr.propi.model.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.List;

/**
 * DTO para la creación de Factura (PropiDB v2.1).
 */
public class FacturaRequestDto {

    @NotNull(message = "El código del cliente es obligatorio")
    private Long codCliente;

    private String condicionVenta = "CONTADO";
    private String medioPago = "TRANSFERENCIA";
    private String moneda = "CRC";

    @DecimalMin(value = "0.01", message = "El tipo de cambio debe ser mayor a cero")
    private BigDecimal tipoCambio = BigDecimal.ONE;

    private String observaciones;

    @NotEmpty(message = "La factura debe incluir al menos una línea de detalle")
    @Valid
    private List<FacturaDetalleItemDto> detalle;

    public FacturaRequestDto() {
    }

    public FacturaRequestDto(Long codCliente, String condicionVenta, String medioPago, String moneda,
                             BigDecimal tipoCambio, String observaciones, List<FacturaDetalleItemDto> detalle) {
        this.codCliente = codCliente;
        this.condicionVenta = condicionVenta != null ? condicionVenta : "CONTADO";
        this.medioPago = medioPago != null ? medioPago : "TRANSFERENCIA";
        this.moneda = moneda != null ? moneda : "CRC";
        this.tipoCambio = tipoCambio != null ? tipoCambio : BigDecimal.ONE;
        this.observaciones = observaciones;
        this.detalle = detalle;
    }

    public Long getCodCliente() {
        return codCliente;
    }

    public void setCodCliente(Long codCliente) {
        this.codCliente = codCliente;
    }

    public String getCondicionVenta() {
        return condicionVenta;
    }

    public void setCondicionVenta(String condicionVenta) {
        this.condicionVenta = condicionVenta;
    }

    public String getMedioPago() {
        return medioPago;
    }

    public void setMedioPago(String medioPago) {
        this.medioPago = medioPago;
    }

    public String getMoneda() {
        return moneda;
    }

    public void setMoneda(String moneda) {
        this.moneda = moneda;
    }

    public BigDecimal getTipoCambio() {
        return tipoCambio;
    }

    public void setTipoCambio(BigDecimal tipoCambio) {
        this.tipoCambio = tipoCambio;
    }

    public String getObservaciones() {
        return observaciones;
    }

    public void setObservaciones(String observaciones) {
        this.observaciones = observaciones;
    }

    public List<FacturaDetalleItemDto> getDetalle() {
        return detalle;
    }

    public void setDetalle(List<FacturaDetalleItemDto> detalle) {
        this.detalle = detalle;
    }
}
