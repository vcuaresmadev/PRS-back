package pe.edu.vallegrande.msdistribution.application.services.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import pe.edu.vallegrande.msdistribution.domain.models.DistributionProgram;
import pe.edu.vallegrande.msdistribution.infrastructure.dto.request.DistributionProgramCreateRequest;
import pe.edu.vallegrande.msdistribution.infrastructure.repository.DistributionProgramRepository;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;
import java.time.LocalDate;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Clase de pruebas unitarias para DistributionProgramServiceImpl.
 * Usa Mockito para simular el repositorio y StepVerifier para validar flujos reactivos.
 */
public class DistributionProgramServiceImplTest {

    // Simulación del repositorio
    @Mock
    private DistributionProgramRepository programRepository;

    // Inyección del servicio a probar
    @InjectMocks
    private DistributionProgramServiceImpl distributionProgramService;

    /**
     * Inicializa los mocks antes de cada prueba.
     */
    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    /**
     * Escenario positivo:
     * Debe retornar todos los programas mapeados correctamente a la respuesta.
     */
    @Test
    void getAll_shouldMapToResponse() {
        // Arrange - Creamos un programa de ejemplo
        DistributionProgram p1 = DistributionProgram.builder()
                .id("1").organizationId("org").zoneId("z").streetId("st")
                .programCode("PROG001").scheduleId("sch").routeId("r")
                .programDate(LocalDate.parse("2024-01-01"))
                .plannedStartTime("08:00").plannedEndTime("10:00")
                .status("PENDING").responsibleUserId("u").observations("obs")
                .createdAt(java.time.Instant.parse("2024-01-01T00:00:00Z")).build();

        when(programRepository.findAll()).thenReturn(Flux.just(p1));

        // Act & Assert - Validamos que el servicio retorna correctamente los datos
        StepVerifier.create(distributionProgramService.getAll())
                .assertNext(resp -> {
                    org.junit.jupiter.api.Assertions.assertEquals("1", resp.getId());
                    org.junit.jupiter.api.Assertions.assertEquals("PROG001", resp.getProgramCode());
                })
                .verifyComplete();
    }

    /**
     * Escenario positivo:
     * Debe retornar un programa por ID si existe.
     */
    @Test
    void getById_shouldReturn_whenExists() {
        // Arrange
        DistributionProgram p = DistributionProgram.builder().id("id-1").programCode("PROG007").build();
        when(programRepository.findById("id-1")).thenReturn(Mono.just(p));

        // Act & Assert
        StepVerifier.create(distributionProgramService.getById("id-1"))
                .assertNext(resp -> org.junit.jupiter.api.Assertions.assertEquals("PROG007", resp.getProgramCode()))
                .verifyComplete();
    }

    /**
     * Escenario negativo:
     * No debe retornar error si el ID no existe (flujo vacío).
     */
    @Test
    void getById_shouldComplete_whenNotFound() {
        when(programRepository.findById("missing")).thenReturn(Mono.empty());
        StepVerifier.create(distributionProgramService.getById("missing"))
                .expectComplete()
                .verify();
    }

    /**
     * Escenario positivo:
     * Debe guardar un nuevo programa correctamente.
     */
    @Test
    void save_shouldReturnResponse() {
        // Arrange - Creamos un request y el programa esperado
        DistributionProgramCreateRequest req = validRequestFor(LocalDate.parse("2024-01-02"));
        DistributionProgram program = DistributionProgram.builder()
                .id("new")
                .organizationId("org-1")
                .programCode("PROG001")
                .scheduleId("sch-1")
                .routeId("route-1")
                .zoneId("zone-1")
                .streetId("street-1")
                .programDate(LocalDate.parse("2024-01-02"))
                .plannedStartTime("08:00")
                .plannedEndTime("10:00")
                .status("PLANNED")
                .responsibleUserId("user-1")
                .observations("Test")
                .createdAt(java.time.Instant.now())
                .build();
        
        // Fix: Mock findTopByOrderByProgramCodeDesc to avoid null pointer
        when(programRepository.findTopByOrderByProgramCodeDesc()).thenReturn(Mono.empty());
        when(programRepository.save(any(DistributionProgram.class))).thenReturn(Mono.just(program));

        // Act & Assert
        StepVerifier.create(distributionProgramService.save(req))
                .assertNext(resp -> org.junit.jupiter.api.Assertions.assertEquals("PROG001", resp.getProgramCode()))
                .verifyComplete();
    }

