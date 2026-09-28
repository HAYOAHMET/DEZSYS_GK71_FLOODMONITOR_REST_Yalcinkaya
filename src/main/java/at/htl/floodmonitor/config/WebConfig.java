package at.htl.floodmonitor.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.servlet.config.annotation.ContentNegotiationConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Konfiguriert die Content Negotiation:
 * <ul>
 *   <li>Standardformat ist JSON (kein Accept-Header &rarr; JSON).</li>
 *   <li>XML wird ueber den Accept-Header (application/xml) unterstuetzt.</li>
 *   <li>Optionaler Query-Parameter {@code ?format=json|xml}.</li>
 *   <li>Nicht unterstuetzte Formate fuehren zu 406 Not Acceptable.</li>
 * </ul>
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void configureContentNegotiation(ContentNegotiationConfigurer configurer) {
        configurer
                .favorParameter(true)
                .parameterName("format")
                .ignoreAcceptHeader(false)
                .defaultContentType(MediaType.APPLICATION_JSON)
                .mediaType("json", MediaType.APPLICATION_JSON)
                .mediaType("xml", MediaType.APPLICATION_XML);
    }
}
