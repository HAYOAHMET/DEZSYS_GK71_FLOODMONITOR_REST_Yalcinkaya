package at.htl.floodmonitor.controller;

import at.htl.floodmonitor.dto.MeasurementListResponse;
import at.htl.floodmonitor.model.Measurement;
import at.htl.floodmonitor.model.WarningLevel;
import at.htl.floodmonitor.service.MeasurementService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.List;

/**
 * REST-Controller fuer Messwerte einer Station.
 */
@RestController
@RequestMapping(value = "/api/v1/stations/{stationId}/measurements",
        produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
public class MeasurementController {

    private final MeasurementService measurementService;

    public MeasurementController(MeasurementService measurementService) {
        this.measurementService = measurementService;
    }

    /**
     * Aktuellster Messwert.
     */
    @GetMapping("/latest")
    public Measurement getLatest(@PathVariable String stationId) {
        return measurementService.getLatest(stationId);
    }

    /**
     * Historische Messwerte mit optionalen Filtern (from, to, limit, warningLevel).
     */
    @GetMapping
    public MeasurementListResponse getHistory(
            @PathVariable String stationId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant to,
            @RequestParam(required = false) Integer limit,
            @RequestParam(required = false) WarningLevel warningLevel) {
        List<Measurement> history = measurementService.getHistory(stationId, from, to, limit, warningLevel);
        return new MeasurementListResponse(stationId, history);
    }
}