    /**
     * Escenario positivo:
     * Debe actualizar un programa existente correctamente.
     */
    @Test
    void update_shouldMapAndSave_whenExists() {
        // Arrange
        String id = "p1";
        DistributionProgram existing = DistributionProgram.builder().id(id).build();
        DistributionProgramCreateRequest req = validRequestFor(LocalDate.parse("2024-01-05"));
        DistributionProgram updated = DistributionProgram.builder()
                .id(id)
                .organizationId("org-1")
                .programCode("PROG001")
                .scheduleId("sch-1")
                .routeId("route-1")
                .zoneId("zone-1")
                .streetId("street-1")
                .programDate(LocalDate.parse("2024-01-05"))
                .plannedStartTime("08:00")
                .plannedEndTime("10:00")
                .status("PLANNED")
                .responsibleUserId("user-1")
                .observations("Test")
                .build();
        
        when(programRepository.findById(id)).thenReturn(Mono.just(existing));
        when(programRepository.save(any(DistributionProgram.class))).thenReturn(Mono.just(updated));

        // Act & Assert
        StepVerifier.create(distributionProgramService.update(id, req))
                .assertNext(resp -> {
                    org.junit.jupiter.api.Assertions.assertEquals("08:00", resp.getPlannedStartTime());
                    org.junit.jupiter.api.Assertions.assertEquals("10:00", resp.getPlannedEndTime());
                })
                .verifyComplete();
    }

    /**
     * Escenario negativo:
     * No debe hacer nada si el programa a actualizar no existe.
     */
    @Test
    void update_shouldComplete_whenNotFound() {
        when(programRepository.findById("missing")).thenReturn(Mono.empty());
        StepVerifier.create(distributionProgramService.update("missing", validRequestFor(LocalDate.parse("2024-01-06"))))
                .expectComplete()
                .verify();
    }

    /**
     * Escenario positivo:
     * Debe eliminar un programa existente correctamente.
     */
    @Test
    void delete_shouldComplete_whenExists() {
        when(programRepository.deleteById("p")).thenReturn(Mono.empty());

        StepVerifier.create(distributionProgramService.delete("p")).verifyComplete();
    }

    /**
     * Escenario negativo:
     * No debe fallar si intenta eliminar un ID inexistente.
     */
    @Test
    void delete_shouldComplete_whenNotFound() {
        when(programRepository.deleteById("missing")).thenReturn(Mono.empty());
        StepVerifier.create(distributionProgramService.delete("missing"))
                .verifyComplete();
    }

    /**
     * Escenario positivo:
     * Debe activar un programa y persistir el cambio.
     */
    @Test
    void changeStatus_activate_shouldPersist() {
        DistributionProgram p = DistributionProgram.builder().id("p").status("OLD").build();
        DistributionProgram updated = DistributionProgram.builder().id("p").status("ACTIVE").build();
        when(programRepository.findById("p")).thenReturn(Mono.just(p));
        when(programRepository.save(any(DistributionProgram.class))).thenReturn(Mono.just(updated));

        StepVerifier.create(distributionProgramService.activate("p"))
                .assertNext(resp -> org.junit.jupiter.api.Assertions.assertEquals("ACTIVE", resp.getStatus()))
                .verifyComplete();
    }

