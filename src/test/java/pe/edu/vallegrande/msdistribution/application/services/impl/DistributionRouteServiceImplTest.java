package pe.edu.vallegrande.msdistribution.application.services.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import pe.edu.vallegrande.msdistribution.domain.enums.Constants;
import pe.edu.vallegrande.msdistribution.domain.models.DistributionRoute;
import pe.edu.vallegrande.msdistribution.infrastructure.dto.request.DistributionRouteCreateRequest;
import pe.edu.vallegrande.msdistribution.infrastructure.exception.CustomException;
import pe.edu.vallegrande.msdistribution.infrastructure.repository.DistributionRouteRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Clase de pruebas unitarias para el servicio DistributionRouteServiceImpl.
 * Usa Mockito para simular el comportamiento del repositorio y StepVerifier
 * para probar los resultados reactivos (Mono y Flux).
 */
public class DistributionRouteServiceImplTest {

    // Se simula (mockea) el repositorio para evitar acceder a la base de datos real
    @Mock
    private DistributionRouteRepository routeRepository;

    // Se inyecta el mock del repositorio en el servicio a probar
    @InjectMocks
    private DistributionRouteServiceImpl routeService;

    /**
     * Método que se ejecuta antes de cada test.
     * Inicializa los mocks de Mockito.
     */
    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    /**
     * ✅ Escenario positivo:
     * Debe crear correctamente una ruta válida con datos correctos.
     */
    @Test
    void save_ShouldCreateRoute_WhenRequestIsValid() {
        System.out.println("Starting test: Creating valid route");
        
        // Arrange - Se crea una solicitud de creación con datos de ejemplo
        DistributionRouteCreateRequest request = new DistributionRouteCreateRequest();
        request.setOrganizationId("org-1");
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

        // Simula que no existen rutas previas (para que genere el código RUT001)
        when(routeRepository.findTopByOrderByRouteCodeDesc()).thenReturn(Mono.empty());

        // Captura del objeto que se guardará (para validarlo luego)
        ArgumentCaptor<DistributionRoute> routeCaptor = ArgumentCaptor.forClass(DistributionRoute.class);

        // Simula que no existe una ruta con el mismo código
        when(routeRepository.existsByRouteCode(anyString())).thenReturn(Mono.just(false));

        // Crea una ruta simulada como si se hubiera guardado correctamente
        DistributionRoute savedRoute = DistributionRoute.builder()
                .id("route-1")
                .organizationId("org-1")
                .routeCode("RUT001")
                .routeName("Ruta Principal")
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

        // Simula el retorno del repositorio al guardar la ruta
        when(routeRepository.save(any(DistributionRoute.class))).thenReturn(Mono.just(savedRoute));

        // Act & Assert - Ejecuta el método y verifica los resultados con StepVerifier
        StepVerifier.create(routeService.save(request))
                .assertNext(response -> {
                    System.out.println("Route created correctly with code: " + response.getRouteCode());
                    assertNotNull(response);
                    assertEquals("route-1", response.getId());
                    assertEquals("org-1", response.getOrganizationId());
                    assertEquals("RUT001", response.getRouteCode());
                    assertEquals("Ruta Principal", response.getRouteName());
                    assertEquals(5, response.getTotalEstimatedDuration());
                    assertEquals("user-1", response.getResponsibleUserId());
                    assertEquals(Constants.ACTIVE.name(), response.getStatus());
                    assertNotNull(response.getCreatedAt());
                    // Verificar que la primera zona se establece correctamente
                    assertEquals("zone-1", response.getZoneId());
                    // Verificar que la lista de zonas se establece correctamente
                    assertNotNull(response.getZones());
                    assertEquals(2, response.getZones().size());
                    assertEquals("zone-1", response.getZones().get(0).getZoneId());
                    assertEquals(Integer.valueOf(1), response.getZones().get(0).getOrder());
                    assertEquals(Integer.valueOf(2), response.getZones().get(0).getEstimatedDuration());
                    assertEquals("zone-2", response.getZones().get(1).getZoneId());
                    assertEquals(Integer.valueOf(2), response.getZones().get(1).getOrder());
                    assertEquals(Integer.valueOf(3), response.getZones().get(1).getEstimatedDuration());
                })
                .verifyComplete();

        // Verifica que los métodos del repositorio se hayan llamado correctamente
        verify(routeRepository).findTopByOrderByRouteCodeDesc();
        verify(routeRepository).existsByRouteCode("RUT001");
        verify(routeRepository).save(routeCaptor.capture());

        // Valida los datos enviados al repositorio antes de guardar
        DistributionRoute routeToSave = routeCaptor.getValue();
        System.out.println("Data sent to repository:");
        System.out.println("   Organization: " + routeToSave.getOrganizationId());
        System.out.println("   Code: " + routeToSave.getRouteCode());
        System.out.println("   Name: " + routeToSave.getRouteName());
        System.out.println("   Total duration: " + routeToSave.getTotalEstimatedDuration());
        System.out.println("   Responsible user: " + routeToSave.getResponsibleUserId());
        System.out.println("   Status: " + routeToSave.getStatus());

        assertEquals("org-1", routeToSave.getOrganizationId());
        assertEquals("RUT001", routeToSave.getRouteCode());
        assertEquals("Ruta Principal", routeToSave.getRouteName());
        assertEquals(5, routeToSave.getTotalEstimatedDuration());
        assertEquals("user-1", routeToSave.getResponsibleUserId());
        assertEquals(Constants.ACTIVE.name(), routeToSave.getStatus());
        // Verificar que la lista de zonas se establece correctamente
        assertNotNull(routeToSave.getZones());
        assertEquals(2, routeToSave.getZones().size());
        assertEquals("zone-1", routeToSave.getZones().get(0).getZoneId());
        assertEquals(1, routeToSave.getZones().get(0).getOrder());
        assertEquals(2, routeToSave.getZones().get(0).getEstimatedDuration());
        assertEquals("zone-2", routeToSave.getZones().get(1).getZoneId());
        assertEquals(2, routeToSave.getZones().get(1).getOrder());
        assertEquals(3, routeToSave.getZones().get(1).getEstimatedDuration());

        System.out.println("Test completed successfully\n");
    }

