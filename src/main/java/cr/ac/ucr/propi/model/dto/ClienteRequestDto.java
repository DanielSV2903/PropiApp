package cr.ac.ucr.propi.model.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * DTO para crear o actualizar un cliente.
 * Los campos sensibles (numIdentificacion, correo, telefono) se cifran
 * en la capa de repositorio antes de persistirse.
 */
public record ClienteRequestDto(
        @NotBlank(message = "El tipo de identificación es obligatorio")
        String tipoIdentificacion,

        @NotBlank(message = "El número de identificación es obligatorio")
        @Size(max = 50, message = "El número de identificación no debe exceder 50 caracteres")
        String numIdentificacion,

        @NotBlank(message = "El nombre es obligatorio")
        @Size(max = 150, message = "El nombre no debe exceder 150 caracteres")
        String nombre,

        @Email(message = "El correo debe tener un formato válido")
        String correo,

        @Size(max = 20, message = "El teléfono no debe exceder 20 caracteres")
        String telefono,

        String direccion,

        String estado
) {}
