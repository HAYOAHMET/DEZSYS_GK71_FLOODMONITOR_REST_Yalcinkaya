package at.htl.floodmonitor.controller;

import at.htl.floodmonitor.dto.StationCreateRequest;
import at.htl.floodmonitor.dto.StationListResponse;
import at.htl.floodmonitor.dto.StationUpdateRequest;
import at.htl.floodmonitor.model.Station;
import at.htl.floodmonitor.model.StationStatistics;
import at.htl.floodmonitor.model.WarningLevel;
import at.htl.floodmonitor.service.StationService;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

/**
 * REST-Controller fuer Stationen. Enthaelt keine umfangreiche Geschaeftslogik,
 * sondern delegiert an {@link StationService}.
 */
@RestController
@RequestMapping(value = "/api/v1/stations",
        produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
public class StationController {

    private final StationService stationService;

    public StationController(StationService stationService) {
        this.stationService = stationService;
    }

    @GetMapping
    public StationListResponse getStations(
            @RequestParam(required = false) String river,
            @RequestParam(required = false) Boolean active,
            @RequestParam(required = false) WarningLevel warningLevel) {
        return new StationListResponse(stationService.findStations(river, active, warningLevel));
    }

    @GetMapping("/{stationId}")
    public Station getStation(@PathVariable String stationId) {
        return stationService.getStation(stationId);
    }

    @GetMapping("/{stationId}/statistics")
    public StationStatistics getStatistics(@PathVariable String stationId) {
        return stationService.getStatistics(stationId);
    }

    // ----------------------------------------------------------
    //  Verwaltung (nur ADMIN)
    // ----------------------------------------------------------

    @PostMapping(consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
    public ResponseEntity<Station> createStation(@Valid @RequestBody StationCreateRequest request) {
        Station created = stationService.createStation(request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(created.getId())
                .toUri();
        return ResponseEntity.created(location).body(created);
    }

    @PatchMapping(value = "/{stationId}",
            consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
    public Station updateStation(@PathVariable String stationId,
                                 @Valid @RequestBody StationUpdateRequest request) {
        return stationService.updateStation(stationId, request);
    }

    @DeleteMapping("/{stationId}")
    public ResponseEntity<Void> deleteStation(@PathVariable String stationId) {
        stationService.deleteStation(stationId);
        return ResponseEntity.noContent().build();
    }
}