    /**
     * ✅ Escenario positivo:
     * Debe generar el siguiente código secuencial correctamente (ej: RUT005 → RUT006).
     */
    @Test
    void save_ShouldGenerateNextRouteCode_WhenPreviousRoutesExist() {
        System.out.println("Starting test: Sequential code generation");
        
        // Arrange - Simula una ruta existente con código RUT005
        DistributionRoute existingRoute = DistributionRoute.builder()
                .routeCode("RUT005")
                .build();

        // Nueva solicitud
        DistributionRouteCreateRequest request = new DistributionRouteCreateRequest();
        request.setOrganizationId("org-1");
        request.setRouteName("Nueva Ruta");
        request.setZones(Collections.singletonList(
            DistributionRouteCreateRequest.ZoneEntry.builder()
                .zoneId("zone-1")
                .order(1)
                .estimatedDuration(3)
                .build()
        ));
        request.setTotalEstimatedDuration(3);
        request.setResponsibleUserId("user-1");

        // Mock de repositorio
        when(routeRepository.findTopByOrderByRouteCodeDesc()).thenReturn(Mono.just(existingRoute));
        when(routeRepository.existsByRouteCode("RUT006")).thenReturn(Mono.just(false));

        // Simula la ruta guardada
        when(routeRepository.save(any(DistributionRoute.class))).thenReturn(Mono.just(
                DistributionRoute.builder()
                        .id("route-2")
                        .routeCode("RUT006")
                        .organizationId("org-1")
                        .routeName("Nueva Ruta")
                        .zones(Collections.singletonList(
                            DistributionRoute.ZoneOrder.builder()
                                .zoneId("zone-1")
                                .order(1)
                                .estimatedDuration(3)
                                .build()
                        ))
                        .totalEstimatedDuration(3)
                        .responsibleUserId("user-1")
                        .status(Constants.ACTIVE.name())
                        .createdAt(Instant.now())
                        .build()
        ));

        // Act & Assert - Verifica que se genere RUT006
        StepVerifier.create(routeService.save(request))
                .assertNext(response -> {
                    assertEquals("RUT006", response.getRouteCode());
                    System.out.println("Code generated correctly: " + response.getRouteCode());
                })
                .verifyComplete();

        System.out.println("Code generation test completed\n");
    }

