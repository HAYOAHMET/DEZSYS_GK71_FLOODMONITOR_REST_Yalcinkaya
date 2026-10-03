package com.example.floodmonitor.model;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Prueft die JSON/XML-Darstellung von Station, insbesondere den Feldnamen "isActive". */
class StationSerializationTest {

    private final Station station = new Station("S1", "Station Linz", "Donau",
        new Location(48.3069, 14.2858), 250.0, 320.0, 380.0, true);

    private static final Set<String> ERWARTETE_FELDER = Set.of(
        "id", "name", "river", "location", "normalWaterLevel",
        "warningWaterLevel", "criticalWaterLevel", "isActive");

    @Test
    void jsonVerwendetFeldnamenIsActive() throws Exception {
        ObjectMapper mapper = JsonMapper.builder()
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
            .build();
        String json = mapper.writeValueAsString(station);
        assertTrue(json.contains("\"isActive\":true"), "Feldname isActive fehlt: " + json);
    }

    @Test
    void jsonEnthaeltIsActiveUndNichtActive() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode node = mapper.readTree(mapper.writeValueAsString(station));
        Set<String> felder = new HashSet<>();
        node.fieldNames().forEachRemaining(felder::add);
        assertEquals(ERWARTETE_FELDER, felder, "JSON-Feldnamen weichen ab (duenner 'active'?): ");
    }

    @Test
    void xmlWurzelelementIstStation() throws Exception {
        XmlMapper mapper = XmlMapper.builder().build();
        String xml = mapper.writeValueAsString(station);
        assertTrue(xml.startsWith("<station>"), "XML beginnt nicht mit <station>: " + xml);
    }

    @Test
    void isActiveLaesstSichRoundtricken() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        Station gelesen = mapper.readValue(mapper.writeValueAsString(station), Station.class);
        assertEquals(station.isActive(), gelesen.isActive());
    }
}
