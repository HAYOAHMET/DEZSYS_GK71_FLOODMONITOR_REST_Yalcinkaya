package at.htl.floodmonitor;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.xpath;

/**
 * Integrationstests fuer die FloodMonitor REST-API. Die Simulation ist in
 * Tests deaktiviert; die deterministischen historischen Messwerte werden beim
 * Start erzeugt, sodass die Tests nicht zufaellig fehlschlagen.
 */
@SpringBootTest
@AutoConfigureMockMvc
class FloodMonitorApiIntegrationTest {

    @Autowired
    private MockMvc mockMvc;


    // 1. Abruf aller Stationen als JSON
    @Test
    @DisplayName("1. Alle Stationen als JSON")
    void getAllStationsAsJson() throws Exception {
        mockMvc.perform(get("/api/v1/stations")
                        .accept(MediaType.APPLICATION_JSON)
                        )
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.count", greaterThan(0)))
                .andExpect(jsonPath("$.stations[0].id").exists());
    }

    // 2. Abruf aller Stationen als XML
    @Test
    @DisplayName("2. Alle Stationen als XML")
    void getAllStationsAsXml() throws Exception {
        mockMvc.perform(get("/api/v1/stations")
                        .accept(MediaType.APPLICATION_XML)
                        )
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_XML))
                .andExpect(xpath("/stations/station[1]/id").exists());
    }

    // 3. Abruf einer existierenden Station
    @Test
    @DisplayName("3. Existierende Station")
    void getExistingStation() throws Exception {
        mockMvc.perform(get("/api/v1/stations/ST-001")
                        .accept(MediaType.APPLICATION_JSON)
                        )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is("ST-001")))
                .andExpect(jsonPath("$.river", is("Donau")));
    }

    // 4. Abruf einer unbekannten Station -> 404
    @Test
    @DisplayName("4. Unbekannte Station -> 404")
    void getUnknownStation() throws Exception {
        mockMvc.perform(get("/api/v1/stations/ST-999")
                        .accept(MediaType.APPLICATION_JSON)
                        )
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status", is(404)))
                .andExpect(jsonPath("$.error", is("Not Found")))
                .andExpect(jsonPath("$.path", is("/api/v1/stations/ST-999")));
    }

    // 5. Abruf des aktuellsten Messwerts
    @Test
    @DisplayName("5. Aktuellster Messwert")
    void getLatestMeasurement() throws Exception {
        mockMvc.perform(get("/api/v1/stations/ST-001/measurements/latest")
                        .accept(MediaType.APPLICATION_JSON)
                        )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.stationId", is("ST-001")))
                .andExpect(jsonPath("$.warningLevel").exists());
    }

    // 6. Filterung nach Zeitraum
    @Test
    @DisplayName("6. Filterung nach Zeitraum")
    void filterByTimeRange() throws Exception {
        mockMvc.perform(get("/api/v1/stations/ST-001/measurements")
                        .param("from", "2020-01-01T00:00:00Z")
                        .param("to", "2100-01-01T00:00:00Z")
                        .accept(MediaType.APPLICATION_JSON)
                        )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.count", greaterThan(0)));
    }

    // 7. Ungueltiger Zeitraum (from nach to) -> 400
    @Test
    @DisplayName("7. Ungueltiger Zeitraum -> 400")
    void invalidTimeRange() throws Exception {
        mockMvc.perform(get("/api/v1/stations/ST-001/measurements")
                        .param("from", "2100-01-01T00:00:00Z")
                        .param("to", "2020-01-01T00:00:00Z")
                        .accept(MediaType.APPLICATION_JSON)
                        )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status", is(400)));
    }

    // 8. Warnstufe NORMAL
    @Test
    @DisplayName("8. Warnstufe NORMAL")
    void warningLevelNormal() throws Exception {
        mockMvc.perform(get("/api/v1/stations/ST-001/measurements")
                        .param("warningLevel", "NORMAL")
                        .accept(MediaType.APPLICATION_JSON)
                        )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.count", greaterThan(0)))
                .andExpect(jsonPath("$.measurements[*].warningLevel", everyItem(is("NORMAL"))));
    }

    // 9. Warnstufe WARNING
    @Test
    @DisplayName("9. Warnstufe WARNING")
    void warningLevelWarning() throws Exception {
        mockMvc.perform(get("/api/v1/stations/ST-001/measurements")
                        .param("warningLevel", "WARNING")
                        .accept(MediaType.APPLICATION_JSON)
                        )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.count", greaterThan(0)))
                .andExpect(jsonPath("$.measurements[*].warningLevel", everyItem(is("WARNING"))));
    }

    // 10. Warnstufe CRITICAL
    @Test
    @DisplayName("10. Warnstufe CRITICAL")
    void warningLevelCritical() throws Exception {
        mockMvc.perform(get("/api/v1/stations/ST-001/measurements")
                        .param("warningLevel", "CRITICAL")
                        .accept(MediaType.APPLICATION_JSON)
                        )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.count", greaterThan(0)))
                .andExpect(jsonPath("$.measurements[*].warningLevel", everyItem(is("CRITICAL"))));
    }

    // 11. Nicht unterstuetztes Format -> 406
    @Test
    @DisplayName("11. Nicht unterstuetztes Format -> 406")
    void unsupportedFormat() throws Exception {
        mockMvc.perform(get("/api/v1/stations")
                        .accept(MediaType.APPLICATION_PDF)
                        )
                .andExpect(status().isNotAcceptable());
    }

    // 12. Maximales Limit -> 400 bei Ueberschreitung
    @Test
    @DisplayName("12a. Zu grosses Limit -> 400")
    void maximumLimitExceeded() throws Exception {
        mockMvc.perform(get("/api/v1/stations/ST-001/measurements")
                        .param("limit", "1001")
                        .accept(MediaType.APPLICATION_JSON)
                        )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status", is(400)));
    }

    @Test
    @DisplayName("12b. Gueltiges Limit begrenzt Ergebnismenge")
    void validLimitCapsResults() throws Exception {
        mockMvc.perform(get("/api/v1/stations/ST-001/measurements")
                        .param("limit", "5")
                        .accept(MediaType.APPLICATION_JSON)
                        )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.count", is(5)));
    }

    // 13. Negatives Limit -> 400
    @Test
    @DisplayName("13. Negatives Limit -> 400")
    void negativeLimit() throws Exception {
        mockMvc.perform(get("/api/v1/stations/ST-001/measurements")
                        .param("limit", "-5")
                        .accept(MediaType.APPLICATION_JSON)
                        )
                .andExpect(status().isBadRequest());
    }

    // 14. Ungueltiger Warnstufen-Wert -> 400
    @Test
    @DisplayName("14. Ungueltige Warnstufe -> 400")
    void invalidWarningLevelValue() throws Exception {
        mockMvc.perform(get("/api/v1/stations/ST-001/measurements")
                        .param("warningLevel", "FLOOD")
                        .accept(MediaType.APPLICATION_JSON)
                        )
                .andExpect(status().isBadRequest());
    }

    // 15. Ungueltiges Datumsformat -> 400
    @Test
    @DisplayName("15. Ungueltiges Datumsformat -> 400")
    void invalidDateFormat() throws Exception {
        mockMvc.perform(get("/api/v1/stations/ST-001/measurements")
                        .param("from", "nicht-ein-datum")
                        .accept(MediaType.APPLICATION_JSON)
                        )
                .andExpect(status().isBadRequest());
    }

    // 16. Statistik
    @Test
    @DisplayName("16. Statistik einer Station")
    void getStatistics() throws Exception {
        mockMvc.perform(get("/api/v1/stations/ST-001/statistics")
                        .accept(MediaType.APPLICATION_JSON)
                        )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.stationId", is("ST-001")))
                .andExpect(jsonPath("$.measurementCount", greaterThan(0)))
                .andExpect(jsonPath("$.maxWaterLevel", greaterThan(0.0)));
    }

    // 17. Warnungen
    @Test
    @DisplayName("17. Aktuelle Warnungen")
    void getAlerts() throws Exception {
        mockMvc.perform(get("/api/v1/alerts")
                        .accept(MediaType.APPLICATION_JSON)
                        )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.count").exists());
    }



    // 20. Station anlegen -> 201
    @Test
    @DisplayName("20. POST -> 201")
    void createStationAsAdmin() throws Exception {
        String body = "{\"id\":\"ST-201\",\"name\":\"Neue Station\",\"river\":\"Ill\","
                + "\"location\":{\"latitude\":47.5,\"longitude\":9.7},"
                + "\"normalWaterLevel\":100,\"warningWaterLevel\":200,\"criticalWaterLevel\":300}";
        mockMvc.perform(post("/api/v1/stations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body)
                        .accept(MediaType.APPLICATION_JSON)
                        )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is("ST-201")));
    }

    // 21. Station mit ungueltigen Schwellwerten -> 400
    @Test
    @DisplayName("21. POST mit ungueltigen Schwellwerten -> 400")
    void createStationInvalidThresholds() throws Exception {
        String body = "{\"id\":\"ST-202\",\"name\":\"Fehlerhaft\",\"river\":\"X\","
                + "\"location\":{\"latitude\":47.5,\"longitude\":9.7},"
                + "\"normalWaterLevel\":100,\"warningWaterLevel\":300,\"criticalWaterLevel\":200}";
        mockMvc.perform(post("/api/v1/stations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body)
                        .accept(MediaType.APPLICATION_JSON)
                        )
                .andExpect(status().isBadRequest());
    }

    // 22. Station aktualisieren (PATCH) und loeschen (DELETE)
    @Test
    @DisplayName("22. PATCH und DELETE")
    void patchAndDeleteStation() throws Exception {
        String body = "{\"id\":\"ST-300\",\"name\":\"Temp\",\"river\":\"Mur\","
                + "\"location\":{\"latitude\":47.0,\"longitude\":15.4},"
                + "\"normalWaterLevel\":100,\"warningWaterLevel\":200,\"criticalWaterLevel\":300}";
        mockMvc.perform(post("/api/v1/stations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body)
                        )
                .andExpect(status().isCreated());

        mockMvc.perform(patch("/api/v1/stations/ST-300")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"active\":false}")
                        .accept(MediaType.APPLICATION_JSON)
                        )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.active", is(false)));

        mockMvc.perform(delete("/api/v1/stations/ST-300")
                        )
                .andExpect(status().isNoContent());
    }

    // 23. Fehler auch als XML abrufbar
    @Test
    @DisplayName("23. Fehler als XML")
    void errorAsXml() throws Exception {
        mockMvc.perform(get("/api/v1/stations/ST-999")
                        .accept(MediaType.APPLICATION_XML)
                        )
                .andExpect(status().isNotFound())
                .andExpect(xpath("/error/status").string("404"));
    }
}