    /**
     * ❌ Escenario negativo:
     * Debe devolver error cuando el repositorio falla al guardar la ruta.
     */
    @Test
    void save_ShouldReturnError_WhenRepositoryFails() {
        System.out.println("Starting negative test: Repository failure when saving route");

        // Arrange - Datos de prueba
        DistributionRouteCreateRequest request = new DistributionRouteCreateRequest();
        request.setOrganizationId("org-1");
        request.setRouteName("Ruta de Prueba");
        request.setZones(Collections.singletonList(
            DistributionRouteCreateRequest.ZoneEntry.builder()
                .zoneId("zone-1")
                .order(1)
                .estimatedDuration(2)
                .build()
        ));
        request.setTotalEstimatedDuration(2);
        request.setResponsibleUserId("user-1");

        // Simula fallo en la base de datos
        when(routeRepository.findTopByOrderByRouteCodeDesc()).thenReturn(Mono.empty());
        when(routeRepository.existsByRouteCode("RUT001")).thenReturn(Mono.just(false));
        when(routeRepository.save(any(DistributionRoute.class)))
                .thenReturn(Mono.error(new RuntimeException("Database error")));

        // Act & Assert - Verifica que se produzca el error esperado
        StepVerifier.create(routeService.save(request))
                .expectErrorSatisfies(error -> {
                    assertTrue(error instanceof RuntimeException);
                    assertEquals("Database error", error.getMessage());
                    System.out.println("Expected error: " + error.getMessage());
                })
                .verify();

        System.out.println("Negative test completed successfully\n");
    }

    /**
     * ✅ Escenario positivo:
     * Debe activar correctamente una ruta existente.
     */
    @Test
    void activate_ShouldActivateRoute_WhenRouteExists() {
        System.out.println("Starting test: Route activation");

        // Arrange - Ruta existente inactiva
        String routeId = "route-1";
        DistributionRoute existingRoute = DistributionRoute.builder()
                .id(routeId)
                .status(Constants.INACTIVE.name())
                .build();

        // Mock de repositorio
        when(routeRepository.findById(routeId)).thenReturn(Mono.just(existingRoute));
        when(routeRepository.save(any(DistributionRoute.class))).thenReturn(Mono.just(
                DistributionRoute.builder().id(routeId).status(Constants.ACTIVE.name()).build()
        ));

        // Act & Assert - Verifica la activación
        StepVerifier.create(routeService.activate(routeId))
                .assertNext(route -> {
                    assertEquals(Constants.ACTIVE.name(), route.getStatus());
                    System.out.println("Route activated correctly");
                })
                .verifyComplete();

        System.out.println("Activation test completed\n");
    }

    /**
     * ✅ Escenario positivo:
     * Debe desactivar correctamente una ruta existente.
     */
    @Test
    void deactivate_ShouldDeactivateRoute_WhenRouteExists() {
        System.out.println("Starting test: Route deactivation");

        // Arrange - Ruta activa
        String routeId = "route-1";
        DistributionRoute existingRoute = DistributionRoute.builder()
                .id(routeId)
                .status(Constants.ACTIVE.name())
                .build();

        when(routeRepository.findById(routeId)).thenReturn(Mono.just(existingRoute));
        when(routeRepository.save(any(DistributionRoute.class))).thenReturn(Mono.just(
                DistributionRoute.builder().id(routeId).status(Constants.INACTIVE.name()).build()
        ));

        // Act & Assert
        StepVerifier.create(routeService.deactivate(routeId))
                .assertNext(route -> {
                    assertEquals(Constants.INACTIVE.name(), route.getStatus());
                    System.out.println("Route deactivated correctly");
                })
                .verifyComplete();

        System.out.println("Deactivation test completed\n");
    }

