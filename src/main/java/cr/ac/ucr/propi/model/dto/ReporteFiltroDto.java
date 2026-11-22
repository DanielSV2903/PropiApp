package cr.ac.ucr.propi.model.dto;

import java.time.LocalDate;

/**
 * DTO para filtrar reportes por rango de fechas, estado y/o usuario.
 */
public record ReporteFiltroDto(
        LocalDate desde,
        LocalDate hasta,
        String estado,
        String usuario
) {}
