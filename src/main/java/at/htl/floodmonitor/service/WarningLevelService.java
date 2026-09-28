package at.htl.floodmonitor.service;

import at.htl.floodmonitor.model.Station;
import at.htl.floodmonitor.model.WarningLevel;
import org.springframework.stereotype.Service;

/**
 * Berechnet die Hochwasserwarnstufe serverseitig anhand des Wasserstands und
 * der Schwellwerte einer Station. Die Warnstufe wird niemals vom Client
 * vorgegeben.
 *
 * <p>Regeln:</p>
 * <ul>
 *   <li>Wasserstand &lt; warningWaterLevel &rarr; NORMAL</li>
 *   <li>warningWaterLevel &le; Wasserstand &lt; criticalWaterLevel &rarr; WARNING</li>
 *   <li>Wasserstand &ge; criticalWaterLevel &rarr; CRITICAL</li>
 *   <li>keine gueltige Messung / Station &rarr; UNKNOWN</li>
 * </ul>
 */
@Service
public class WarningLevelService {

    public WarningLevel calculate(Station station, Double waterLevel) {
        if (station == null || waterLevel == null || waterLevel < 0) {
            return WarningLevel.UNKNOWN;
        }
        if (waterLevel >= station.getCriticalWaterLevel()) {
            return WarningLevel.CRITICAL;
        }
        if (waterLevel >= station.getWarningWaterLevel()) {
            return WarningLevel.WARNING;
        }
        return WarningLevel.NORMAL;
    }
}
