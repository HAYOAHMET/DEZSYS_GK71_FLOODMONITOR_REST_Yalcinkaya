package at.htl.floodmonitor.dto;

import at.htl.floodmonitor.model.Location;
import jakarta.validation.Valid;
import jakarta.validation.constraints.PositiveOrZero;

/**
 * Anfrageobjekt zum teilweisen Aktualisieren einer Station (PATCH).
 * Alle Felder sind optional; nur gesetzte Felder werden uebernommen.
 */
public class StationUpdateRequest {

    private String name;

    private String river;

    @Valid
    private Location location;

    @PositiveOrZero(message = "normalWaterLevel muss >= 0 sein")
    private Double normalWaterLevel;

    @PositiveOrZero(message = "warningWaterLevel muss >= 0 sein")
    private Double warningWaterLevel;

    @PositiveOrZero(message = "criticalWaterLevel muss >= 0 sein")
    private Double criticalWaterLevel;

    private Boolean active;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getRiver() {
        return river;
    }

    public void setRiver(String river) {
        this.river = river;
    }

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    public Double getNormalWaterLevel() {
        return normalWaterLevel;
    }

    public void setNormalWaterLevel(Double normalWaterLevel) {
        this.normalWaterLevel = normalWaterLevel;
    }

    public Double getWarningWaterLevel() {
        return warningWaterLevel;
    }

    public void setWarningWaterLevel(Double warningWaterLevel) {
        this.warningWaterLevel = warningWaterLevel;
    }

    public Double getCriticalWaterLevel() {
        return criticalWaterLevel;
    }

    public void setCriticalWaterLevel(Double criticalWaterLevel) {
        this.criticalWaterLevel = criticalWaterLevel;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }
}
