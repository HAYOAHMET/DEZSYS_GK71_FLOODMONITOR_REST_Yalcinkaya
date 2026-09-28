package at.htl.floodmonitor.service;

import at.htl.floodmonitor.model.Location;
import at.htl.floodmonitor.model.Measurement;
import at.htl.floodmonitor.model.Station;
import at.htl.floodmonitor.model.StationStatus;
import at.htl.floodmonitor.model.WarningLevel;
import at.htl.floodmonitor.repository.MeasurementRepository;
import at.htl.floodmonitor.repository.StationRepository;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Simuliert regelmaessig realistische, zusammenhaengende Messwerte fuer jede
 * aktive Station.
 *
 * <p>Simulationsregeln:</p>
 * <ul>
 *   <li>Wasserstaende sind niemals negativ.</li>
 *   <li>Der Akkustand liegt zwischen 0 und 100 Prozent und sinkt langsam.</li>
 *   <li>Starker Niederschlag erhoeht den Wasserstand tendenziell.</li>
 *   <li>Ein hoeherer Wasserstand fuehrt zu hoeherer Durchflussmenge.</li>
 *   <li>Der Wasserstand aendert sich sanft (Bezug zum vorherigen Messwert).</li>
 *   <li>Bei sehr niedrigem Akkustand (&lt; 15 %) wechselt der Status auf MAINTENANCE.</li>
 *   <li>Fuer deaktivierte Stationen werden keine Messungen erzeugt.</li>
 * </ul>
 */
@Service
public class SimulatorService {

    private static final Logger log = LoggerFactory.getLogger(SimulatorService.class);
    private static final int LOW_BATTERY_THRESHOLD = 15;

    private final StationRepository stationRepository;
    private final MeasurementRepository measurementRepository;
    private final WarningLevelService warningLevelService;

    private final Map<String, Measurement> lastMeasurement = new ConcurrentHashMap<>();
    private final Map<String, Integer> batteryByStation = new ConcurrentHashMap<>();
    private final Random random = new Random();

    @Value("${simulation.enabled:true}")
    private boolean simulationEnabled;

    @Value("${simulation.seed-history:30}")
    private int seedHistory;

    public SimulatorService(StationRepository stationRepository,
                            MeasurementRepository measurementRepository,
                            WarningLevelService warningLevelService) {
        this.stationRepository = stationRepository;
        this.measurementRepository = measurementRepository;
        this.warningLevelService = warningLevelService;
    }

    @PostConstruct
    public void init() {
        seedStations();
        seedHistoricalMeasurements();
        log.info("Simulator initialisiert. Simulation aktiv: {}", simulationEnabled);
    }

    /**
     * Legt die vorkonfigurierten Messstationen an (mindestens drei).
     */
    private void seedStations() {
        if (!stationRepository.findAll().isEmpty()) {
            return;
        }
        stationRepository.save(new Station("ST-001", "Donaubruecke Linz", "Donau",
                new Location(48.3069, 14.2858), 250.0, 350.0, 450.0, true));
        stationRepository.save(new Station("ST-002", "Trauneinmuendung", "Traun",
                new Location(48.2000, 14.2600), 180.0, 260.0, 340.0, true));
        stationRepository.save(new Station("ST-003", "Ennsau", "Enns",
                new Location(48.2100, 14.4700), 200.0, 300.0, 400.0, true));
        stationRepository.save(new Station("ST-004", "Steyr Wehr", "Steyr",
                new Location(48.0400, 14.4200), 150.0, 220.0, 300.0, false));
        log.info("{} Stationen vorbefuellt", stationRepository.findAll().size());
    }

