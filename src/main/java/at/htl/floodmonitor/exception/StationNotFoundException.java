package at.htl.floodmonitor.exception;

/**
 * Wird geworfen, wenn eine Station mit der angegebenen ID nicht existiert (404).
 */
public class StationNotFoundException extends RuntimeException {

    public StationNotFoundException(String stationId) {
        super("Station " + stationId + " wurde nicht gefunden");
    }
}
