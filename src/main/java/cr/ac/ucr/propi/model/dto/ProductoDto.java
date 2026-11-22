package cr.ac.ucr.propi.model.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

/**
 * DTO para Productos / Inmuebles con CABYS e Impuesto (PropiDB v2.1).
 */
public class ProductoDto {

    private Long codProducto;

    @NotBlank(message = "El código CABYS es obligatorio")
    private String codigoCabys;

    @NotBlank(message = "La descripción es obligatoria")
    private String descripcion;

    private String unidadMedida;

    @NotNull(message = "El precio es obligatorio")
    @DecimalMin(value = "0.01", message = "El precio debe ser mayor a cero")
    private BigDecimal precio;

    @NotNull(message = "El código de impuesto es obligatorio")
    private Long codImpuesto;

    // Campos de solo lectura provenientes del JOIN con tbl_tipo_impuesto
    private String nombreImpuesto;
    private BigDecimal porcentaje;

    private String estado;

    public ProductoDto() {
    }

    public ProductoDto(Long codProducto, String codigoCabys, String descripcion, String unidadMedida,
                       BigDecimal precio, Long codImpuesto, String nombreImpuesto, BigDecimal porcentaje, String estado) {
        this.codProducto = codProducto;
        this.codigoCabys = codigoCabys;
        this.descripcion = descripcion;
        this.unidadMedida = unidadMedida;
        this.precio = precio;
        this.codImpuesto = codImpuesto;
        this.nombreImpuesto = nombreImpuesto;
        this.porcentaje = porcentaje;
        this.estado = estado;
    }

    public Long getCodProducto() {
        return codProducto;
    }

    public void setCodProducto(Long codProducto) {
        this.codProducto = codProducto;
    }

    public String getCodigoCabys() {
        return codigoCabys;
    }

    public void setCodigoCabys(String codigoCabys) {
        this.codigoCabys = codigoCabys;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getUnidadMedida() {
        return unidadMedida;
    }

    public void setUnidadMedida(String unidadMedida) {
        this.unidadMedida = unidadMedida;
    }

    public BigDecimal getPrecio() {
        return precio;
    }

    public void setPrecio(BigDecimal precio) {
        this.precio = precio;
    }

    public Long getCodImpuesto() {
        return codImpuesto;
    }

    public void setCodImpuesto(Long codImpuesto) {
        this.codImpuesto = codImpuesto;
    }

    public String getNombreImpuesto() {
        return nombreImpuesto;
    }

    public void setNombreImpuesto(String nombreImpuesto) {
        this.nombreImpuesto = nombreImpuesto;
    }

    public BigDecimal getPorcentaje() {
        return porcentaje;
    }

    public void setPorcentaje(BigDecimal porcentaje) {
        this.porcentaje = porcentaje;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }
}
