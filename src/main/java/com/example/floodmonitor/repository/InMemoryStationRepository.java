package com.example.floodmonitor.repository;

import com.example.floodmonitor.model.Station;
import org.springframework.stereotype.Repository;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/** In-Memory-Implementierung, threadsicher ueber ConcurrentHashMap. */
@Repository
public class InMemoryStationRepository implements StationRepository {

    private final Map<String, Station> stations = new ConcurrentHashMap<>();

    @Override
    public List<Station> findAll() {
        // nach ID sortiert fuer stabile Ausgabe
        return stations.values().stream()
            .sorted(Comparator.comparing(Station::getId))
            .collect(Collectors.toList());
    }

    @Override
    public Optional<Station> findById(String id) {
        return Optional.ofNullable(stations.get(id));
    }

    @Override
    public boolean existsById(String id) {
        return stations.containsKey(id);
    }

    @Override
    public Station save(Station station) {
        stations.put(station.getId(), station);
        return station;
    }

    @Override
    public void deleteById(String id) {
        stations.remove(id);
    }
}
