package cr.ac.ucr.propi.repository.base;

import org.springframework.jdbc.core.JdbcTemplate;

/**
 * Clase base para repositorios JDBC que asegura la propagación del contexto
 * de sesión de SQL Server (REQ 005) antes de invocar procedimientos de escritura.
 */
public abstract class SessionContextAwareRepository {

    protected void setSessionContext(JdbcTemplate jdbc, String username) {
        jdbc.update("EXEC sys.sp_set_session_context @key = N'cod_usuario', @value = ?", username);
        jdbc.update("EXEC sys.sp_set_session_context @key = N'cod_sesion', @value = ?", 1);
    }

    protected void setSessionContext(JdbcTemplate jdbc, String username, Object sessionId) {
        jdbc.update("EXEC sys.sp_set_session_context @key = N'cod_usuario', @value = ?", username);
        jdbc.update("EXEC sys.sp_set_session_context @key = N'cod_sesion', @value = ?", sessionId != null ? sessionId : 1);
    }
}
