package com.example.floodmonitor.repository;

import com.example.floodmonitor.model.Measurement;
import com.example.floodmonitor.model.StationStatus;
import com.example.floodmonitor.model.WarningLevel;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Tests fuer die In-Memory-MeasurementRepository (Limit und Reihenfolge). */
class MeasurementRepositoryTest {

    private Instant basis = Instant.parse("2026-05-01T10:00:00Z");

    private Measurement messung(String stationId, Instant timestamp) {
        return new Measurement(stationId, timestamp, 250.0, 80.0, 0.0, 12.0, 99.0,
            StationStatus.ONLINE, WarningLevel.NORMAL);
    }

    @Test
    void speichernUndAbrufenNachStation() {
        MeasurementRepository repository = new InMemoryMeasurementRepository(1000);
        repository.save(messung("ST-001", basis));
        repository.save(messung("ST-001", basis.plusSeconds(10)));

        assertEquals(2, repository.findByStationId("ST-001").size());
        assertTrue(repository.findByStationId("ST-002").isEmpty());
    }

    @Test
    void historyLimitWirftAeltesteRaus() {
        MeasurementRepository repository = new InMemoryMeasurementRepository(3);
        for (int i = 1; i <= 5; i++) {
            repository.save(messung("ST-001", basis.plusSeconds(i)));
        }

        List<Instant> zeiten = repository.findByStationId("ST-001").stream()
            .map(Measurement::getTimestamp).toList();
        assertEquals(3, zeiten.size());
        assertEquals(List.of(basis.plusSeconds(3), basis.plusSeconds(4), basis.plusSeconds(5)), zeiten);
    }

    @Test
    void reihenfolgeSortiertNachTimestampUnabhaengigVonSpeicherreihenfolge() {
        MeasurementRepository repository = new InMemoryMeasurementRepository(1000);
        repository.save(messung("ST-001", basis.plusSeconds(30)));
        repository.save(messung("ST-001", basis.plusSeconds(10)));
        repository.save(messung("ST-001", basis.plusSeconds(20)));

        List<Instant> zeiten = repository.findByStationId("ST-001").stream()
            .map(Measurement::getTimestamp).toList();
        assertEquals(List.of(basis.plusSeconds(10), basis.plusSeconds(20), basis.plusSeconds(30)), zeiten);
    }

    @Test
    void findLatestLiefertNeuesteMessung() {
        MeasurementRepository repository = new InMemoryMeasurementRepository(1000);
        repository.save(messung("ST-001", basis.plusSeconds(10)));
        repository.save(messung("ST-001", basis.plusSeconds(30)));
        repository.save(messung("ST-001", basis.plusSeconds(20)));

        assertEquals(basis.plusSeconds(30),
            repository.findLatestByStationId("ST-001").orElseThrow().getTimestamp());
    }

    @Test
    void findLatestLeerBeiUnbekannterStation() {
        MeasurementRepository repository = new InMemoryMeasurementRepository(1000);
        assertTrue(repository.findLatestByStationId("gibts-nicht").isEmpty());
    }
}
