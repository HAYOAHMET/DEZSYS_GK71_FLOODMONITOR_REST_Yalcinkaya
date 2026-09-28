package at.htl.floodmonitor.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * OpenAPI-Dokumentation (erreichbar unter /swagger-ui.html).
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI floodMonitorOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("FloodMonitor REST API")
                        .description("Hochwasser-Fruehwarnsystem (DEZSYS GK71). "
                                + "REST-Schnittstelle fuer Stationen, Messwerte, Statistiken und Warnungen.")
                        .version("1.0.0")
                        .contact(new Contact().name("DEZSYS GK71"))
                        .license(new License().name("MIT")));
    }
}
