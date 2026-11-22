package cr.ac.ucr.propi.model.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

/**
 * DTO para cada línea de detalle de una factura.
 */
public record FacturaDetalleDto(
        @NotNull(message = "El código del producto es obligatorio")
        Long codProducto,

        @NotNull(message = "La cantidad es obligatoria")
        @Positive(message = "La cantidad debe ser positiva")
        Integer cantidad,

        @NotNull(message = "El precio unitario es obligatorio")
        @Positive(message = "El precio unitario debe ser positivo")
        BigDecimal precioUnitario,

        BigDecimal descuento
) {}