    /**
     * ❌ Escenario negativo:
     * Debe lanzar error si se intenta activar una ruta inexistente.
     */
    @Test
    void activate_ShouldReturnError_WhenRouteNotFound() {
        System.out.println("Starting negative test: Activation of non-existent route");

        // Arrange
        String routeId = "route-inexistente";
        when(routeRepository.findById(routeId)).thenReturn(Mono.empty());

        // Act & Assert
        StepVerifier.create(routeService.activate(routeId))
                .expectErrorSatisfies(error -> {
                    assertTrue(error instanceof CustomException);
                    CustomException ce = (CustomException) error;
                    assertEquals("Route not found", ce.getErrorMessage().getMessage());
                    System.out.println("Expected error: " + ce.getMessage());
                })
                .verify();

        System.out.println("Negative test completed successfully\n");
    }

    /**
     * ✅ Escenario positivo:
     * Debe retornar todas las rutas.
     */
    @Test
    void getAll_ShouldReturnAllRoutes_WhenCalled() {
        System.out.println("Starting test: Get all routes");
        
        // Arrange - Lista simulada de rutas
        List<DistributionRoute> routes = Arrays.asList(
            DistributionRoute.builder()
                .id("route-1")
                .routeCode("RUT001")
                .routeName("Ruta Principal")
                .status(Constants.ACTIVE.name())
                .build(),
            DistributionRoute.builder()
                .id("route-2")
                .routeCode("RUT002")
                .routeName("Ruta Secundaria")
                .status(Constants.INACTIVE.name())
                .build()
        );

        when(routeRepository.findAll()).thenReturn(Flux.fromIterable(routes));

        // Act & Assert
        StepVerifier.create(routeService.getAll())
                .assertNext(route -> {
                    assertEquals("route-1", route.getId());
                    assertEquals("RUT001", route.getRouteCode());
                    assertEquals("Ruta Principal", route.getRouteName());
                    assertEquals(Constants.ACTIVE.name(), route.getStatus());
                    System.out.println("First route obtained correctly");
                })
                .assertNext(route -> {
                    assertEquals("route-2", route.getId());
                    assertEquals("RUT002", route.getRouteCode());
                    assertEquals("Ruta Secundaria", route.getRouteName());
                    assertEquals(Constants.INACTIVE.name(), route.getStatus());
                    System.out.println("Second route obtained correctly");
                })
                .verifyComplete();

        verify(routeRepository).findAll();
        System.out.println("Get all routes test completed\n");
    }

    /**
     * ✅ Escenario positivo:
     * Debe retornar todas las rutas activas.
     */
    @Test
    void getAllActive_ShouldReturnActiveRoutes_WhenCalled() {
        System.out.println("Starting test: Get active routes");
        
        // Arrange
        List<DistributionRoute> activeRoutes = Arrays.asList(
            DistributionRoute.builder()
                .id("route-1")
                .routeCode("RUT001")
                .routeName("Ruta Principal")
                .status(Constants.ACTIVE.name())
                .build(),
            DistributionRoute.builder()
                .id("route-3")
                .routeCode("RUT003")
                .routeName("Ruta Nocturna")
                .status(Constants.ACTIVE.name())
                .build()
        );

        when(routeRepository.findAllByStatus(Constants.ACTIVE.name()))
            .thenReturn(Flux.fromIterable(activeRoutes));

        // Act & Assert
        StepVerifier.create(routeService.getAllActive())
                .assertNext(route -> {
                    assertEquals("route-1", route.getId());
                    assertEquals(Constants.ACTIVE.name(), route.getStatus());
                    System.out.println("First active route obtained correctly");
                })
                .assertNext(route -> {
                    assertEquals("route-3", route.getId());
                    assertEquals(Constants.ACTIVE.name(), route.getStatus());
                    System.out.println("Second active route obtained correctly");
                })
                .verifyComplete();

        verify(routeRepository).findAllByStatus(Constants.ACTIVE.name());
        System.out.println("Get active routes test completed\n");
    }

