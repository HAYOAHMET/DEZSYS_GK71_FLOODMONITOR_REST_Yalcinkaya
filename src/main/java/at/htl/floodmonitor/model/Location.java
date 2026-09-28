package at.htl.floodmonitor.model;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

/**
 * Geografische Position einer Messstation (Breiten- und Laengengrad).
 */
public class Location {

    @NotNull(message = "latitude darf nicht null sein")
    @DecimalMin(value = "-90.0", message = "latitude muss >= -90 sein")
    @DecimalMax(value = "90.0", message = "latitude muss <= 90 sein")
    private Double latitude;

    @NotNull(message = "longitude darf nicht null sein")
    @DecimalMin(value = "-180.0", message = "longitude muss >= -180 sein")
    @DecimalMax(value = "180.0", message = "longitude muss <= 180 sein")
    private Double longitude;

    public Location() {
    }

    public Location(Double latitude, Double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }
}
