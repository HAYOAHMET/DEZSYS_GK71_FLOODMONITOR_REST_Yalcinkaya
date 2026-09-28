package at.htl.floodmonitor.dto;

import at.htl.floodmonitor.model.Station;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

import java.util.List;

/**
 * Antwort-Wrapper fuer Stationslisten (saubere JSON- und XML-Darstellung).
 */
@JacksonXmlRootElement(localName = "stations")
public class StationListResponse {

    @JacksonXmlProperty(localName = "count")
    private int count;

    @JacksonXmlElementWrapper(useWrapping = false)
    @JacksonXmlProperty(localName = "station")
    private List<Station> stations;

    public StationListResponse() {
    }

    public StationListResponse(List<Station> stations) {
        this.stations = stations;
        this.count = stations != null ? stations.size() : 0;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public List<Station> getStations() {
        return stations;
    }

    public void setStations(List<Station> stations) {
        this.stations = stations;
        this.count = stations != null ? stations.size() : 0;
    }
}