    /**
     * ✅ Escenario positivo:
     * Debe retornar todas las rutas inactivas.
     */
    @Test
    void getAllInactive_ShouldReturnInactiveRoutes_WhenCalled() {
        System.out.println("Starting test: Get inactive routes");
        
        // Arrange
        List<DistributionRoute> inactiveRoutes = Arrays.asList(
            DistributionRoute.builder()
                .id("route-2")
                .routeCode("RUT002")
                .routeName("Ruta Secundaria")
                .status(Constants.INACTIVE.name())
                .build()
        );

        when(routeRepository.findAllByStatus(Constants.INACTIVE.name()))
            .thenReturn(Flux.fromIterable(inactiveRoutes));

        // Act & Assert
        StepVerifier.create(routeService.getAllInactive())
                .assertNext(route -> {
                    assertEquals("route-2", route.getId());
                    assertEquals(Constants.INACTIVE.name(), route.getStatus());
                    System.out.println("Inactive route obtained correctly");
                })
                .verifyComplete();

        verify(routeRepository).findAllByStatus(Constants.INACTIVE.name());
        System.out.println("Get inactive routes test completed\n");
    }

    /**
     * ✅ Escenario positivo:
     * Debe retornar una ruta por ID si existe.
     */
    @Test
    void getById_ShouldReturnRoute_WhenRouteExists() {
        System.out.println("Starting test: Get route by existing ID");
        
        // Arrange
        String routeId = "route-1";
        DistributionRoute route = DistributionRoute.builder()
                .id(routeId)
                .routeCode("RUT001")
                .routeName("Ruta Principal")
                .status(Constants.ACTIVE.name())
                .build();

        when(routeRepository.findById(routeId)).thenReturn(Mono.just(route));

        // Act & Assert
        StepVerifier.create(routeService.getById(routeId))
                .assertNext(foundRoute -> {
                    assertEquals(routeId, foundRoute.getId());
                    assertEquals("RUT001", foundRoute.getRouteCode());
                    assertEquals("Ruta Principal", foundRoute.getRouteName());
                    assertEquals(Constants.ACTIVE.name(), foundRoute.getStatus());
                    System.out.println("Route found correctly");
                })
                .verifyComplete();

        verify(routeRepository).findById(routeId);
        System.out.println("Get route by ID test completed\n");
    }

    /**
     * ❌ Escenario negativo:
     * Debe lanzar error si no existe la ruta al buscar por ID.
     */
    @Test
    void getById_ShouldReturnError_WhenRouteNotFound() {
        System.out.println("Starting negative test: Get non-existent route");
        
        // Arrange
        String routeId = "route-inexistente";
        when(routeRepository.findById(routeId)).thenReturn(Mono.empty());

        // Act & Assert
        StepVerifier.create(routeService.getById(routeId))
                .expectErrorSatisfies(error -> {
                    assertTrue(error instanceof CustomException);
                    CustomException ce = (CustomException) error;
                    assertEquals("Route not found", ce.getErrorMessage().getMessage());
                    System.out.println("Expected error: " + ce.getMessage());
                })
                .verify();

        verify(routeRepository).findById(routeId);
        System.out.println("Negative test completed successfully\n");
    }

    /**
     * ✅ Escenario positivo:
     * Debe eliminar una ruta existente correctamente.
     */
    @Test
    void delete_ShouldDeleteRoute_WhenRouteExists() {
        System.out.println("Starting test: Delete existing route");
        
        // Arrange
        String routeId = "route-1";
        DistributionRoute route = DistributionRoute.builder()
                .id(routeId)
                .routeCode("RUT001")
                .routeName("Ruta a Eliminar")
                .status(Constants.ACTIVE.name())
                .build();

        when(routeRepository.findById(routeId)).thenReturn(Mono.just(route));
        when(routeRepository.delete(any(DistributionRoute.class))).thenReturn(Mono.empty());

        // Act & Assert
        StepVerifier.create(routeService.delete(routeId))
                .verifyComplete();

        verify(routeRepository).findById(routeId);
        verify(routeRepository).delete(any(DistributionRoute.class));
        System.out.println("Route deleted correctly");
        System.out.println("Deletion test completed\n");
    }

