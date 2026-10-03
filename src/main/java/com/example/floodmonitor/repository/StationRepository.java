package com.example.floodmonitor.repository;

import com.example.floodmonitor.model.Station;

import java.util.List;
import java.util.Optional;

/** Datenzugriff auf Stationen. */
public interface StationRepository {

    List<Station> findAll();

    Optional<Station> findById(String id);

    boolean existsById(String id);

    Station save(Station station);

    void deleteById(String id);
}
