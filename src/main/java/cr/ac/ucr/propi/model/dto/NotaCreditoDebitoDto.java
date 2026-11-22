package cr.ac.ucr.propi.model.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO para Notas de Crédito / Débito (PropiDB v2.1).
 */
public class NotaCreditoDebitoDto {

    private Long codNota;

    @NotBlank(message = "El código de la nota es obligatorio")
    private String codigo;

    private LocalDateTime fecNota;

    @NotNull(message = "El código de la factura asociada es obligatorio")
    private Long codFactura;

    @NotBlank(message = "El tipo de nota (CREDITO/DEBITO) es obligatorio")
    private String tipo;

    @NotBlank(message = "El motivo es obligatorio")
    private String motivo;

    @NotNull(message = "El monto de ajuste es obligatorio")
    @DecimalMin(value = "0.01", message = "El monto de ajuste debe ser mayor a cero")
    private BigDecimal montoAjuste;

    // Solo lectura proveniente del JOIN
    private String nombreCliente;

    public NotaCreditoDebitoDto() {
    }

    public NotaCreditoDebitoDto(Long codNota, String codigo, LocalDateTime fecNota, Long codFactura,
                                String tipo, String motivo, BigDecimal montoAjuste, String nombreCliente) {
        this.codNota = codNota;
        this.codigo = codigo;
        this.fecNota = fecNota;
        this.codFactura = codFactura;
        this.tipo = tipo;
        this.motivo = motivo;
        this.montoAjuste = montoAjuste;
        this.nombreCliente = nombreCliente;
    }

    public Long getCodNota() {
        return codNota;
    }

    public void setCodNota(Long codNota) {
        this.codNota = codNota;
    }

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }

    public LocalDateTime getFecNota() {
        return fecNota;
    }

    public void setFecNota(LocalDateTime fecNota) {
        this.fecNota = fecNota;
    }

    public Long getCodFactura() {
        return codFactura;
    }

    public void setCodFactura(Long codFactura) {
        this.codFactura = codFactura;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public String getMotivo() {
        return motivo;
    }

    public void setMotivo(String motivo) {
        this.motivo = motivo;
    }

    public BigDecimal getMontoAjuste() {
        return montoAjuste;
    }

    public void setMontoAjuste(BigDecimal montoAjuste) {
        this.montoAjuste = montoAjuste;
    }

    public String getNombreCliente() {
        return nombreCliente;
    }

    public void setNombreCliente(String nombreCliente) {
        this.nombreCliente = nombreCliente;
    }
}
