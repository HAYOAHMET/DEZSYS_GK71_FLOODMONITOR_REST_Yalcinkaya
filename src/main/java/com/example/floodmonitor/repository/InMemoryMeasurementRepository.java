package com.example.floodmonitor.repository;

import com.example.floodmonitor.model.Measurement;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/** In-Memory-Implementierung: pro Station eine nach timestamp sortierte, begrenzte History. */
@Repository
public class InMemoryMeasurementRepository implements MeasurementRepository {

    private static final Comparator<Measurement> NACH_TIMESTAMP =
        Comparator.comparing(Measurement::getTimestamp);

    private final int maxPerStation;

    // ConcurrentHashMap wegen paralleler Schreibzugriffe durch den Simulator;
    // jede Stations-History ist ein eigenes, synchronisiertes List-Objekt
    private final Map<String, List<Measurement>> historie = new ConcurrentHashMap<>();

    public InMemoryMeasurementRepository(
            @Value("${measurement.history.max-per-station:1000}") int maxPerStation) {
        if (maxPerStation < 1) {
            throw new IllegalArgumentException("measurement.history.max-per-station muss >= 1 sein");
        }
        this.maxPerStation = maxPerStation;
    }

    @Override
    public Measurement save(Measurement measurement) {
        List<Measurement> queue = historie.computeIfAbsent(
            measurement.getStationId(), id -> new ArrayList<>());
        synchronized (queue) {
            // sortierte Einfuegeposition suchen, aelteste Messung fliegt raus
            int position = Collections.binarySearch(queue, measurement, NACH_TIMESTAMP);
            if (position < 0) {
                position = -(position + 1);
            }
            queue.add(position, measurement);
            while (queue.size() > maxPerStation) {
                queue.remove(0);
            }
        }
        return measurement;
    }

    @Override
    public List<Measurement> findByStationId(String stationId) {
        List<Measurement> queue = historie.get(stationId);
        if (queue == null) {
            return List.of();
        }
        synchronized (queue) {
            return new ArrayList<>(queue);
        }
    }

    @Override
    public Optional<Measurement> findLatestByStationId(String stationId) {
        List<Measurement> queue = historie.get(stationId);
        if (queue == null) {
            return Optional.empty();
        }
        synchronized (queue) {
            return queue.isEmpty() ? Optional.empty() : Optional.of(queue.get(queue.size() - 1));
        }
    }
}