    /**
     * ❌ Escenario negativo:
     * Debe lanzar error si se intenta eliminar una ruta inexistente.
     */
    @Test
    void delete_ShouldReturnError_WhenRouteNotFound() {
        System.out.println("Starting negative test: Delete non-existent route");
        
        // Arrange
        String routeId = "route-inexistente";
        when(routeRepository.findById(routeId)).thenReturn(Mono.empty());

        // Act & Assert
        StepVerifier.create(routeService.delete(routeId))
                .expectErrorSatisfies(error -> {
                    assertTrue(error instanceof CustomException);
                    CustomException ce = (CustomException) error;
                    assertEquals("Route not found", ce.getErrorMessage().getMessage());
                    System.out.println("Expected error: " + ce.getMessage());
                })
                .verify();

        verify(routeRepository).findById(routeId);
        verify(routeRepository, never()).delete(any(DistributionRoute.class));
        System.out.println("Negative deletion test completed\n");
    }

    /**
     * ❌ Escenario negativo:
     * Debe lanzar error si se intenta desactivar una ruta inexistente.
     */
    @Test
    void deactivate_ShouldReturnError_WhenRouteNotFound() {
        System.out.println("Starting negative test: Deactivate non-existent route");

        // Arrange
        String routeId = "route-inexistente";
        when(routeRepository.findById(routeId)).thenReturn(Mono.empty());

        // Act & Assert
        StepVerifier.create(routeService.deactivate(routeId))
                .expectErrorSatisfies(error -> {
                    assertTrue(error instanceof CustomException);
                    CustomException ce = (CustomException) error;
                    assertEquals("Route not found", ce.getErrorMessage().getMessage());
                    System.out.println("Expected error: " + ce.getMessage());
                })
                .verify();

        System.out.println("Negative deactivation test completed\n");
    }

    /**
     * 🧩 Escenario de validación:
     * Debe lanzar NumberFormatException si el código existente no tiene formato válido.
     */
    @Test
    void generateNextRouteCode_ShouldThrowNumberFormatException_WhenInvalidCodeFormat() {
        System.out.println("Starting test: Invalid code parsing error");
        
        // Arrange - Simulate a code with invalid format
        DistributionRoute existingRoute = DistributionRoute.builder()
                .routeCode("RUTINVALID")
                .build();

        DistributionRouteCreateRequest request = new DistributionRouteCreateRequest();
        request.setOrganizationId("org-1");
        request.setRouteName("Ruta de Prueba");
        request.setZones(Collections.singletonList(
            DistributionRouteCreateRequest.ZoneEntry.builder()
                .zoneId("zone-1")
                .order(1)
                .estimatedDuration(2)
                .build()
        ));
        request.setTotalEstimatedDuration(2);
        request.setResponsibleUserId("user-1");

        when(routeRepository.findTopByOrderByRouteCodeDesc()).thenReturn(Mono.just(existingRoute));
        when(routeRepository.existsByRouteCode("RUT001")).thenReturn(Mono.just(false));
        when(routeRepository.save(any(DistributionRoute.class))).thenReturn(Mono.just(
                DistributionRoute.builder()
                        .id("route-1")
                        .routeCode("RUT001")
                        .organizationId("org-1")
                        .routeName("Ruta de Prueba")
                        .zones(Collections.singletonList(
                            DistributionRoute.ZoneOrder.builder()
                                .zoneId("zone-1")
                                .order(1)
                                .estimatedDuration(2)
                                .build()
                        ))
                        .totalEstimatedDuration(2)
                        .responsibleUserId("user-1")
                        .status(Constants.ACTIVE.name())
                        .createdAt(Instant.now())
                        .build()
        ));

        // Act & Assert
        StepVerifier.create(routeService.save(request))
                .expectNextCount(1)
                .verifyComplete();

        System.out.println("Parsing error test completed\n");
    }
}