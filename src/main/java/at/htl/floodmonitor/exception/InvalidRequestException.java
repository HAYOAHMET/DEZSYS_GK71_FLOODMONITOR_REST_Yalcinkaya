package at.htl.floodmonitor.exception;

/**
 * Wird bei ungueltigen Anfrageparametern geworfen (400 Bad Request),
 * z. B. bei "from" nach "to" oder ungueltigen limit-Werten.
 */
public class InvalidRequestException extends RuntimeException {

    public InvalidRequestException(String message) {
        super(message);
    }
}
