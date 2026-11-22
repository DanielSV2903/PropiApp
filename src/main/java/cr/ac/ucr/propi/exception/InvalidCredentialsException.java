package cr.ac.ucr.propi.exception;

/**
 * Excepción lanzada cuando las credenciales de SQL Server son inválidas.
 */
public class InvalidCredentialsException extends RuntimeException {

    public InvalidCredentialsException(String message) {
        super(message);
    }

    public InvalidCredentialsException(String message, Throwable cause) {
        super(message, cause);
    }
}
