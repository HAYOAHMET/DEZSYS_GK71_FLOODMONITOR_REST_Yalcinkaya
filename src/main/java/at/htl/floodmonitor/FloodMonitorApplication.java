package at.htl.floodmonitor;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Einstiegspunkt des Hochwasser-Fruehwarnsystems (DEZSYS GK71).
 */
@SpringBootApplication
@EnableScheduling
public class FloodMonitorApplication {

    public static void main(String[] args) {
        SpringApplication.run(FloodMonitorApplication.class, args);
    }
}
