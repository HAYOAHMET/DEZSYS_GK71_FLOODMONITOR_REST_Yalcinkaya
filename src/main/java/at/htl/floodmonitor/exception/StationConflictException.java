package at.htl.floodmonitor.exception;

/**
 * Wird geworfen, wenn eine Station mit gleicher ID bereits existiert (409 Conflict).
 */
public class StationConflictException extends RuntimeException {

    public StationConflictException(String stationId) {
        super("Station " + stationId + " existiert bereits");
    }
}
