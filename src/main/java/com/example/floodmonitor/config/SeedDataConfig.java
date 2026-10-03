package com.example.floodmonitor.config;

import com.example.floodmonitor.model.Location;
import com.example.floodmonitor.model.Station;
import com.example.floodmonitor.repository.StationRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Seed-Daten: legt die vier Startstationen beim Application-Start an. */
@Configuration
public class SeedDataConfig {

    @Bean
    CommandLineRunner stationSeedData(StationRepository stationRepository) {
        return args -> {
            stationRepository.save(new Station("ST-001", "Wien Nussdorf", "Donau",
                new Location(48.2489, 16.3667), 250.0, 300.0, 450.0, true));
            stationRepository.save(new Station("ST-002", "Krems", "Donau",
                new Location(48.4093, 15.6000), 220.0, 350.0, 500.0, true));
            stationRepository.save(new Station("ST-003", "Innsbruck", "Inn",
                new Location(47.2692, 11.4041), 150.0, 280.0, 400.0, true));
            stationRepository.save(new Station("ST-004", "Linz Traun", "Traun",
                new Location(48.3069, 14.2858), 120.0, 250.0, 350.0, false));
        };
    }
}
