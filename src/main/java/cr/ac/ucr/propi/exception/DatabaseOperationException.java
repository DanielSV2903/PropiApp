package cr.ac.ucr.propi.exception;

/**
 * Excepción lanzada cuando una operación de base de datos falla.
 * Envuelve SQLException / DataAccessException con un mensaje seguro para el cliente.
 */
public class DatabaseOperationException extends RuntimeException {

    public DatabaseOperationException(String message) {
        super(message);
    }

    public DatabaseOperationException(String message, Throwable cause) {
        super(message, cause);
    }
}
