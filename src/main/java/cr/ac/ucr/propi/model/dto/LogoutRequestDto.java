package cr.ac.ucr.propi.model.dto;

import jakarta.validation.constraints.NotNull;

/**
 * DTO para la solicitud de cierre de sesión.
 */
public record LogoutRequestDto(
        @NotNull(message = "El código de acceso es obligatorio")
        Long codAcceso
) {}
