package at.htl.floodmonitor.dto;

import at.htl.floodmonitor.model.Location;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

/**
 * Anfrageobjekt zum Anlegen einer neuen Station (POST).
 */
public class StationCreateRequest {

    @NotBlank(message = "id darf nicht leer sein")
    private String id;

    @NotBlank(message = "name darf nicht leer sein")
    private String name;

    @NotBlank(message = "river darf nicht leer sein")
    private String river;

    @NotNull(message = "location darf nicht null sein")
    @Valid
    private Location location;

    @NotNull(message = "normalWaterLevel darf nicht null sein")
    @PositiveOrZero(message = "normalWaterLevel muss >= 0 sein")
    private Double normalWaterLevel;

    @NotNull(message = "warningWaterLevel darf nicht null sein")
    @PositiveOrZero(message = "warningWaterLevel muss >= 0 sein")
    private Double warningWaterLevel;

    @NotNull(message = "criticalWaterLevel darf nicht null sein")
    @PositiveOrZero(message = "criticalWaterLevel muss >= 0 sein")
    private Double criticalWaterLevel;

    private Boolean active = true;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

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
