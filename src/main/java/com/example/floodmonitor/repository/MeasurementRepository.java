package com.example.floodmonitor.repository;

import com.example.floodmonitor.model.Measurement;

import java.util.List;
import java.util.Optional;

/** Datenzugriff auf Messungen. */
public interface MeasurementRepository {

    Measurement save(Measurement measurement);

    List<Measurement> findByStationId(String stationId);

    Optional<Measurement> findLatestByStationId(String stationId);
}
