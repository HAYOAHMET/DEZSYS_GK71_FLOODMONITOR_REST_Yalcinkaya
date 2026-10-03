package com.example.floodmonitor.config;

import com.example.floodmonitor.model.Station;
import com.example.floodmonitor.repository.StationRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Prueft, dass der Initializer die vier Seed-Stationen anlegt. */
@SpringBootTest
class SeedDataConfigTest {

    @Autowired
    private StationRepository stationRepository;

    @Test
    void startEnthaeltGenauDieVierStationen() {
        List<String> ids = stationRepository.findAll().stream().map(Station::getId).toList();
        assertEquals(List.of("ST-001", "ST-002", "ST-003", "ST-004"), ids);
    }

    @Test
    void linzTraunIstDeaktiviert() {
        Station linzTraun = stationRepository.findById("ST-004").orElseThrow();
        assertFalse(linzTraun.isActive());
        assertEquals("Traun", linzTraun.getRiver());
    }

    @Test
    void wienNussdorfHatPlausibleWerte() {
        Station wien = stationRepository.findById("ST-001").orElseThrow();
        assertTrue(wien.isActive());
        assertEquals("Donau", wien.getRiver());
        assertEquals(250.0, wien.getNormalWaterLevel());
        assertEquals(300.0, wien.getWarningWaterLevel());
        assertEquals(450.0, wien.getCriticalWaterLevel());
    }
}
