package pe.edu.vallegrande.msdistribution.integration;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.TestPropertySource;
import pe.edu.vallegrande.msdistribution.application.services.DistributionRouteService;
import pe.edu.vallegrande.msdistribution.domain.enums.Constants;
import pe.edu.vallegrande.msdistribution.domain.models.DistributionRoute;
import pe.edu.vallegrande.msdistribution.infrastructure.dto.request.DistributionRouteCreateRequest;
import pe.edu.vallegrande.msdistribution.infrastructure.repository.DistributionRouteRepository;
import pe.edu.vallegrande.msdistribution.infrastructure.service.ExternalServiceClient;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

/**
 * Pruebas de integración para DistributionRouteService
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
public class DistributionRouteIntegrationTest {

    @Autowired
    private DistributionRouteService distributionRouteService;

    @Autowired
    private DistributionRouteRepository distributionRouteRepository;

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
        distributionRouteRepository.deleteAll().block();
    }

    // ==================== CREATE TESTS ====================

    @Test
    @Order(1)
    @DisplayName("IT-01: Crear ruta de distribución exitosamente con código RUT001")
    void createDistributionRoute_ShouldGenerateRUT001_WhenNoRoutesExist() {
        System.out.println("🚀 Iniciando IT-01: Crear ruta de distribución con código RUT001...");
        // Arrange
        DistributionRouteCreateRequest request = new DistributionRouteCreateRequest();
        request.setOrganizationId("org-123");
        request.setRouteName("Ruta Principal");
        request.setZones(Arrays.asList(
            DistributionRouteCreateRequest.ZoneEntry.builder()
                .zoneId("zone-1")
                .order(1)
                .estimatedDuration(2)
                .build(),
            DistributionRouteCreateRequest.ZoneEntry.builder()
                .zoneId("zone-2")
                .order(2)
                .estimatedDuration(3)
                .build()
        ));
        request.setTotalEstimatedDuration(5);
        request.setResponsibleUserId("user-1");

        // Act & Assert
        StepVerifier.create(distributionRouteService.save(request))
                .assertNext(route -> {
                System.out.println("✅ Ruta creada con ID: " + route.getId());
                System.out.println("➡️ Código generado: " + route.getRouteCode());
                System.out.println("📦 Organización: " + route.getOrganizationId());
                System.out.println("👤 Responsable: " + route.getResponsibleUserId());

                assertNotNull(route.getId());
                assertEquals("org-123", route.getOrganizationId());
                assertEquals("RUT001", route.getRouteCode());
                assertEquals("Ruta Principal", route.getRouteName());
                assertEquals(5, route.getTotalEstimatedDuration());
                assertEquals("user-1", route.getResponsibleUserId());
                assertEquals(Constants.ACTIVE.name(), route.getStatus());
                assertNotNull(route.getCreatedAt());
                // Verificar que la primera zona se establece correctamente
                assertEquals("zone-1", route.getZoneId());
                // Verificar que la lista de zonas se establece correctamente
                assertNotNull(route.getZones());
                assertEquals(2, route.getZones().size());
                assertEquals("zone-1", route.getZones().get(0).getZoneId());
                assertEquals(Integer.valueOf(1), route.getZones().get(0).getOrder());
                assertEquals(Integer.valueOf(2), route.getZones().get(0).getEstimatedDuration());
                assertEquals("zone-2", route.getZones().get(1).getZoneId());
                assertEquals(Integer.valueOf(2), route.getZones().get(1).getOrder());
                assertEquals(Integer.valueOf(3), route.getZones().get(1).getEstimatedDuration());
            })
            .verifyComplete();

        System.out.println("🎯 IT-01 completada exitosamente ✅\n");
    }

    @Test
    @Order(2)
    @DisplayName("IT-02: Crear múltiples rutas con códigos secuenciales")
    void createMultipleDistributionRoutes_ShouldGenerateSequentialCodes() {
        System.out.println("🚀 Iniciando IT-02: Crear múltiples rutas con códigos secuenciales...");
        // Arrange - Crear primera ruta
        DistributionRouteCreateRequest request1 = new DistributionRouteCreateRequest();
        request1.setOrganizationId("org-123");
        request1.setRouteName("Ruta 1");
        request1.setZones(Collections.singletonList(
            DistributionRouteCreateRequest.ZoneEntry.builder()
                .zoneId("zone-1")
                .order(1)
                .estimatedDuration(3)
                .build()
        ));
        request1.setTotalEstimatedDuration(3);
        request1.setResponsibleUserId("user-1");

        // Act - Crear tres rutas
        distributionRouteService.save(request1).block();

        DistributionRouteCreateRequest request2 = new DistributionRouteCreateRequest();
        request2.setOrganizationId("org-123");
        request2.setRouteName("Ruta 2");
        request2.setZones(Collections.singletonList(
            DistributionRouteCreateRequest.ZoneEntry.builder()
                .zoneId("zone-2")
                .order(1)
                .estimatedDuration(4)
                .build()
        ));
        request2.setTotalEstimatedDuration(4);
        request2.setResponsibleUserId("user-2");

        DistributionRouteCreateRequest request3 = new DistributionRouteCreateRequest();
        request3.setOrganizationId("org-123");
        request3.setRouteName("Ruta 3");
        request3.setZones(Collections.singletonList(
            DistributionRouteCreateRequest.ZoneEntry.builder()
                .zoneId("zone-3")
                .order(1)
                .estimatedDuration(6)
                .build()
        ));
        request3.setTotalEstimatedDuration(6);
        request3.setResponsibleUserId("user-3");

        // Assert
        StepVerifier.create(distributionRouteService.save(request2))
                .assertNext(route -> {
                System.out.println("✅ Segunda ruta creada: " + route.getRouteName() + " | Código: " + route.getRouteCode());
                assertEquals("RUT002", route.getRouteCode());
            })
            .verifyComplete();

        StepVerifier.create(distributionRouteService.save(request3))
                .assertNext(route -> {
                        System.out.println("✅ Tercera ruta creada: " + route.getRouteName() + " | Código: " + route.getRouteCode());
                        assertEquals("RUT003", route.getRouteCode());
                })
                .verifyComplete();

        System.out.println("🎯 IT-02 completada exitosamente ✅\n");
    }

    // ==================== READ TESTS ====================

    @Test
    @Order(3)
    @DisplayName("IT-03: Obtener ruta por ID exitosamente")
    void getDistributionRouteById_ShouldReturnRoute_WhenExists() {
        System.out.println("🔍 Iniciando IT-03: Obtener ruta por ID exitosamente...");
        // Arrange
        DistributionRoute route = DistributionRoute.builder()
                .organizationId("org-123")
                .routeCode("RUT001")
                .routeName("Ruta Test")
                .zones(Arrays.asList(
                    DistributionRoute.ZoneOrder.builder()
                        .zoneId("zone-1")
                        .order(1)
                        .estimatedDuration(2)
                        .build(),
                    DistributionRoute.ZoneOrder.builder()
                        .zoneId("zone-2")
                        .order(2)
                        .estimatedDuration(3)
                        .build()
                ))
                .totalEstimatedDuration(5)
                .responsibleUserId("user-1")
                .status(Constants.ACTIVE.name())
                .createdAt(Instant.now())
                .build();
        
        DistributionRoute saved = distributionRouteRepository.save(route).block();
        System.out.println("📦 Ruta guardada con ID: " + saved.getId());

        // Act & Assert
        StepVerifier.create(distributionRouteService.getById(saved.getId()))
                .assertNext(result -> {
                System.out.println("✅ Ruta encontrada: " + result.getRouteName());
                System.out.println("➡️ Código: " + result.getRouteCode());
                assertEquals(saved.getId(), result.getId());
                assertEquals("RUT001", result.getRouteCode());
                assertEquals("Ruta Test", result.getRouteName());
            })
            .verifyComplete();

        System.out.println("🎯 IT-03 completada exitosamente ✅\n");
    }

    @Test
    @Order(4)
    @DisplayName("IT-04: Listar todas las rutas")
    void getAllDistributionRoutes_ShouldReturnAllRoutes() {
        System.out.println("📋 Iniciando IT-04: Listar todas las rutas...");
        // Arrange - Crear múltiples rutas
        DistributionRoute route1 = DistributionRoute.builder()
                .organizationId("org-123")
                .routeCode("RUT001")
                .routeName("Ruta 1")
                .zones(Collections.singletonList(
                    DistributionRoute.ZoneOrder.builder()
                        .zoneId("zone-1")
                        .order(1)
                        .estimatedDuration(3)
                        .build()
                ))
                .totalEstimatedDuration(3)
                .status(Constants.ACTIVE.name())
                .responsibleUserId("user-1")
                .createdAt(Instant.now())
                .build();

        DistributionRoute route2 = DistributionRoute.builder()
                .organizationId("org-123")
                .routeCode("RUT002")
                .routeName("Ruta 2")
                .zones(Collections.singletonList(
                    DistributionRoute.ZoneOrder.builder()
                        .zoneId("zone-2")
                        .order(1)
                        .estimatedDuration(4)
                        .build()
                ))
                .totalEstimatedDuration(4)
                .status(Constants.INACTIVE.name())
                .responsibleUserId("user-2")
                .createdAt(Instant.now())
                .build();

        distributionRouteRepository.save(route1).block();
        distributionRouteRepository.save(route2).block();
        System.out.println("🗂️  Rutas creadas: [Ruta 1, Ruta 2]");

        // Act & Assert
        StepVerifier.create(distributionRouteService.getAll())
                .expectNextCount(2)
                .verifyComplete();

        System.out.println("✅ Se listaron correctamente todas las rutas (activas e inactivas).");
        System.out.println("🎯 IT-04 completada exitosamente ✅\n");
    }

    @Test
    @Order(5)
    @DisplayName("IT-05: Listar solo rutas activas")
    void getAllActiveDistributionRoutes_ShouldReturnOnlyActive() {
        System.out.println("🟢 Iniciando IT-05: Listar solo rutas activas...");
        // Arrange
        DistributionRoute active = DistributionRoute.builder()
                .organizationId("org-123")
                .routeCode("RUT001")
                .routeName("Ruta Activa")
                .zones(Collections.singletonList(
                    DistributionRoute.ZoneOrder.builder()
                        .zoneId("zone-1")
                        .order(1)
                        .estimatedDuration(3)
                        .build()
                ))
                .totalEstimatedDuration(3)
                .status(Constants.ACTIVE.name())
                .responsibleUserId("user-1")
                .createdAt(Instant.now())
                .build();

        DistributionRoute inactive = DistributionRoute.builder()
                .organizationId("org-123")
                .routeCode("RUT002")
                .routeName("Ruta Inactiva")
                .zones(Collections.singletonList(
                    DistributionRoute.ZoneOrder.builder()
                        .zoneId("zone-2")
                        .order(1)
                        .estimatedDuration(4)
                        .build()
                ))
                .totalEstimatedDuration(4)
                .status(Constants.INACTIVE.name())
                .responsibleUserId("user-2")
                .createdAt(Instant.now())
                .build();

        distributionRouteRepository.save(active).block();
        distributionRouteRepository.save(inactive).block();
        System.out.println("📦 Rutas creadas: Activa (" + active.getRouteCode() + ") e Inactiva (" + inactive.getRouteCode() + ")");

        // Act & Assert
        StepVerifier.create(distributionRouteService.getAllActive())
                .assertNext(route -> {
                System.out.println("✅ Ruta activa listada: " + route.getRouteName());
                assertEquals(Constants.ACTIVE.name(), route.getStatus());
            })
            .verifyComplete();

        System.out.println("🎯 IT-05 completada exitosamente ✅\n");
    }

    @Test
    @Order(6)
    @DisplayName("IT-06: Listar solo rutas inactivas")
    void getAllInactiveDistributionRoutes_ShouldReturnOnlyInactive() {
        System.out.println("🔴 Iniciando IT-06: Listar solo rutas inactivas...");
        // Arrange
        DistributionRoute active = DistributionRoute.builder()
                .organizationId("org-123")
                .routeCode("RUT001")
                .routeName("Ruta Activa")
                .zones(Collections.singletonList(
                    DistributionRoute.ZoneOrder.builder()
                        .zoneId("zone-1")
                        .order(1)
                        .estimatedDuration(3)
                        .build()
                ))
                .totalEstimatedDuration(3)
                .status(Constants.ACTIVE.name())
                .responsibleUserId("user-1")
                .createdAt(Instant.now())
                .build();

        DistributionRoute inactive = DistributionRoute.builder()
                .organizationId("org-123")
                .routeCode("RUT002")
                .routeName("Ruta Inactiva")
                .zones(Collections.singletonList(
                    DistributionRoute.ZoneOrder.builder()
                        .zoneId("zone-2")
                        .order(1)
                        .estimatedDuration(4)
                        .build()
                ))
                .totalEstimatedDuration(4)
                .status(Constants.INACTIVE.name())
                .responsibleUserId("user-2")
                .createdAt(Instant.now())
                .build();

        distributionRouteRepository.save(active).block();
        distributionRouteRepository.save(inactive).block();
        System.out.println("📦 Rutas creadas: Activa (" + active.getRouteCode() + ") e Inactiva (" + inactive.getRouteCode() + ")");

        // Act & Assert
        StepVerifier.create(distributionRouteService.getAllInactive())
                .assertNext(route -> {
                System.out.println("✅ Ruta inactiva listada: " + route.getRouteName());
                assertEquals(Constants.INACTIVE.name(), route.getStatus());
            })
            .verifyComplete();

        System.out.println("🎯 IT-06 completada exitosamente ✅\n");
    }

    // ==================== UPDATE TESTS ====================

    @Test
    @Order(7)
    @DisplayName("IT-07: Actualizar ruta exitosamente")
    void updateDistributionRoute_ShouldUpdateFields_WhenValid() {
        System.out.println("🛠️ Iniciando IT-07: Actualizar ruta exitosamente...");
        // Arrange - Crear ruta inicial
        DistributionRoute existing = DistributionRoute.builder()
                .organizationId("org-123")
                .routeCode("RUT001")
                .routeName("Nombre Original")
                .zones(Collections.singletonList(
                    DistributionRoute.ZoneOrder.builder()
                        .zoneId("zone-1")
                        .order(1)
                        .estimatedDuration(3)
                        .build()
                ))
                .totalEstimatedDuration(3)
                .status(Constants.ACTIVE.name())
                .responsibleUserId("user-1")
                .createdAt(Instant.now())
                .build();

        DistributionRoute saved = distributionRouteRepository.save(existing).block();
        System.out.println("📦 Ruta original creada:");
        System.out.println("   🔹 ID: " + saved.getId());
        System.out.println("   🔹 Nombre: " + saved.getRouteName());
        System.out.println("   🔹 Duración: " + saved.getTotalEstimatedDuration() + " horas");
        System.out.println("   🔹 Responsable: " + saved.getResponsibleUserId());

        DistributionRouteCreateRequest update = DistributionRouteCreateRequest.builder()
                .routeName("Nombre Actualizado")
                .zones(Arrays.asList(
                    DistributionRouteCreateRequest.ZoneEntry.builder()
                        .zoneId("zone-1")
                        .order(1)
                        .estimatedDuration(2)
                        .build(),
                    DistributionRouteCreateRequest.ZoneEntry.builder()
                        .zoneId("zone-2")
                        .order(2)
                        .estimatedDuration(3)
                        .build()
                ))
                .totalEstimatedDuration(5)
                .responsibleUserId("user-2")
                .build();

        System.out.println("📝 Datos nuevos para actualización:");
        System.out.println("   🔸 Nombre: " + update.getRouteName());
        System.out.println("   🔸 Duración: " + update.getTotalEstimatedDuration() + " horas");
        System.out.println("   🔸 Responsable: " + update.getResponsibleUserId());

        // Act & Assert
        StepVerifier.create(distributionRouteService.update(saved.getId(), update))
                .assertNext(updated -> {
                System.out.println("\n✅ Ruta actualizada correctamente:");
                System.out.println("   🔹 Nombre: " + updated.getRouteName());
                System.out.println("   🔹 Duración: " + updated.getTotalEstimatedDuration() + " horas");
                System.out.println("   🔹 Responsable: " + updated.getResponsibleUserId());
                System.out.println("   🔹 Código (no cambia): " + updated.getRouteCode());

                assertEquals("Nombre Actualizado", updated.getRouteName());
                assertEquals(5, updated.getTotalEstimatedDuration());
                assertEquals("user-2", updated.getResponsibleUserId());
                assertEquals("RUT001", updated.getRouteCode()); // No cambia
                // Verificar que la primera zona se establece correctamente
                assertEquals("zone-1", updated.getZoneId());
                // Verificar que la lista de zonas se establece correctamente
                assertNotNull(updated.getZones());
                assertEquals(2, updated.getZones().size());
                assertEquals("zone-1", updated.getZones().get(0).getZoneId());
                assertEquals(Integer.valueOf(1), updated.getZones().get(0).getOrder());
                assertEquals(Integer.valueOf(2), updated.getZones().get(0).getEstimatedDuration());
                assertEquals("zone-2", updated.getZones().get(1).getZoneId());
                assertEquals(Integer.valueOf(2), updated.getZones().get(1).getOrder());
                assertEquals(Integer.valueOf(3), updated.getZones().get(1).getEstimatedDuration());
            })
            .verifyComplete();

        System.out.println("🎯 IT-07 completada exitosamente ✅\n");
    }

    // ==================== ACTIVATE/DEACTIVATE TESTS ====================

    @Test
    @Order(8)
    @DisplayName("IT-08: Activar ruta inactiva")
    void activateDistributionRoute_ShouldChangeStatus_WhenInactive() {
        // Arrange
        DistributionRoute inactive = DistributionRoute.builder()
                .organizationId("org-123")
                .routeCode("RUT001")
                .routeName("Ruta Inactiva")
                .zones(Collections.singletonList(
                    DistributionRoute.ZoneOrder.builder()
                        .zoneId("zone-1")
                        .order(1)
                        .estimatedDuration(3)
                        .build()
                ))
                .totalEstimatedDuration(3)
                .status(Constants.INACTIVE.name())
                .responsibleUserId("user-1")
                .createdAt(Instant.now())
                .build();

        DistributionRoute saved = distributionRouteRepository.save(inactive).block();

        // Act & Assert
        StepVerifier.create(distributionRouteService.activate(saved.getId()))
                .assertNext(activated -> assertEquals(Constants.ACTIVE.name(), activated.getStatus()))
                .verifyComplete();
    }

    @Test
    @Order(9)
    @DisplayName("IT-09: Desactivar ruta activa")
    void deactivateDistributionRoute_ShouldChangeStatus_WhenActive() {
        // Arrange
        DistributionRoute active = DistributionRoute.builder()
                .organizationId("org-123")
                .routeCode("RUT001")
                .routeName("Ruta Activa")
                .zones(Collections.singletonList(
                    DistributionRoute.ZoneOrder.builder()
                        .zoneId("zone-1")
                        .order(1)
                        .estimatedDuration(3)
                        .build()
                ))
                .totalEstimatedDuration(3)
                .status(Constants.ACTIVE.name())
                .responsibleUserId("user-1")
                .createdAt(Instant.now())
                .build();

        DistributionRoute saved = distributionRouteRepository.save(active).block();

        // Act & Assert
        StepVerifier.create(distributionRouteService.deactivate(saved.getId()))
                .assertNext(deactivated -> assertEquals(Constants.INACTIVE.name(), deactivated.getStatus()))
                .verifyComplete();
    }

    // ==================== DELETE TESTS ====================

    @Test
    @Order(10)
    @DisplayName("IT-10: Eliminar ruta exitosamente")
    void deleteDistributionRoute_ShouldRemove_WhenExists() {
        System.out.println("🗑️ Iniciando IT-10: Eliminar ruta exitosamente...");
        // Arrange
        DistributionRoute route = DistributionRoute.builder()
                .organizationId("org-123")
                .routeCode("RUT001")
                .routeName("Ruta a Eliminar")
                .zones(Collections.singletonList(
                    DistributionRoute.ZoneOrder.builder()
                        .zoneId("zone-1")
                        .order(1)
                        .estimatedDuration(3)
                        .build()
                ))
                .totalEstimatedDuration(3)
                .status(Constants.ACTIVE.name())
                .responsibleUserId("user-1")
                .createdAt(Instant.now())
                .build();

        DistributionRoute saved = distributionRouteRepository.save(route).block();
        System.out.println("📦 Ruta creada con estado ACTIVO:");
        System.out.println("   🔹 ID: " + saved.getId());
        System.out.println("   🔹 Nombre: " + saved.getRouteName());
        System.out.println("   🔹 Zonas: " + saved.getZones());
        System.out.println("   🔹 Estado actual: " + saved.getStatus());

        // Act
        StepVerifier.create(distributionRouteService.delete(saved.getId()))
            .verifyComplete();
        System.out.println("✅ Ruta eliminada correctamente.");

        // Assert - Verificar que no existe
        System.out.println("→ Verificando que la ruta fue eliminada del repositorio...");
        StepVerifier.create(distributionRouteRepository.findById(saved.getId()))
                .expectNextCount(0)
                .verifyComplete();
        System.out.println("✔ Verificación exitosa: la ruta ya no existe en la base de datos.");

        System.out.println("🎯 IT-10 completada exitosamente ✅\n");
    }
}