package at.htl.floodmonitor.dto;

import at.htl.floodmonitor.model.Measurement;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

import java.util.List;

/**
 * Antwort-Wrapper fuer aktuelle Warnungen (saubere JSON- und XML-Darstellung).
 */
@JacksonXmlRootElement(localName = "alerts")
public class AlertListResponse {

    @JacksonXmlProperty(localName = "count")
    private int count;

    @JacksonXmlElementWrapper(useWrapping = false)
    @JacksonXmlProperty(localName = "alert")
    private List<Measurement> alerts;

    public AlertListResponse() {
    }

    public AlertListResponse(List<Measurement> alerts) {
        this.alerts = alerts;
        this.count = alerts != null ? alerts.size() : 0;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public List<Measurement> getAlerts() {
        return alerts;
    }

    public void setAlerts(List<Measurement> alerts) {
        this.alerts = alerts;
        this.count = alerts != null ? alerts.size() : 0;
    }
}