    /**
     * Escenario positivo:
     * Debe desactivar un programa y persistir el cambio.
     */
    @Test
    void changeStatus_deactivate_shouldPersist() {
        DistributionProgram p = DistributionProgram.builder().id("p").status("OLD").build();
        DistributionProgram updated = DistributionProgram.builder().id("p").status("INACTIVE").build();
        when(programRepository.findById("p")).thenReturn(Mono.just(p));
        when(programRepository.save(any(DistributionProgram.class))).thenReturn(Mono.just(updated));

        StepVerifier.create(distributionProgramService.desactivate("p"))
                .assertNext(resp -> org.junit.jupiter.api.Assertions.assertEquals("INACTIVE", resp.getStatus()))
                .verifyComplete();
    }

    /**
     * Escenario negativo:
     * No debe hacer nada si el programa no existe al cambiar su estado.
     */
    @Test
    void changeStatus_shouldComplete_whenNotFound() {
        when(programRepository.findById("missing")).thenReturn(Mono.empty());
        StepVerifier.create(distributionProgramService.activate("missing"))
                .expectComplete()
                .verify();
    }

    /**
     * Validación:
     * Debe manejar correctamente valores nulos en fechas del programa.
     */
    @Test
    void toResponse_shouldHandleNullProgramDateAndCreatedAt() {
        DistributionProgram p = DistributionProgram.builder()
                .id("x").programCode("PROG123")
                .programDate(null).createdAt(null)
                .build();

        when(programRepository.findById("x")).thenReturn(Mono.just(p));

        StepVerifier.create(distributionProgramService.getById("x"))
                .assertNext(resp -> {
                    org.junit.jupiter.api.Assertions.assertNull(resp.getProgramDate());
                    org.junit.jupiter.api.Assertions.assertNull(resp.getCreatedAt());
                })
                .verifyComplete();
    }

    /**
     * Método auxiliar:
     * Crea un request válido para usar en las pruebas.
     */
    private DistributionProgramCreateRequest validRequestFor(LocalDate date) {
        return DistributionProgramCreateRequest.builder()
                .scheduleId("sch-1")
                .routeId("route-1")
                .zoneId("zone-1")
                .organizationId("org-1")
                .streetId("street-1")
                .programDate(date)
                .plannedStartTime("08:00")
                .plannedEndTime("10:00")
                .responsibleUserId("user-1")
                .observations("Test")
                .build();
    }

    /**
     * Escenario negativo:
     * Debe lanzar un error si el repositorio falla al guardar un programa.
     */
    @Test
    void saveDistributionProgram_shouldReturnError_whenRepositoryFails() {
         System.out.println("Starting test: Repository failure when saving DistributionProgram");

        // Arrange - Construimos el request
        DistributionProgramCreateRequest request = DistributionProgramCreateRequest.builder()
                .scheduleId("sch-1")
                .routeId("route-1")
                .zoneId("zone-1")
                .organizationId("org-1")
                .streetId("street-1")
                .programDate(LocalDate.now())
                .plannedStartTime("08:00")
                .plannedEndTime("10:00")
                .responsibleUserId("user-1")
                .observations("Test observation")
                .build();

        // Fix: Mock findTopByOrderByProgramCodeDesc to avoid null pointer
        when(programRepository.findTopByOrderByProgramCodeDesc()).thenReturn(Mono.empty());
        // Simulamos un fallo en la base de datos
        when(programRepository.save(ArgumentMatchers.any(DistributionProgram.class)))
                .thenReturn(Mono.error(new RuntimeException("Database error")));

        // Act & Assert - Validamos que el error es correctamente capturado
        StepVerifier.create(distributionProgramService.save(request))
                .expectErrorSatisfies(throwable -> {
                    System.out.println("Error captured: " + throwable.getMessage());
                    assert throwable instanceof RuntimeException;
                    assert throwable.getMessage().contains("Database error");
                })
                .verify();

        System.out.println("Test completed: Repository error was correctly detected\n");
    }
}