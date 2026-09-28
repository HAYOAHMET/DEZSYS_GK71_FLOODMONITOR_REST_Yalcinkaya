package at.htl.floodmonitor.repository;

import at.htl.floodmonitor.model.Measurement;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * In-Memory-Ablage der Messungen, gruppiert nach Stations-ID.
 * Messungen werden chronologisch (aeltester zuerst) gehalten.
 */
@Repository
public class MeasurementRepository {

    private final Map<String, List<Measurement>> measurementsByStation = new ConcurrentHashMap<>();

    public void add(Measurement measurement) {
        measurementsByStation
                .computeIfAbsent(measurement.getStationId(), k -> new CopyOnWriteArrayList<>())
                .add(measurement);
    }

    /**
     * Liefert alle Messungen einer Station chronologisch aufsteigend sortiert.
     */
    public List<Measurement> findByStationId(String stationId) {
        List<Measurement> list = measurementsByStation.getOrDefault(stationId, List.of());
        List<Measurement> copy = new ArrayList<>(list);
        copy.sort(Comparator.comparing(Measurement::getTimestamp));
        return copy;
    }

    /**
     * Liefert die aktuellste Messung einer Station.
     */
    public Optional<Measurement> findLatest(String stationId) {
        List<Measurement> list = measurementsByStation.getOrDefault(stationId, List.of());
        return list.stream().max(Comparator.comparing(Measurement::getTimestamp));
    }

    public void deleteByStationId(String stationId) {
        measurementsByStation.remove(stationId);
    }
}
