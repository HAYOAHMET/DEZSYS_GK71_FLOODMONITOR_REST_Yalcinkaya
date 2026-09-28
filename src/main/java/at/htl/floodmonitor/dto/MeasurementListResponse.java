package at.htl.floodmonitor.dto;

import at.htl.floodmonitor.model.Measurement;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

import java.util.List;

/**
 * Antwort-Wrapper fuer Messwertlisten (saubere JSON- und XML-Darstellung).
 */
@JacksonXmlRootElement(localName = "measurements")
public class MeasurementListResponse {

    @JacksonXmlProperty(localName = "stationId")
    private String stationId;

    @JacksonXmlProperty(localName = "count")
    private int count;

    @JacksonXmlElementWrapper(useWrapping = false)
    @JacksonXmlProperty(localName = "measurement")
    private List<Measurement> measurements;

    public MeasurementListResponse() {
    }

    public MeasurementListResponse(String stationId, List<Measurement> measurements) {
        this.stationId = stationId;
        this.measurements = measurements;
        this.count = measurements != null ? measurements.size() : 0;
    }

    public String getStationId() {
        return stationId;
    }

    public void setStationId(String stationId) {
        this.stationId = stationId;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public List<Measurement> getMeasurements() {
        return measurements;
    }

    public void setMeasurements(List<Measurement> measurements) {
        this.measurements = measurements;
        this.count = measurements != null ? measurements.size() : 0;
    }
}
