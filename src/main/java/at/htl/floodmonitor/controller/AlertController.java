package at.htl.floodmonitor.controller;

import at.htl.floodmonitor.dto.AlertListResponse;
import at.htl.floodmonitor.model.WarningLevel;
import at.htl.floodmonitor.service.MeasurementService;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST-Controller fuer aktuelle Warnungen des Fruehwarnsystems.
 */
@RestController
@RequestMapping(value = "/api/v1/alerts",
        produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
public class AlertController {

    private final MeasurementService measurementService;

    public AlertController(MeasurementService measurementService) {
        this.measurementService = measurementService;
    }

    @GetMapping
    public AlertListResponse getAlerts(
            @RequestParam(required = false) WarningLevel warningLevel,
            @RequestParam(required = false) String river) {
        return new AlertListResponse(measurementService.getAlerts(warningLevel, river));
    }
}
