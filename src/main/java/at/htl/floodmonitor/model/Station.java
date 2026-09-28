package at.htl.floodmonitor.model;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

/**
 * Eine Messstation des Hochwasser-Fruehwarnsystems an einem Flussabschnitt.
 */
@JacksonXmlRootElement(localName = "station")
public class Station {

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

    private boolean active = true;

    public Station() {
    }

    public Station(String id, String name, String river, Location location,
                   Double normalWaterLevel, Double warningWaterLevel,
                   Double criticalWaterLevel, boolean active) {
        this.id = id;
        this.name = name;
        this.river = river;
        this.location = location;
        this.normalWaterLevel = normalWaterLevel;
        this.warningWaterLevel = warningWaterLevel;
        this.criticalWaterLevel = criticalWaterLevel;
        this.active = active;
    }

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

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}
