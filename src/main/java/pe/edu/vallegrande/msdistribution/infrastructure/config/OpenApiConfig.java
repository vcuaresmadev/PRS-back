package pe.edu.vallegrande.msdistribution.infrastructure.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Value("${app.name:Microservicio de Distribución}")
    private String appName;

    @Value("${app.description:Microservicio para gestión de Distribución del Sistema JASS Digital}")
    private String appDescription;

    @Value("${app.version:2.0.0}")
    private String appVersion;

    @Value("${app.organization:Valle Grande}")
    private String organization;

    @Value("${server.servlet.context-path:/jass/ms-distribution}")
    private String contextPath;

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title(appName)
                        .description(appDescription)
                        .version(appVersion)
                        .contact(new Contact()
                                .name(organization)
                                .email("soporte@vallegrande.edu.pe"))
                        .license(new License()
                                .name("MIT License")
                                .url("https://opensource.org/licenses/MIT")))
                .servers(List.of(
                        new Server()
                                .url("https://lab.vallegrande.edu.pe" + contextPath)
                                .description("Servidor de Producción"),
                        new Server()
                                .url("http://localhost:8086" + contextPath)
                                .description("Servidor de Desarrollo")
                ));
    }
}