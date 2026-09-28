package at.htl.floodmonitor.service;

import at.htl.floodmonitor.dto.StationCreateRequest;
import at.htl.floodmonitor.dto.StationUpdateRequest;
import at.htl.floodmonitor.exception.InvalidRequestException;
import at.htl.floodmonitor.exception.StationConflictException;
import at.htl.floodmonitor.exception.StationNotFoundException;
import at.htl.floodmonitor.model.Measurement;
import at.htl.floodmonitor.model.Station;
import at.htl.floodmonitor.model.StationStatistics;
import at.htl.floodmonitor.model.WarningLevel;
import at.htl.floodmonitor.repository.MeasurementRepository;
import at.htl.floodmonitor.repository.StationRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Geschaeftslogik fuer Stationen: Abfrage, Filterung, Verwaltung (CRUD)
 * und statistische Auswertung.
 */
@Service
public class StationService {

    private final StationRepository stationRepository;
    private final MeasurementRepository measurementRepository;
    private final WarningLevelService warningLevelService;

    public StationService(StationRepository stationRepository,
                          MeasurementRepository measurementRepository,
                          WarningLevelService warningLevelService) {
        this.stationRepository = stationRepository;
        this.measurementRepository = measurementRepository;
        this.warningLevelService = warningLevelService;
    }

    /**
     * Liefert alle Stationen, optional gefiltert nach Fluss, aktiv-Status und
     * aktueller Warnstufe.
     */
    public List<Station> findStations(String river, Boolean active, WarningLevel warningLevel) {
        return stationRepository.findAll().stream()
                .filter(s -> river == null || s.getRiver().equalsIgnoreCase(river))
                .filter(s -> active == null || s.isActive() == active)
                .filter(s -> warningLevel == null || currentWarningLevel(s) == warningLevel)
                .sorted((a, b) -> a.getId().compareTo(b.getId()))
                .collect(Collectors.toList());
    }

    public Station getStation(String stationId) {
        return stationRepository.findById(stationId)
                .orElseThrow(() -> new StationNotFoundException(stationId));
    }

    /**
     * Aktuelle Warnstufe einer Station = Warnstufe der aktuellsten Messung,
     * andernfalls UNKNOWN.
     */
    public WarningLevel currentWarningLevel(Station station) {
        return measurementRepository.findLatest(station.getId())
                .map(Measurement::getWarningLevel)
                .orElse(WarningLevel.UNKNOWN);
    }

    // ----------------------------------------------------------
    //  Verwaltung (Erweiterung)
    // ----------------------------------------------------------

    public Station createStation(StationCreateRequest request) {
        if (stationRepository.existsById(request.getId())) {
            throw new StationConflictException(request.getId());
        }
        validateThresholds(request.getNormalWaterLevel(),
                request.getWarningWaterLevel(),
                request.getCriticalWaterLevel());

        Station station = new Station(
                request.getId(),
                request.getName(),
                request.getRiver(),
                request.getLocation(),
                request.getNormalWaterLevel(),
                request.getWarningWaterLevel(),
                request.getCriticalWaterLevel(),
                request.getActive() == null || request.getActive()
        );
        return stationRepository.save(station);
    }

    public Station updateStation(String stationId, StationUpdateRequest request) {
        Station station = getStation(stationId);

        if (request.getName() != null) {
            station.setName(request.getName());
        }
        if (request.getRiver() != null) {
            station.setRiver(request.getRiver());
        }
        if (request.getLocation() != null) {
            station.setLocation(request.getLocation());
        }
        if (request.getNormalWaterLevel() != null) {
            station.setNormalWaterLevel(request.getNormalWaterLevel());
        }
        if (request.getWarningWaterLevel() != null) {
            station.setWarningWaterLevel(request.getWarningWaterLevel());
        }
        if (request.getCriticalWaterLevel() != null) {
            station.setCriticalWaterLevel(request.getCriticalWaterLevel());
        }
        if (request.getActive() != null) {
            station.setActive(request.getActive());
        }

        validateThresholds(station.getNormalWaterLevel(),
                station.getWarningWaterLevel(),
                station.getCriticalWaterLevel());

        return stationRepository.save(station);
    }

    public void deleteStation(String stationId) {
        if (!stationRepository.existsById(stationId)) {
            throw new StationNotFoundException(stationId);
        }
        stationRepository.deleteById(stationId);
        measurementRepository.deleteByStationId(stationId);
    }

    private void validateThresholds(Double normal, Double warning, Double critical) {
        if (normal == null || warning == null || critical == null) {
            throw new InvalidRequestException("normal-, warning- und criticalWaterLevel muessen gesetzt sein");
        }
        if (!(critical > warning)) {
            throw new InvalidRequestException("criticalWaterLevel muss groesser als warningWaterLevel sein");
        }
        if (!(warning >= normal)) {
            throw new InvalidRequestException("warningWaterLevel muss groesser oder gleich normalWaterLevel sein");
        }
    }

    // ----------------------------------------------------------
    //  Statistik
    // ----------------------------------------------------------

    public StationStatistics getStatistics(String stationId) {
        Station station = getStation(stationId);
        List<Measurement> measurements = measurementRepository.findByStationId(station.getId());

        if (measurements.isEmpty()) {
            return new StationStatistics(stationId, 0, 0, 0, 0, 0, 0, 0, 0);
        }

        double min = measurements.stream().mapToDouble(Measurement::getWaterLevel).min().orElse(0);
        double max = measurements.stream().mapToDouble(Measurement::getWaterLevel).max().orElse(0);
        double avgWater = measurements.stream().mapToDouble(Measurement::getWaterLevel).average().orElse(0);
        double avgFlow = measurements.stream().mapToDouble(Measurement::getFlowRate).average().orElse(0);
        double totalRain = measurements.stream().mapToDouble(Measurement::getRainfall).sum();
        long count = measurements.size();
        long warnings = measurements.stream()
                .filter(m -> m.getWarningLevel() == WarningLevel.WARNING
                        || m.getWarningLevel() == WarningLevel.CRITICAL)
                .count();
        long critical = measurements.stream()
                .filter(m -> m.getWarningLevel() == WarningLevel.CRITICAL)
                .count();

        return new StationStatistics(
                stationId,
                round(min), round(max), round(avgWater), round(avgFlow), round(totalRain),
                count, warnings, critical
        );
    }

    private double round(double value) {
        return Math.round(value * 10.0) / 10.0;
    }

    public Optional<Station> findByIdOptional(String stationId) {
        return stationRepository.findById(stationId);
    }
}
