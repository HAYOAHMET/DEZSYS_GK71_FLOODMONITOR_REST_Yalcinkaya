package com.example.floodmonitor.repository;

import com.example.floodmonitor.model.Location;
import com.example.floodmonitor.model.Station;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Tests fuer die In-Memory-StationRepository. */
class StationRepositoryTest {

    private final StationRepository repository = new InMemoryStationRepository();

    private Station station(String id) {
        return new Station(id, "Station " + id, "Donau", new Location(48.0, 16.0),
            200.0, 300.0, 400.0, true);
    }

    @Test
    void speichernUndAbrufen() {
        Station gespeichert = repository.save(station("ST-001"));

        assertTrue(repository.existsById("ST-001"));
        assertEquals(gespeichert, repository.findById("ST-001").orElseThrow());
    }

    @Test
    void findAllLiefertAlleSortiertNachId() {
        repository.save(station("ST-003"));
        repository.save(station("ST-001"));
        repository.save(station("ST-002"));

        assertEquals(List.of("ST-001", "ST-002", "ST-003"),
            repository.findAll().stream().map(Station::getId).toList());
    }

    @Test
    void speichernUeberschreibtBeiGleicherId() {
        repository.save(station("ST-001"));
        Station geaendert = new Station("ST-001", "Neuer Name", "Donau",
            new Location(48.0, 16.0), 210.0, 310.0, 410.0, false);
        repository.save(geaendert);

        assertEquals(1, repository.findAll().size());
        assertEquals("Neuer Name", repository.findById("ST-001").orElseThrow().getName());
    }

    @Test
    void loeschenEntferntStation() {
        repository.save(station("ST-001"));
        repository.deleteById("ST-001");

        assertFalse(repository.existsById("ST-001"));
        assertEquals(Optional.empty(), repository.findById("ST-001"));
    }

    @Test
    void unbekannteIdLiefertLeer() {
        assertFalse(repository.existsById("gibts-nicht"));
        assertTrue(repository.findById("gibts-nicht").isEmpty());
    }
}
