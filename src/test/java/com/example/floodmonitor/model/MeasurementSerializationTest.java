package com.example.floodmonitor.model;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilderFactory;
import java.io.StringReader;
import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Prueft, dass Measurement als JSON und als XML dieselben Felder liefert. */
class MeasurementSerializationTest {

    private static final Set<String> ERWARTETE_FELDER = Set.of(
        "stationId", "timestamp", "waterLevel", "flowRate", "rainfall",
        "temperature", "batteryLevel", "status", "warningLevel");

    private final Measurement measurement = new Measurement(
        "S1", Instant.parse("2026-10-03T08:15:00Z"), 312.5, 84.2, 4.1, 8.3, 97.5,
        StationStatus.ONLINE, WarningLevel.WARNING);

    @Test
    void jsonUndXmlEnthaltenDieselbenFelder() throws Exception {
        String json = jsonMapper().writeValueAsString(measurement);
        String xml = xmlMapper().writeValueAsString(measurement);

        Set<String> jsonFelder = feldnamen(jsonMapper().readTree(json));
        Set<String> xmlFelder = feldnamen(xmlMapper().readTree(xml));

        assertEquals(jsonFelder, xmlFelder);
        assertEquals(ERWARTETE_FELDER, jsonFelder);
    }

    @Test
    void xmlWurzelelementIstMeasurement() throws Exception {
        String xml = xmlMapper().writeValueAsString(measurement);
        assertTrue(xml.startsWith("<measurement>"), "XML beginnt nicht mit <measurement>: " + xml);
    }

    @Test
    void xmlEnthaeltKeineAttribute() throws Exception {
        String xml = xmlMapper().writeValueAsString(measurement);
        Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder()
            .parse(new InputSource(new StringReader(xml)));
        assertTrue("measurement".equals(doc.getDocumentElement().getNodeName()),
            "Wurzelelement ist nicht 'measurement'");
        assertFalse(hatAttribute(doc.getDocumentElement()), "XML enthaelt Attribute: " + xml);
    }

    /** Prueft rekursiv, ob ein Element Attribute besitzt. */
    private static boolean hatAttribute(Node node) {
        if (node.hasAttributes()) {
            return true;
        }
        NodeList children = node.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            if (hatAttribute(children.item(i))) {
                return true;
            }
        }
        return false;
    }

    @Test
    void zeitstempelAlsIso8601MitZ() throws Exception {
        String json = jsonMapper().writeValueAsString(measurement);
        String xml = xmlMapper().writeValueAsString(measurement);
        assertTrue(json.contains("\"2026-10-03T08:15:00Z\""), "JSON-Zeitstempel falsch: " + json);
        assertTrue(xml.contains("2026-10-03T08:15:00Z"), "XML-Zeitstempel falsch: " + xml);
    }

    private static ObjectMapper jsonMapper() {
        return JsonMapper.builder()
            .addModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
            .build();
    }

    private static XmlMapper xmlMapper() {
        return XmlMapper.builder()
            .addModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
            .build();
    }

    private static Set<String> feldnamen(JsonNode node) {
        return StreamSupport
            .stream(Spliterators.spliteratorUnknownSize(node.fieldNames(), Spliterator.ORDERED), false)
            .collect(Collectors.toCollection(HashSet::new));
    }
}
