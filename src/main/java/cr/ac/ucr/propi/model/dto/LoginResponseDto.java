package cr.ac.ucr.propi.model.dto;

import java.util.List;

/**
 * DTO para la respuesta de inicio de sesión exitoso.
 * Incluye el token JWT, el usuario, los roles y el codAcceso generado por sp_registrar_ingreso.
 */
public record LoginResponseDto(
        String token,
        String username,
        List<String> roles,
        long expiresInMs,
        Long codAcceso
) {
    public LoginResponseDto(String token, String username, List<String> roles, long expiresInMs) {
        this(token, username, roles, expiresInMs, null);
    }
}