    /**
     * Erzeugt deterministische historische Messwerte, damit direkt nach dem
     * Start Daten (inklusive aller Warnstufen) vorhanden sind. Der Wasserstand
     * durchlaeuft den Bereich von NORMAL ueber WARNING bis CRITICAL.
     */
    private void seedHistoricalMeasurements() {
        int count = Math.max(seedHistory, 12);
        Instant start = Instant.now().minus((long) count * 10, ChronoUnit.MINUTES);

        for (Station station : stationRepository.findAll()) {
            if (!station.isActive()) {
                continue;
            }
            Random deterministic = new Random(station.getId().hashCode());
            double range = station.getCriticalWaterLevel() + 40 - (station.getNormalWaterLevel() - 20);
            double base = station.getNormalWaterLevel() - 20;
            int battery = 100;

            for (int i = 0; i < count; i++) {
                // Sinusfoermiger Verlauf sorgt fuer alle Warnstufen und sanfte Uebergaenge
                double fraction = (Math.sin((double) i / count * Math.PI * 2 - Math.PI / 2) + 1) / 2.0;
                double waterLevel = round(base + fraction * range + (deterministic.nextDouble() - 0.5) * 5);
                waterLevel = Math.max(0, waterLevel);

                double rainfall = round(deterministic.nextDouble() * 20);
                double flowRate = round(waterLevel * 2.1 + rainfall * 1.5);
                double temperature = round(8 + deterministic.nextDouble() * 15);
                battery = Math.max(20, battery - deterministic.nextInt(2));

                Instant ts = start.plus((long) i * 10, ChronoUnit.MINUTES);
                StationStatus status = battery < LOW_BATTERY_THRESHOLD
                        ? StationStatus.MAINTENANCE : StationStatus.ONLINE;
                WarningLevel level = warningLevelService.calculate(station, waterLevel);

                Measurement m = new Measurement(station.getId(), ts, waterLevel, flowRate,
                        rainfall, temperature, battery, status, level);
                measurementRepository.add(m);
                lastMeasurement.put(station.getId(), m);
            }
            batteryByStation.put(station.getId(), battery);
        }
        log.info("Historische Messwerte erzeugt ({} pro aktiver Station)", count);
    }

    /**
     * Erzeugt periodisch neue Messwerte fuer alle aktiven Stationen.
     */
    @Scheduled(fixedRateString = "${simulation.interval:10000}",
            initialDelayString = "${simulation.interval:10000}")
    public void generateMeasurements() {
        if (!simulationEnabled) {
            return;
        }
        for (Station station : stationRepository.findAll()) {
            if (!station.isActive()) {
                continue;
            }
            Measurement previous = lastMeasurement.get(station.getId());
            Measurement next = generateNext(station, previous);
            measurementRepository.add(next);
            lastMeasurement.put(station.getId(), next);
        }
        log.debug("Neue Messwerte erzeugt");
    }

    /**
     * Erzeugt den naechsten Messwert unter Beruecksichtigung des vorherigen.
     */
    Measurement generateNext(Station station, Measurement previous) {
        double prevWater = previous != null ? previous.getWaterLevel() : station.getNormalWaterLevel();
        int prevBattery = previous != null ? previous.getBatteryLevel()
                : batteryByStation.getOrDefault(station.getId(), 100);

        double rainfall = round(Math.max(0, random.nextGaussian() * 5 + 5));
        // sanfte Aenderung: kleiner zufaelliger Anteil + Einfluss des Niederschlags
        double delta = (random.nextDouble() - 0.5) * 8 + rainfall * 0.6;
        double waterLevel = round(Math.max(0, prevWater + delta));
        double flowRate = round(waterLevel * 2.1 + rainfall * 1.5);
        double temperature = round(8 + random.nextDouble() * 15);

        int battery = Math.max(0, prevBattery - random.nextInt(2));
        batteryByStation.put(station.getId(), battery);

        StationStatus status = battery < LOW_BATTERY_THRESHOLD
                ? StationStatus.MAINTENANCE : StationStatus.ONLINE;
        WarningLevel level = warningLevelService.calculate(station, waterLevel);

        return new Measurement(station.getId(), Instant.now(), waterLevel, flowRate,
                rainfall, temperature, battery, status, level);
    }

    private double round(double value) {
        return Math.round(value * 10.0) / 10.0;
    }

    /**
     * Fuer Tests: erzeugt genau einen neuen Messwert-Zyklus, unabhaengig vom
     * enabled-Flag.
     */
    public void triggerOnce() {
        boolean prev = simulationEnabled;
        simulationEnabled = true;
        try {
            generateMeasurements();
        } finally {
            simulationEnabled = prev;
        }
    }

    /**
     * Nur fuer Tests / interne Nutzung: fuegt einen konkreten Messwert hinzu.
     */
    public Map<String, Measurement> getLastMeasurements() {
        return new HashMap<>(lastMeasurement);
    }
}
