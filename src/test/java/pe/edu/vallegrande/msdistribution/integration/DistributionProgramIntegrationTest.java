package pe.edu.vallegrande.msdistribution.integration;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.TestPropertySource;
import pe.edu.vallegrande.msdistribution.application.services.DistributionProgramService;
import pe.edu.vallegrande.msdistribution.domain.models.DistributionProgram;
import pe.edu.vallegrande.msdistribution.infrastructure.dto.request.DistributionProgramCreateRequest;
import pe.edu.vallegrande.msdistribution.infrastructure.repository.DistributionProgramRepository;
import pe.edu.vallegrande.msdistribution.infrastructure.service.ExternalServiceClient;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.LocalDate;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

/**
 * Pruebas de integración para DistributionProgramService
 * Usa MongoDB Embedded automáticamente configurado por Spring Boot
 */
@SpringBootTest(classes = {
    pe.edu.vallegrande.msdistribution.VgMsDistribution.class
})
@TestPropertySource(properties = {
    "de.flapdoodle.mongodb.embedded.version=5.0.5",
    "spring.data.mongodb.auto-index-creation=true"
})
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class DistributionProgramIntegrationTest {

    @Autowired
    private DistributionProgramService distributionProgramService;

    @Autowired
    private DistributionProgramRepository distributionProgramRepository;

    @MockBean
    private ExternalServiceClient externalServiceClient;

    @BeforeEach
    void setUp() {
        // Mock external service calls
        pe.edu.vallegrande.msdistribution.infrastructure.client.dto.ExternalOrganization org = 
            new pe.edu.vallegrande.msdistribution.infrastructure.client.dto.ExternalOrganization();
        org.setOrganizationId("org-123");
        org.setOrganizationName("Test Organization");
        
        when(externalServiceClient.getOrganizationById("org-123"))
            .thenReturn(Mono.just(org));
        
        // Clean database before each test
        distributionProgramRepository.deleteAll().block();
    }

    // ==================== CREATE TESTS ====================

    @Test
    @Order(1)
    @DisplayName("IT-01: Crear programa de distribución exitosamente")
    void createDistributionProgram_ShouldCreateProgram_WhenValidRequest() {
        System.out.println("\n================= INICIO TEST: IT-01 =================");
        System.out.println("Descripción: Crear programa de distribución exitosamente");
        // Arrange
        DistributionProgramCreateRequest request = DistributionProgramCreateRequest.builder()
                .organizationId("org-123")
                // programCode se genera automáticamente, no se incluye en la solicitud
                .scheduleId("schedule-1")
                .routeId("route-1")
                .zoneId("zone-1")
                .streetId("street-1")
                .programDate(LocalDate.of(2024, 1, 15))
                .plannedStartTime("08:00")
                .plannedEndTime("12:00")
                .responsibleUserId("user-1")
                .observations("Programa de prueba")
                .build();

        System.out.println("➡️ Datos de entrada:");
        System.out.println("   Organización: " + request.getOrganizationId());
        System.out.println("   Ruta: " + request.getRouteId());
        System.out.println("   Fecha: " + request.getProgramDate());
        System.out.println("   Horario: " + request.getPlannedStartTime() + " - " + request.getPlannedEndTime());
        System.out.println("   Responsable: " + request.getResponsibleUserId());

        // Act & Assert
        StepVerifier.create(distributionProgramService.save(request))
                .assertNext(program -> {
                System.out.println("\n✅ Programa creado correctamente:");
                System.out.println("   ID generado: " + program.getId());
                System.out.println("   Código: " + program.getProgramCode());
                System.out.println("   Estado: " + program.getStatus());
                System.out.println("   Observaciones: " + program.getObservations());
                System.out.println("   Fecha creación: " + program.getCreatedAt());

                assertNotNull(program.getId());
                assertEquals("org-123", program.getOrganizationId());
                // El código se genera automáticamente, debería ser PRG001
                assertEquals("PRG001", program.getProgramCode());
                assertEquals("schedule-1", program.getScheduleId());
                assertEquals("route-1", program.getRouteId());
                assertEquals("zone-1", program.getZoneId());
                assertEquals("street-1", program.getStreetId());
                assertEquals(LocalDate.of(2024, 1, 15), program.getProgramDate());
                assertEquals("08:00", program.getPlannedStartTime());
                assertEquals("12:00", program.getPlannedEndTime());
                assertEquals("PLANNED", program.getStatus());
                assertEquals("user-1", program.getResponsibleUserId());
                assertEquals("Programa de prueba", program.getObservations());
                assertNotNull(program.getCreatedAt());
            })
            .verifyComplete();

        System.out.println("================= FIN TEST: IT-01 =================\n");
    }

    // ==================== READ TESTS ====================

    @Test
    @Order(2)
    @DisplayName("IT-02: Obtener programa por ID exitosamente")
    void getDistributionProgramById_ShouldReturnProgram_WhenExists() {
        System.out.println("\n================= INICIO TEST: IT-02 =================");
        System.out.println("Descripción: Obtener programa de distribución por ID exitosamente");
        // Arrange
        DistributionProgram program = DistributionProgram.builder()
                .organizationId("org-123")
                .programCode("PRG001")
                .scheduleId("schedule-1")
                .routeId("route-1")
                .zoneId("zone-1")
                .streetId("street-1")
                .programDate(LocalDate.of(2024, 1, 15))
                .plannedStartTime("08:00")
                .plannedEndTime("12:00")
                .status("PLANNED")
                .responsibleUserId("user-1")
                .observations("Programa de prueba")
                .createdAt(Instant.now())
                .build();
        
        DistributionProgram saved = distributionProgramRepository.save(program).block();
        System.out.println("➡️ Programa guardado:");
        System.out.println("   ID: " + saved.getId());
        System.out.println("   Código: " + saved.getProgramCode());
        System.out.println("   Estado: " + saved.getStatus());

        // Act & Assert
        StepVerifier.create(distributionProgramService.getById(saved.getId()))
                .assertNext(result -> {
                System.out.println("\n✅ Programa obtenido correctamente:");
                System.out.println("   ID: " + result.getId());
                System.out.println("   Código: " + result.getProgramCode());
                System.out.println("   Ruta: " + result.getRouteId());
                System.out.println("   Estado: " + result.getStatus());

                assertEquals(saved.getId(), result.getId());
                assertEquals("PRG001", result.getProgramCode());
                assertEquals("schedule-1", result.getScheduleId());
                assertEquals("route-1", result.getRouteId());
            })
            .verifyComplete();

        System.out.println("================= FIN TEST: IT-02 =================\n");
    }

    @Test
    @Order(3)
    @DisplayName("IT-03: Listar todos los programas")
    void getAllDistributionPrograms_ShouldReturnAllPrograms() {
        System.out.println("\n================= INICIO TEST: IT-03 =================");
        System.out.println("Descripción: Listar todos los programas de distribución");
        // Arrange - Crear múltiples programas
        DistributionProgram program1 = DistributionProgram.builder()
                .organizationId("org-123")
                .programCode("PRG001")
                .scheduleId("schedule-1")
                .routeId("route-1")
                .zoneId("zone-1")
                .streetId("street-1")
                .programDate(LocalDate.of(2024, 1, 15))
                .plannedStartTime("08:00")
                .plannedEndTime("12:00")
                .status("PLANNED")
                .responsibleUserId("user-1")
                .observations("Programa 1")
                .createdAt(Instant.now())
                .build();

        DistributionProgram program2 = DistributionProgram.builder()
                .organizationId("org-123")
                .programCode("PRG002")
                .scheduleId("schedule-2")
                .routeId("route-2")
                .zoneId("zone-2")
                .streetId("street-2")
                .programDate(LocalDate.of(2024, 1, 16))
                .plannedStartTime("09:00")
                .plannedEndTime("13:00")
                .status("ACTIVE")
                .responsibleUserId("user-2")
                .observations("Programa 2")
                .createdAt(Instant.now())
                .build();

        distributionProgramRepository.save(program1).block();
        distributionProgramRepository.save(program2).block();
        System.out.println("➡️ Se han creado 2 programas de prueba:");
        System.out.println("   - " + program1.getProgramCode() + " (" + program1.getStatus() + ")");
        System.out.println("   - " + program2.getProgramCode() + " (" + program2.getStatus() + ")");

        // Act & Assert
        StepVerifier.create(distributionProgramService.getAll())
                .assertNext(p -> {
                    System.out.println("   📦 Programa encontrado: " + p.getProgramCode());
                    assertEquals("PRG001", p.getProgramCode());
                })
                .assertNext(p -> {
                    System.out.println("   📦 Programa encontrado: " + p.getProgramCode());
                    assertEquals("PRG002", p.getProgramCode());
                })
                .verifyComplete();

        System.out.println("✅ Se listaron correctamente todos los programas");
        System.out.println("================= FIN TEST: IT-03 =================\n");
    }

    // ==================== UPDATE TESTS ====================

    @Test
    @Order(4)
    @DisplayName("IT-04: Actualizar programa exitosamente")
    void updateDistributionProgram_ShouldUpdateFields_WhenValid() {
        System.out.println("\n================= INICIO TEST: IT-04 =================");
        System.out.println("Descripción: Actualizar un programa de distribución exitosamente");
        // Arrange - Crear programa inicial
        DistributionProgram existing = DistributionProgram.builder()
                .organizationId("org-123")
                .programCode("PRG001")
                .scheduleId("schedule-1")
                .routeId("route-1")
                .zoneId("zone-1")
                .streetId("street-1")
                .programDate(LocalDate.of(2024, 1, 15))
                .plannedStartTime("08:00")
                .plannedEndTime("12:00")
                .status("PLANNED")
                .responsibleUserId("user-1")
                .observations("Programa original")
                .createdAt(Instant.now())
                .build();

        DistributionProgram saved = distributionProgramRepository.save(existing).block();
        System.out.println("➡️ Programa original guardado:");
        System.out.println("   ID: " + saved.getId());
        System.out.println("   Código: " + saved.getProgramCode());
        System.out.println("   Ruta: " + saved.getRouteId());
        System.out.println("   Horario: " + saved.getPlannedStartTime() + " - " + saved.getPlannedEndTime());
        System.out.println("   Observaciones: " + saved.getObservations());

        DistributionProgramCreateRequest update = DistributionProgramCreateRequest.builder()
                .organizationId("org-123")
                // programCode no se actualiza, se mantiene el generado automáticamente
                .scheduleId("schedule-2")
                .routeId("route-2")
                .zoneId("zone-2")
                .streetId("street-2")
                .programDate(LocalDate.of(2024, 1, 20))
                .plannedStartTime("10:00")
                .plannedEndTime("14:00")
                .responsibleUserId("user-2")
                .observations("Programa actualizado")
                .build();

        System.out.println("\n🛠️ Iniciando actualización del programa...");
        System.out.println("   Nueva ruta: " + update.getRouteId());
        System.out.println("   Nuevo horario: " + update.getPlannedStartTime() + " - " + update.getPlannedEndTime());

        // Act & Assert
        StepVerifier.create(distributionProgramService.update(saved.getId(), update))
                .assertNext(updated -> {
                System.out.println("\n✅ Programa actualizado exitosamente:");
                System.out.println("   ID: " + updated.getId());
                System.out.println("   Código (no cambiado): " + updated.getProgramCode());
                System.out.println("   Nueva ruta: " + updated.getRouteId());
                System.out.println("   Nueva zona: " + updated.getZoneId());
                System.out.println("   Nueva calle: " + updated.getStreetId());
                System.out.println("   Nueva fecha: " + updated.getProgramDate());
                System.out.println("   Nuevo horario: " + updated.getPlannedStartTime() + " - " + updated.getPlannedEndTime());
                System.out.println("   Responsable: " + updated.getResponsibleUserId());
                System.out.println("   Observaciones: " + updated.getObservations());

                assertEquals("PRG001", updated.getProgramCode()); // El código no cambia
                assertEquals("schedule-2", updated.getScheduleId());
                assertEquals("route-2", updated.getRouteId());
                assertEquals("zone-2", updated.getZoneId());
                assertEquals("street-2", updated.getStreetId());
                assertEquals(LocalDate.of(2024, 1, 20), updated.getProgramDate());
                assertEquals("10:00", updated.getPlannedStartTime());
                assertEquals("14:00", updated.getPlannedEndTime());
                assertEquals("user-2", updated.getResponsibleUserId());
                assertEquals("Programa actualizado", updated.getObservations());
            })
            .verifyComplete();

        System.out.println("================= FIN TEST: IT-04 =================\n");
    }

    // ==================== ACTIVATE/DEACTIVATE TESTS ====================

    @Test
    @Order(5)
    @DisplayName("IT-05: Activar programa")
    void activateDistributionProgram_ShouldChangeStatus_WhenInactive() {
        System.out.println("\n=== [IT-05: Activar programa] ===");
        System.out.println("→ Preparando datos de prueba...");
        // Arrange
        DistributionProgram program = DistributionProgram.builder()
                .organizationId("org-123")
                .programCode("PRG001")
                .scheduleId("schedule-1")
                .routeId("route-1")
                .zoneId("zone-1")
                .streetId("street-1")
                .programDate(LocalDate.of(2024, 1, 15))
                .plannedStartTime("08:00")
                .plannedEndTime("12:00")
                .status("PLANNED")
                .responsibleUserId("user-1")
                .observations("Programa para activar")
                .createdAt(Instant.now())
                .build();

        DistributionProgram saved = distributionProgramRepository.save(program).block();
        System.out.println("✔ Programa guardado con ID: " + saved.getId());

        // Act & Assert
        System.out.println("→ Ejecutando activación...");
        StepVerifier.create(distributionProgramService.activate(saved.getId()))
                .assertNext(activated -> {
                        System.out.println("✔ Estado anterior: " + saved.getStatus());
                        System.out.println("✔ Estado actual: " + activated.getStatus());
                        assertEquals("ACTIVE", activated.getStatus(), "El estado debería cambiar a ACTIVE");
                        System.out.println("✅ Activación completada correctamente.");
                })
                .verifyComplete();

        System.out.println("=== [FIN IT-05] ===\n");
    }

    @Test
    @Order(6)
    @DisplayName("IT-06: Desactivar programa")
    void deactivateDistributionProgram_ShouldChangeStatus_WhenActive() {
        System.out.println("\n=== [IT-06: Desactivar programa] ===");
        System.out.println("→ Preparando datos de prueba...");
        // Arrange
        DistributionProgram program = DistributionProgram.builder()
                .organizationId("org-123")
                .programCode("PRG001")
                .scheduleId("schedule-1")
                .routeId("route-1")
                .zoneId("zone-1")
                .streetId("street-1")
                .programDate(LocalDate.of(2024, 1, 15))
                .plannedStartTime("08:00")
                .plannedEndTime("12:00")
                .status("ACTIVE")
                .responsibleUserId("user-1")
                .observations("Programa para desactivar")
                .createdAt(Instant.now())
                .build();

        DistributionProgram saved = distributionProgramRepository.save(program).block();
        System.out.println("✔ Programa guardado con ID: " + saved.getId());

        // Act & Assert
        System.out.println("→ Ejecutando desactivación...");
        StepVerifier.create(distributionProgramService.desactivate(saved.getId()))
                .assertNext(deactivated -> {
                        System.out.println("✔ Estado anterior: " + saved.getStatus());
                        System.out.println("✔ Estado actual: " + deactivated.getStatus());
                        assertEquals("INACTIVE", deactivated.getStatus(), "El estado debería cambiar a INACTIVE");
                        System.out.println("✅ Desactivación completada correctamente.");
                })
                .verifyComplete();

        System.out.println("=== [FIN IT-06] ===\n");
    }

    // ==================== DELETE TESTS ====================

    @Test
    @Order(7)
    @DisplayName("IT-07: Eliminar programa exitosamente")
    void deleteDistributionProgram_ShouldRemove_WhenExists() {
        // Arrange
        DistributionProgram program = DistributionProgram.builder()
                .organizationId("org-123")
                .programCode("PRG001")
                .scheduleId("schedule-1")
                .routeId("route-1")
                .zoneId("zone-1")
                .streetId("street-1")
                .programDate(LocalDate.of(2024, 1, 15))
                .plannedStartTime("08:00")
                .plannedEndTime("12:00")
                .status("ACTIVE")
                .responsibleUserId("user-1")
                .observations("Programa para eliminar")
                .createdAt(Instant.now())
                .build();

        DistributionProgram saved = distributionProgramRepository.save(program).block();
        System.out.println("✔ Programa creado con ID: " + saved.getId());
        System.out.println("✔ Estado inicial: " + saved.getStatus());

        // Act
        System.out.println("→ Ejecutando eliminación del programa...");
        StepVerifier.create(distributionProgramService.delete(saved.getId()))
                .verifyComplete();
        System.out.println("✅ Eliminación completada correctamente en el servicio.");

        // Assert - Verificar que no existe
        System.out.println("→ Verificando que el programa fue eliminado del repositorio...");
        StepVerifier.create(distributionProgramRepository.findById(saved.getId()))
                .expectNextCount(0)
                .verifyComplete();
        System.out.println("✔ Verificación exitosa: el programa ya no existe en la base de datos.");

        System.out.println("=== [FIN IT-07] ===\n");
    }

}