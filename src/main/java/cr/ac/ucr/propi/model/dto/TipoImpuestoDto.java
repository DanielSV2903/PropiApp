package cr.ac.ucr.propi.model.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

/**
 * DTO para Tipos de Impuesto (PropiDB v2.1).
 */
public class TipoImpuestoDto {

    private Long codImpuesto;

    @NotBlank(message = "El código de impuesto es obligatorio")
    private String codigo;

    @NotBlank(message = "La descripción es obligatoria")
    private String descripcion;

    @NotNull(message = "El porcentaje es obligatorio")
    @DecimalMin(value = "0.0", inclusive = true, message = "El porcentaje debe ser mayor o igual a 0")
    @DecimalMax(value = "100.0", inclusive = true, message = "El porcentaje no puede ser mayor a 100")
    private BigDecimal porcentaje;

    @NotBlank(message = "La tarifa es obligatoria")
    private String tarifa;

    private String estado;

    public TipoImpuestoDto() {
    }

    public TipoImpuestoDto(Long codImpuesto, String codigo, String descripcion,
                           BigDecimal porcentaje, String tarifa, String estado) {
        this.codImpuesto = codImpuesto;
        this.codigo = codigo;
        this.descripcion = descripcion;
        this.porcentaje = porcentaje;
        this.tarifa = tarifa;
        this.estado = estado;
    }

    public Long getCodImpuesto() {
        return codImpuesto;
    }

    public void setCodImpuesto(Long codImpuesto) {
        this.codImpuesto = codImpuesto;
    }

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public BigDecimal getPorcentaje() {
        return porcentaje;
    }

    public void setPorcentaje(BigDecimal porcentaje) {
        this.porcentaje = porcentaje;
    }

    public String getTarifa() {
        return tarifa;
    }

    public void setTarifa(String tarifa) {
        this.tarifa = tarifa;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }
}
