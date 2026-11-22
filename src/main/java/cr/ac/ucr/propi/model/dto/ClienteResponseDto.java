package cr.ac.ucr.propi.model.dto;

import java.time.LocalDateTime;

/**
 * DTO de respuesta para datos de cliente (con campos sensibles ya desencriptados).
 */
public record ClienteResponseDto(
        Long codCliente,
        String tipoIdentificacion,
        String numIdentificacion,
        String nombre,
        String correo,
        String telefono,
        String direccion,
        String estado,
        LocalDateTime fecRegistro,
        LocalDateTime fecModificacion
) {}
