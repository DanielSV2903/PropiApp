package cr.ac.ucr.propi.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.UncategorizedSQLException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Manejador centralizado de excepciones (PropiDB v2.1).
 * Cumple con el REQ 008 procesando errores nativos de SQL Server:
 * - Error 229 (Permission Denied) -> 403 Forbidden
 * - Errores de negocio (THROW 500xx, CHECKs, triggers) -> 400 Bad Request
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<Map<String, Object>> handleInvalidCredentials(InvalidCredentialsException ex) {
        return buildResponse(HttpStatus.UNAUTHORIZED, ex.getMessage());
    }

    @ExceptionHandler(DatabaseOperationException.class)
    public ResponseEntity<Map<String, Object>> handleDatabaseOperation(DatabaseOperationException ex) {
        log.error("Error de operación de negocio", ex);
        return buildResponse(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    @ExceptionHandler(UncategorizedSQLException.class)
    public ResponseEntity<Map<String, Object>> handleUncategorizedSql(UncategorizedSQLException ex) {
        SQLException sqlEx = ex.getSQLException();
        log.warn("UncategorizedSQLException capturada: ErrorCode={}, SQLState={}", sqlEx.getErrorCode(), sqlEx.getSQLState());

        // Error 229: The EXECUTE/SELECT/DELETE permission was denied on the object (REQ 008)
        if (sqlEx.getErrorCode() == 229) {
            return buildResponse(HttpStatus.FORBIDDEN,
                    "Acceso Denegado (REQ 008): " + sqlEx.getMessage());
        }

        // Errores de negocio personalizados (THROW 50012, 50040, 50051) o RAISERROR de triggers
        if (sqlEx.getErrorCode() >= 50000 || sqlEx.getErrorCode() == 547) {
            return buildResponse(HttpStatus.BAD_REQUEST, sqlEx.getMessage());
        }

        log.error("Error SQL no categorizado", ex);
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR,
                "Error al procesar la solicitud en base de datos: " + sqlEx.getMessage());
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Map<String, Object>> handleDataIntegrity(DataIntegrityViolationException ex) {
        log.warn("Violación de integridad de datos", ex);
        Throwable root = ex.getMostSpecificCause();
        String message = root != null ? root.getMessage() : ex.getMessage();
        return buildResponse(HttpStatus.BAD_REQUEST, "Violación de regla de integridad: " + message);
    }

    @ExceptionHandler(SQLException.class)
    public ResponseEntity<Map<String, Object>> handleSqlException(SQLException ex) {
        log.warn("SQLException directa capturada: ErrorCode={}, SQLState={}", ex.getErrorCode(), ex.getSQLState());

        if (ex.getErrorCode() == 229) {
            return buildResponse(HttpStatus.FORBIDDEN,
                    "Acceso Denegado por seguridad SQL Server (REQ 008): " + ex.getMessage());
        }

        if (ex.getErrorCode() >= 50000 || ex.getErrorCode() == 547) {
            return buildResponse(HttpStatus.BAD_REQUEST, ex.getMessage());
        }

        log.error("Error SQL", ex);
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR,
                "Error interno de base de datos. Contacte al administrador.");
    }

    @ExceptionHandler(DataAccessException.class)
    public ResponseEntity<Map<String, Object>> handleDataAccess(DataAccessException ex) {
        log.error("DataAccessException capturada", ex);
        Throwable root = ex.getMostSpecificCause();
        String message = root != null ? root.getMessage() : ex.getMessage();
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR,
                "Error al acceder a la base de datos: " + message);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidation(MethodArgumentNotValidException ex) {
        Map<String, String> fieldErrors = new HashMap<>();
        for (FieldError fe : ex.getBindingResult().getFieldErrors()) {
            fieldErrors.put(fe.getField(), fe.getDefaultMessage());
        }
        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", LocalDateTime.now().toString());
        body.put("status", HttpStatus.BAD_REQUEST.value());
        body.put("error", "Errores de validación");
        body.put("fieldErrors", fieldErrors);
        return ResponseEntity.badRequest().body(body);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGeneral(Exception ex) {
        log.error("Excepción no controlada", ex);
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR,
                "Error interno del servidor: " + ex.getMessage());
    }

    // ──────────────────────────────────────────────────────────────────────────
    private ResponseEntity<Map<String, Object>> buildResponse(HttpStatus status, String message) {
        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", LocalDateTime.now().toString());
        body.put("status", status.value());
        body.put("error", status.getReasonPhrase());
        body.put("message", message);
        return ResponseEntity.status(status).body(body);
    }
}
