package at.htl.floodmonitor.repository;

import at.htl.floodmonitor.model.Station;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * In-Memory-Ablage der Messstationen.
 */
@Repository
public class StationRepository {

    private final Map<String, Station> stations = new ConcurrentHashMap<>();

    public List<Station> findAll() {
        return new ArrayList<>(stations.values());
    }

    public Optional<Station> findById(String id) {
        return Optional.ofNullable(stations.get(id));
    }

    public boolean existsById(String id) {
        return stations.containsKey(id);
    }

    public Station save(Station station) {
        stations.put(station.getId(), station);
        return station;
    }

    public boolean deleteById(String id) {
        return stations.remove(id) != null;
    }
}
