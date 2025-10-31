package pe.edu.vallegrande.msdistribution.integration;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.TestPropertySource;
import pe.edu.vallegrande.msdistribution.application.services.DistributionScheduleService;
import pe.edu.vallegrande.msdistribution.domain.enums.Constants;
import pe.edu.vallegrande.msdistribution.domain.models.DistributionSchedule;
import pe.edu.vallegrande.msdistribution.infrastructure.dto.request.DistributionScheduleCreateRequest;
import pe.edu.vallegrande.msdistribution.infrastructure.repository.DistributionScheduleRepository;
import pe.edu.vallegrande.msdistribution.infrastructure.service.ExternalServiceClient;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

/**
 * Pruebas de integración para DistributionScheduleService
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
public class DistributionScheduleIntegrationTest {

    @Autowired
    private DistributionScheduleService distributionScheduleService;

    @Autowired
    private DistributionScheduleRepository distributionScheduleRepository;

    @MockBean
    private ExternalServiceClient externalServiceClient;

    @BeforeEach
    void setUp() {
        // Mock external service calls
        when(externalServiceClient.getOrganizationById("org-123"))
            .thenReturn(Mono.just(new pe.edu.vallegrande.msdistribution.infrastructure.client.dto.ExternalOrganization()));
        
        // Clean database before each test
        distributionScheduleRepository.deleteAll().block();
    }

    // ==================== CREATE TESTS ====================

    @Test
    @Order(1)
    @DisplayName("IT-01: Crear horario de distribución exitosamente con código HOR001")
    void createDistributionSchedule_ShouldGenerateHOR001_WhenNoSchedulesExist() {
        System.out.println("=== IT-01: Iniciando prueba de creación de horario con código HOR001 ===");
        // Arrange
        List<String> daysOfWeek = Arrays.asList("LUNES", "MIÉRCOLES", "VIERNES");
        
        DistributionScheduleCreateRequest request = new DistributionScheduleCreateRequest();
        request.setOrganizationId("org-123");
        request.setZoneId("zone-1");
        request.setScheduleName("Horario Zona Centro");
        request.setDaysOfWeek(daysOfWeek);
        request.setStartTime("06:00");
        request.setEndTime("12:00");
        request.setDurationHours(6);

        System.out.println("Datos del horario a crear:");
        System.out.println(" - Organización: " + request.getOrganizationId());
        System.out.println(" - Zona: " + request.getZoneId());
        System.out.println(" - Nombre: " + request.getScheduleName());
        System.out.println(" - Días: " + request.getDaysOfWeek());
        System.out.println(" - Hora inicio: " + request.getStartTime() + " | Hora fin: " + request.getEndTime());

        // Act & Assert
        StepVerifier.create(distributionScheduleService.save(request))
                .assertNext(schedule -> {
                
                    System.out.println("✅ Horario creado con éxito. Validando campos...");
                    assertNotNull(schedule.getId());
                assertEquals("org-123", schedule.getOrganizationId());
                assertEquals("HOR001", schedule.getScheduleCode());
                assertEquals("zone-1", schedule.getZoneId());
                assertEquals("Horario Zona Centro", schedule.getScheduleName());
                assertEquals(3, schedule.getDaysOfWeek().size());
                assertEquals("LUNES", schedule.getDaysOfWeek().get(0));
                assertEquals("MIÉRCOLES", schedule.getDaysOfWeek().get(1));
                assertEquals("VIERNES", schedule.getDaysOfWeek().get(2));
                assertEquals("06:00", schedule.getStartTime());
                assertEquals("12:00", schedule.getEndTime());
                assertEquals(6, schedule.getDurationHours());
                assertEquals(Constants.ACTIVE.name(), schedule.getStatus());
                assertNotNull(schedule.getCreatedAt());

                System.out.println(" - Código generado: " + schedule.getScheduleCode());
                System.out.println(" - Estado: " + schedule.getStatus());
                System.out.println(" - Fecha creación: " + schedule.getCreatedAt());
                })
                .verifyComplete();

                System.out.println("✅ IT-01 completada correctamente.\n");
    }

    @Test
    @Order(2)
    @DisplayName("IT-02: Crear múltiples horarios con códigos secuenciales")
    void createMultipleDistributionSchedules_ShouldGenerateSequentialCodes() {
        System.out.println("=== IT-02: Iniciando prueba de creación múltiple de horarios secuenciales ===");
        // Arrange - Crear primer horario
        List<String> daysOfWeek1 = Arrays.asList("LUNES", "MIÉRCOLES");
        
        DistributionScheduleCreateRequest request1 = new DistributionScheduleCreateRequest();
        request1.setOrganizationId("org-123");
        request1.setZoneId("zone-1");
        request1.setScheduleName("Horario 1");
        request1.setDaysOfWeek(daysOfWeek1);
        request1.setStartTime("06:00");
        request1.setEndTime("12:00");
        request1.setDurationHours(6);

        // Act - Crear tres horarios
        System.out.println("Creando primer horario...");
        distributionScheduleService.save(request1).block();
        System.out.println("✅ Horario 1 creado con código HOR001");

        List<String> daysOfWeek2 = Arrays.asList("MARTES", "JUEVES");
        DistributionScheduleCreateRequest request2 = new DistributionScheduleCreateRequest();
        request2.setOrganizationId("org-123");
        request2.setZoneId("zone-2");
        request2.setScheduleName("Horario 2");
        request2.setDaysOfWeek(daysOfWeek2);
        request2.setStartTime("08:00");
        request2.setEndTime("14:00");
        request2.setDurationHours(6);

        List<String> daysOfWeek3 = Arrays.asList("SÁBADO", "DOMINGO");
        DistributionScheduleCreateRequest request3 = new DistributionScheduleCreateRequest();
        request3.setOrganizationId("org-123");
        request3.setZoneId("zone-3");
        request3.setScheduleName("Horario 3");
        request3.setDaysOfWeek(daysOfWeek3);
        request3.setStartTime("10:00");
        request3.setEndTime("16:00");
        request3.setDurationHours(6);

        // Assert
        System.out.println("Creando segundo horario...");
        StepVerifier.create(distributionScheduleService.save(request2))
                .assertNext(schedule -> {
                System.out.println(" - Código generado: " + schedule.getScheduleCode());
                assertEquals("HOR002", schedule.getScheduleCode());
            })
            .verifyComplete();
        System.out.println("✅ Segundo horario creado correctamente.");

        System.out.println("Creando tercer horario...");
        StepVerifier.create(distributionScheduleService.save(request3))
            .assertNext(schedule -> {
                System.out.println(" - Código generado: " + schedule.getScheduleCode());
                assertEquals("HOR003", schedule.getScheduleCode());
            })
            .verifyComplete();
        System.out.println("✅ Tercer horario creado correctamente.");

        System.out.println("✅ IT-02 completada con generación secuencial HOR001 → HOR002 → HOR003\n");
    }

    // ==================== READ TESTS ====================

    @Test
    @Order(3)
    @DisplayName("IT-03: Obtener horario por ID exitosamente")
    void getDistributionScheduleById_ShouldReturnSchedule_WhenExists() {
        System.out.println("=== IT-03: Iniciando prueba para obtener horario por ID ===");
        // Arrange
        List<String> daysOfWeek = Arrays.asList("LUNES", "MIÉRCOLES");
        
        DistributionSchedule schedule = DistributionSchedule.builder()
                .organizationId("org-123")
                .scheduleCode("HOR001")
                .zoneId("zone-1")
                .scheduleName("Horario Test")
                .daysOfWeek(daysOfWeek)
                .startTime("06:00")
                .endTime("12:00")
                .durationHours(6)
                .status(Constants.ACTIVE.name())
                .createdAt(Instant.now())
                .build();
        
        DistributionSchedule saved = distributionScheduleRepository.save(schedule).block();
        System.out.println("Horario guardado con ID: " + saved.getId());

        // Act & Assert
        StepVerifier.create(distributionScheduleService.getById(saved.getId()))
                .assertNext(result -> {
                System.out.println("✅ Horario obtenido exitosamente. Validando datos...");
                assertEquals(saved.getId(), result.getId());
                assertEquals("HOR001", result.getScheduleCode());
                assertEquals("Horario Test", result.getScheduleName());
                System.out.println(" - Código: " + result.getScheduleCode());
                System.out.println(" - Nombre: " + result.getScheduleName());
            })
            .verifyComplete();

        System.out.println("✅ IT-03 completada correctamente.\n");
    }

    @Test
    @Order(4)
    @DisplayName("IT-04: Listar todos los horarios")
    void getAllDistributionSchedules_ShouldReturnAllSchedules() {
        System.out.println("=== IT-04: Iniciando prueba para listar todos los horarios ===");
        // Arrange - Crear múltiples horarios
        List<String> daysOfWeek1 = Arrays.asList("LUNES", "MIÉRCOLES");
        List<String> daysOfWeek2 = Arrays.asList("MARTES", "JUEVES");
        
        DistributionSchedule schedule1 = DistributionSchedule.builder()
                .organizationId("org-123")
                .scheduleCode("HOR001")
                .zoneId("zone-1")
                .scheduleName("Horario 1")
                .daysOfWeek(daysOfWeek1)
                .startTime("06:00")
                .endTime("12:00")
                .durationHours(6)
                .status(Constants.ACTIVE.name())
                .createdAt(Instant.now())
                .build();

        DistributionSchedule schedule2 = DistributionSchedule.builder()
                .organizationId("org-123")
                .scheduleCode("HOR002")
                .zoneId("zone-2")
                .scheduleName("Horario 2")
                .daysOfWeek(daysOfWeek2)
                .startTime("08:00")
                .endTime("14:00")
                .durationHours(6)
                .status(Constants.INACTIVE.name())
                .createdAt(Instant.now())
                .build();

        distributionScheduleRepository.save(schedule1).block();
        distributionScheduleRepository.save(schedule2).block();
        System.out.println("Se crearon 2 horarios de prueba.");

        // Act & Assert
        StepVerifier.create(distributionScheduleService.getAll())
                .expectNextCount(2)
                .verifyComplete();

        System.out.println("✅ IT-04: Se listaron correctamente todos los horarios.\n");
    }

    @Test
    @Order(5)
    @DisplayName("IT-05: Listar solo horarios activos")
    void getAllActiveDistributionSchedules_ShouldReturnOnlyActive() {
        System.out.println("=== IT-05: Iniciando prueba para listar horarios activos ===");
        // Arrange
        List<String> daysOfWeek1 = Arrays.asList("LUNES", "MIÉRCOLES");
        List<String> daysOfWeek2 = Arrays.asList("MARTES", "JUEVES");
        
        DistributionSchedule active = DistributionSchedule.builder()
                .organizationId("org-123")
                .scheduleCode("HOR001")
                .zoneId("zone-1")
                .scheduleName("Horario Activo")
                .daysOfWeek(daysOfWeek1)
                .startTime("06:00")
                .endTime("12:00")
                .durationHours(6)
                .status(Constants.ACTIVE.name())
                .createdAt(Instant.now())
                .build();

        DistributionSchedule inactive = DistributionSchedule.builder()
                .organizationId("org-123")
                .scheduleCode("HOR002")
                .zoneId("zone-2")
                .scheduleName("Horario Inactivo")
                .daysOfWeek(daysOfWeek2)
                .startTime("08:00")
                .endTime("14:00")
                .durationHours(6)
                .status(Constants.INACTIVE.name())
                .createdAt(Instant.now())
                .build();

        distributionScheduleRepository.save(active).block();
        distributionScheduleRepository.save(inactive).block();
        System.out.println("Se insertaron horarios activos e inactivos.");

        // Act & Assert
        StepVerifier.create(distributionScheduleService.getAllActive())
                .assertNext(schedule -> {
                System.out.println("✅ Se encontró un horario activo:");
                System.out.println(" - Código: " + schedule.getScheduleCode());
                System.out.println(" - Nombre: " + schedule.getScheduleName());
                assertEquals(Constants.ACTIVE.name(), schedule.getStatus());
            })
            .verifyComplete();

        System.out.println("✅ IT-05 completada correctamente.\n");
    }

    @Test
    @Order(6)
    @DisplayName("IT-06: Listar solo horarios inactivos")
    void getAllInactiveDistributionSchedules_ShouldReturnOnlyInactive() {
        System.out.println("=== IT-06: Iniciando prueba para listar horarios inactivos ===");
        // Arrange
        List<String> daysOfWeek1 = Arrays.asList("LUNES", "MIÉRCOLES");
        List<String> daysOfWeek2 = Arrays.asList("MARTES", "JUEVES");
        
        DistributionSchedule active = DistributionSchedule.builder()
                .organizationId("org-123")
                .scheduleCode("HOR001")
                .zoneId("zone-1")
                .scheduleName("Horario Activo")
                .daysOfWeek(daysOfWeek1)
                .startTime("06:00")
                .endTime("12:00")
                .durationHours(6)
                .status(Constants.ACTIVE.name())
                .createdAt(Instant.now())
                .build();

        DistributionSchedule inactive = DistributionSchedule.builder()
                .organizationId("org-123")
                .scheduleCode("HOR002")
                .zoneId("zone-2")
                .scheduleName("Horario Inactivo")
                .daysOfWeek(daysOfWeek2)
                .startTime("08:00")
                .endTime("14:00")
                .durationHours(6)
                .status(Constants.INACTIVE.name())
                .createdAt(Instant.now())
                .build();

        distributionScheduleRepository.save(active).block();
        distributionScheduleRepository.save(inactive).block();
        System.out.println("Se insertaron horarios activos e inactivos.");

        // Act & Assert
        StepVerifier.create(distributionScheduleService.getAllInactive())
                .assertNext(schedule -> {
                System.out.println("✅ Se encontró un horario inactivo:");
                System.out.println(" - Código: " + schedule.getScheduleCode());
                System.out.println(" - Nombre: " + schedule.getScheduleName());
                assertEquals(Constants.INACTIVE.name(), schedule.getStatus());
            })
            .verifyComplete();

        System.out.println("✅ IT-06 completada correctamente.\n");
    }

    // ==================== UPDATE TESTS ====================

    @Test
    @Order(7)
    @DisplayName("IT-07: Actualizar horario exitosamente")
    void updateDistributionSchedule_ShouldUpdateFields_WhenValid() {
        System.out.println("=== IT-07: Iniciando prueba para actualizar horario ===");
        // Arrange - Crear horario inicial
        List<String> daysOfWeek = Arrays.asList("LUNES", "MIÉRCOLES");
        
        DistributionSchedule existing = DistributionSchedule.builder()
                .organizationId("org-123")
                .scheduleCode("HOR001")
                .zoneId("zone-1")
                .scheduleName("Nombre Original")
                .daysOfWeek(daysOfWeek)
                .startTime("06:00")
                .endTime("12:00")
                .durationHours(6)
                .status(Constants.ACTIVE.name())
                .createdAt(Instant.now())
                .build();

        DistributionSchedule saved = distributionScheduleRepository.save(existing).block();
        System.out.println("Horario original guardado con ID: " + saved.getId());
        System.out.println(" - Código: " + saved.getScheduleCode());
        System.out.println(" - Nombre: " + saved.getScheduleName());
        System.out.println(" - Días: " + saved.getDaysOfWeek());
        System.out.println(" - Horario: " + saved.getStartTime() + " a " + saved.getEndTime());
        System.out.println(" - Duración: " + saved.getDurationHours() + " horas");

        DistributionSchedule update = DistributionSchedule.builder()
                .scheduleName("Nombre Actualizado")
                .daysOfWeek(Arrays.asList("MARTES", "JUEVES", "VIERNES"))
                .startTime("08:00")
                .endTime("16:00")
                .durationHours(8)
                .build();
        System.out.println("Iniciando actualización de horario...");

        // Act & Assert
        StepVerifier.create(distributionScheduleService.update(saved.getId(), update))
                .assertNext(updated -> {
                System.out.println("✅ Horario actualizado correctamente. Verificando cambios...");
                System.out.println(" - Nuevo nombre: " + updated.getScheduleName());
                System.out.println(" - Nuevos días: " + updated.getDaysOfWeek());
                System.out.println(" - Nuevo horario: " + updated.getStartTime() + " a " + updated.getEndTime());
                System.out.println(" - Nueva duración: " + updated.getDurationHours() + " horas");

                assertEquals("Nombre Actualizado", updated.getScheduleName());
                assertEquals(3, updated.getDaysOfWeek().size());
                assertEquals("08:00", updated.getStartTime());
                assertEquals("16:00", updated.getEndTime());
                assertEquals(8, updated.getDurationHours());
                assertEquals("HOR001", updated.getScheduleCode()); // No cambia
            })
            .verifyComplete();

        System.out.println("✅ IT-07 completada correctamente.\n");
    }

    // ==================== ACTIVATE/DEACTIVATE TESTS ====================

    @Test
    @Order(8)
    @DisplayName("IT-08: Activar horario inactivo")
    void activateDistributionSchedule_ShouldChangeStatus_WhenInactive() {
        System.out.println("=== IT-08: Iniciando prueba para activar horario inactivo ===");
        // Arrange
        List<String> daysOfWeek = Arrays.asList("LUNES", "MIÉRCOLES");
        
        DistributionSchedule inactive = DistributionSchedule.builder()
                .organizationId("org-123")
                .scheduleCode("HOR001")
                .zoneId("zone-1")
                .scheduleName("Horario Inactivo")
                .daysOfWeek(daysOfWeek)
                .startTime("06:00")
                .endTime("12:00")
                .durationHours(6)
                .status(Constants.INACTIVE.name())
                .createdAt(Instant.now())
                .build();

        DistributionSchedule saved = distributionScheduleRepository.save(inactive).block();
        System.out.println("Horario inicial guardado:");
        System.out.println(" - ID: " + saved.getId());
        System.out.println(" - Código: " + saved.getScheduleCode());
        System.out.println(" - Estado inicial: " + saved.getStatus());
        System.out.println("Intentando activar el horario...");

        // Act & Assert
        StepVerifier.create(distributionScheduleService.activate(saved.getId()))
                .assertNext(activated -> {
                System.out.println("✅ Horario activado correctamente:");
                System.out.println(" - ID: " + activated.getId());
                System.out.println(" - Nuevo estado: " + activated.getStatus());

                assertEquals(Constants.ACTIVE.name(), activated.getStatus());
            })
            .verifyComplete();

        System.out.println("✅ IT-08 completada correctamente.\n");
    }

    @Test
    @Order(9)
    @DisplayName("IT-09: Desactivar horario activo")
    void deactivateDistributionSchedule_ShouldChangeStatus_WhenActive() {
        System.out.println("=== IT-09: Iniciando prueba para desactivar horario activo ===");
        // Arrange
        List<String> daysOfWeek = Arrays.asList("LUNES", "MIÉRCOLES");
        
        DistributionSchedule active = DistributionSchedule.builder()
                .organizationId("org-123")
                .scheduleCode("HOR001")
                .zoneId("zone-1")
                .scheduleName("Horario Activo")
                .daysOfWeek(daysOfWeek)
                .startTime("06:00")
                .endTime("12:00")
                .durationHours(6)
                .status(Constants.ACTIVE.name())
                .createdAt(Instant.now())
                .build();

        DistributionSchedule saved = distributionScheduleRepository.save(active).block();
        System.out.println("Horario inicial guardado:");
        System.out.println(" - ID: " + saved.getId());
        System.out.println(" - Código: " + saved.getScheduleCode());
        System.out.println(" - Estado inicial: " + saved.getStatus());
        System.out.println("Intentando desactivar el horario...");

        // Act & Assert
        StepVerifier.create(distributionScheduleService.deactivate(saved.getId()))
                .assertNext(deactivated -> {
                System.out.println("✅ Horario desactivado correctamente:");
                System.out.println(" - ID: " + deactivated.getId());
                System.out.println(" - Nuevo estado: " + deactivated.getStatus());

                assertEquals(Constants.INACTIVE.name(), deactivated.getStatus());
            })
            .verifyComplete();

        System.out.println("✅ IT-09 completada correctamente.\n");
    }

    // ==================== DELETE TESTS ====================

    @Test
    @Order(10)
    @DisplayName("IT-10: Eliminar horario exitosamente")
    void deleteDistributionSchedule_ShouldRemove_WhenExists() {
        System.out.println("=== IT-10: Iniciando prueba para eliminar horario existente ===");
        // Arrange
        List<String> daysOfWeek = Arrays.asList("LUNES", "MIÉRCOLES");
        
        DistributionSchedule schedule = DistributionSchedule.builder()
                .organizationId("org-123")
                .scheduleCode("HOR001")
                .zoneId("zone-1")
                .scheduleName("Horario a Eliminar")
                .daysOfWeek(daysOfWeek)
                .startTime("06:00")
                .endTime("12:00")
                .durationHours(6)
                .status(Constants.ACTIVE.name())
                .createdAt(Instant.now())
                .build();

        DistributionSchedule saved = distributionScheduleRepository.save(schedule).block();
        System.out.println("Horario creado exitosamente:");
        System.out.println(" - ID: " + saved.getId());
        System.out.println(" - Código: " + saved.getScheduleCode());
        System.out.println(" - Nombre: " + saved.getScheduleName());
        System.out.println(" - Estado: " + saved.getStatus());
        System.out.println("Intentando eliminar el horario...");

        // Act
        StepVerifier.create(distributionScheduleService.delete(saved.getId()))
                .then(() -> System.out.println("✅ Llamada a servicio delete() completada."))
            .verifyComplete();

        System.out.println("Verificando que el horario fue eliminado de la base de datos...");

        // Assert - Verificar que no existe
        StepVerifier.create(distributionScheduleRepository.findById(saved.getId()))
                .expectNextCount(0)
                .verifyComplete();

        System.out.println("✅ Verificación completada: el horario fue eliminado correctamente.");
        System.out.println("✅ IT-10 completada con éxito.\n");
    }
}