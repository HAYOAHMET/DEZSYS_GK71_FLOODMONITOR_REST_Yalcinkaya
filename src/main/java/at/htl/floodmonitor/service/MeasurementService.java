package at.htl.floodmonitor.service;

import at.htl.floodmonitor.exception.InvalidRequestException;
import at.htl.floodmonitor.exception.StationNotFoundException;
import at.htl.floodmonitor.model.Measurement;
import at.htl.floodmonitor.model.Station;
import at.htl.floodmonitor.model.WarningLevel;
import at.htl.floodmonitor.repository.MeasurementRepository;
import at.htl.floodmonitor.repository.StationRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Geschaeftslogik fuer Messwerte: aktuellster Wert, historische Werte mit
 * Filtern sowie aktuelle Warnungen.
 */
@Service
public class MeasurementService {

    private static final int MAX_LIMIT = 1000;

    private final MeasurementRepository measurementRepository;
    private final StationRepository stationRepository;

    @Value("${simulation.max-limit:1000}")
    private int maxLimit = MAX_LIMIT;

    public MeasurementService(MeasurementRepository measurementRepository,
                              StationRepository stationRepository) {
        this.measurementRepository = measurementRepository;
        this.stationRepository = stationRepository;
    }

    public Measurement getLatest(String stationId) {
        requireStation(stationId);
        return measurementRepository.findLatest(stationId)
                .orElseThrow(() -> new StationNotFoundException(
                        "Fuer Station " + stationId + " existiert keine Messung"));
    }

    public Optional<Measurement> findLatestOptional(String stationId) {
        return measurementRepository.findLatest(stationId);
    }

    /**
     * Historische Messwerte einer Station mit optionalen Filtern.
     */
    public List<Measurement> getHistory(String stationId, Instant from, Instant to,
                                        Integer limit, WarningLevel warningLevel) {
        requireStation(stationId);

        if (from != null && to != null && from.isAfter(to)) {
            throw new InvalidRequestException("'from' darf nicht nach 'to' liegen");
        }
        if (limit != null) {
            if (limit < 0) {
                throw new InvalidRequestException("'limit' darf nicht negativ sein");
            }
            if (limit > maxLimit) {
                throw new InvalidRequestException("'limit' darf hoechstens " + maxLimit + " betragen");
            }
        }

        List<Measurement> result = measurementRepository.findByStationId(stationId).stream()
                .filter(m -> from == null || !m.getTimestamp().isBefore(from))
                .filter(m -> to == null || !m.getTimestamp().isAfter(to))
                .filter(m -> warningLevel == null || m.getWarningLevel() == warningLevel)
                .sorted(Comparator.comparing(Measurement::getTimestamp).reversed())
                .collect(Collectors.toList());

        if (limit != null && result.size() > limit) {
            result = new ArrayList<>(result.subList(0, limit));
        }
        return result;
    }

    /**
     * Liefert die aktuellen Warnungen (aktuellste Messung je Station), optional
     * gefiltert nach Warnstufe und Fluss. Ohne Warnstufen-Filter werden nur
     * Messungen mit WARNING oder CRITICAL zurueckgegeben.
     */
    public List<Measurement> getAlerts(WarningLevel warningLevel, String river) {
        List<Measurement> alerts = new ArrayList<>();
        for (Station station : stationRepository.findAll()) {
            if (river != null && !station.getRiver().equalsIgnoreCase(river)) {
                continue;
            }
            Optional<Measurement> latest = measurementRepository.findLatest(station.getId());
            if (latest.isEmpty()) {
                continue;
            }
            Measurement m = latest.get();
            if (warningLevel != null) {
                if (m.getWarningLevel() == warningLevel) {
                    alerts.add(m);
                }
            } else if (m.getWarningLevel() == WarningLevel.WARNING
                    || m.getWarningLevel() == WarningLevel.CRITICAL) {
                alerts.add(m);
            }
        }
        alerts.sort(Comparator.comparing(Measurement::getStationId));
        return alerts;
    }

    private void requireStation(String stationId) {
        if (!stationRepository.existsById(stationId)) {
            throw new StationNotFoundException(stationId);
        }
    }
}
