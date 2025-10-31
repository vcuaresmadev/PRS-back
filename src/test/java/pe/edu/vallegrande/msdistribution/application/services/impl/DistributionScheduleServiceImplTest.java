package pe.edu.vallegrande.msdistribution.application.services.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import pe.edu.vallegrande.msdistribution.domain.enums.Constants;
import pe.edu.vallegrande.msdistribution.domain.models.DistributionSchedule;
import pe.edu.vallegrande.msdistribution.infrastructure.dto.request.DistributionScheduleCreateRequest;
import pe.edu.vallegrande.msdistribution.infrastructure.exception.CustomException;
import pe.edu.vallegrande.msdistribution.infrastructure.repository.DistributionScheduleRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * 🔹 Clase de pruebas unitarias para DistributionScheduleServiceImpl
 * Se usa Mockito para simular el repositorio y StepVerifier para validar flujos reactivos (Mono / Flux)
 */
public class DistributionScheduleServiceImplTest {

    // Se simula el repositorio (no se conecta a base de datos)
    @Mock
    private DistributionScheduleRepository scheduleRepository;

    // Se inyecta el mock dentro del servicio que se probará
    @InjectMocks
    private DistributionScheduleServiceImpl scheduleService;

    // Antes de cada prueba se inicializan los mocks
    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    // ================================================================
    // ✅ Caso positivo: crear horario correctamente
    // ================================================================
    @Test
    void save_ShouldCreateSchedule_WhenRequestIsValid() {
        System.out.println("Starting test: Creating valid schedule");
        
        // Arrange - Build the request
        // Se crea la lista de días
        List<String> daysOfWeek = Arrays.asList("LUNES", "MIÉRCOLES", "VIERNES");
        
        // Se construye la solicitud (request) con los datos del horario
        DistributionScheduleCreateRequest request = new DistributionScheduleCreateRequest();
        request.setOrganizationId("org-1");
        request.setZoneId("zone-1");
        request.setScheduleName("Horario Zona Centro");
        request.setDaysOfWeek(daysOfWeek);
        request.setStartTime("06:00");
        request.setEndTime("12:00");
        request.setDurationHours(6);

        // Se simula que no hay horarios previos → generará el código HOR001
        when(scheduleRepository.findTopByOrderByScheduleCodeDesc()).thenReturn(Mono.empty());
        when(scheduleRepository.existsByScheduleCode("HOR001")).thenReturn(Mono.just(false));

        // Capturamos el objeto que se guardará en la base (para validarlo después)
        ArgumentCaptor<DistributionSchedule> scheduleCaptor = ArgumentCaptor.forClass(DistributionSchedule.class);

       // Simulamos el objeto que devuelve el repositorio al guardar
        DistributionSchedule savedSchedule = DistributionSchedule.builder()
                .id("schedule-1")
                .organizationId("org-1")
                .scheduleCode("HOR001")
                .zoneId("zone-1")
                .scheduleName("Horario Zona Centro")
                .daysOfWeek(daysOfWeek)
                .startTime("06:00")
                .endTime("12:00")
                .durationHours(6)
                .status(Constants.ACTIVE.name())
                .createdAt(Instant.now())
                .build();

        when(scheduleRepository.save(any(DistributionSchedule.class))).thenReturn(Mono.just(savedSchedule));

        // Act & Assert 
        // Ejecutamos el método del servicio y verificamos el resultado
        StepVerifier.create(scheduleService.save(request))
                .assertNext(response -> {
                    System.out.println("Schedule created correctly with code: " + response.getScheduleCode());
                    assertNotNull(response);
                    assertEquals("schedule-1", response.getId());
                    assertEquals("org-1", response.getOrganizationId());
                    assertEquals("HOR001", response.getScheduleCode());
                    assertEquals("zone-1", response.getZoneId());
                    assertEquals("Horario Zona Centro", response.getScheduleName());
                    assertEquals(3, response.getDaysOfWeek().size());
                    assertEquals("LUNES", response.getDaysOfWeek().get(0));
                    assertEquals("MIÉRCOLES", response.getDaysOfWeek().get(1));
                    assertEquals("VIERNES", response.getDaysOfWeek().get(2));
                    assertEquals("06:00", response.getStartTime());
                    assertEquals("12:00", response.getEndTime());
                    assertEquals(6, response.getDurationHours());
                    assertEquals(Constants.ACTIVE.name(), response.getStatus());
                    assertNotNull(response.getCreatedAt());
                })
                .verifyComplete();

        // Verificamos que los métodos del repositorio fueron llamados correctamente
        verify(scheduleRepository).findTopByOrderByScheduleCodeDesc();
        verify(scheduleRepository).existsByScheduleCode("HOR001");
        verify(scheduleRepository).save(scheduleCaptor.capture());

        // Validamos lo que se envió a guardar
        DistributionSchedule scheduleToSave = scheduleCaptor.getValue();
        System.out.println("Data sent to repository:");
        System.out.println("   Organization: " + scheduleToSave.getOrganizationId());
        System.out.println("   Code: " + scheduleToSave.getScheduleCode());
        System.out.println("   Zone: " + scheduleToSave.getZoneId());
        System.out.println("   Name: " + scheduleToSave.getScheduleName());
        System.out.println("   Days: " + scheduleToSave.getDaysOfWeek());
        System.out.println("   Start time: " + scheduleToSave.getStartTime());
        System.out.println("   End time: " + scheduleToSave.getEndTime());
        System.out.println("   Duration: " + scheduleToSave.getDurationHours());
        System.out.println("   Status: " + scheduleToSave.getStatus());

        assertEquals("org-1", scheduleToSave.getOrganizationId());
        assertEquals("HOR001", scheduleToSave.getScheduleCode());
        assertEquals("zone-1", scheduleToSave.getZoneId());
        assertEquals("Horario Zona Centro", scheduleToSave.getScheduleName());
        assertEquals(3, scheduleToSave.getDaysOfWeek().size());
        assertEquals("06:00", scheduleToSave.getStartTime());
        assertEquals("12:00", scheduleToSave.getEndTime());
        assertEquals(6, scheduleToSave.getDurationHours());
        assertEquals(Constants.ACTIVE.name(), scheduleToSave.getStatus());

        System.out.println("Test completed successfully\n");
    }

    // ================================================================
    // ✅ Caso positivo: genera el siguiente código secuencial (HOR008)
    // ================================================================
    @Test
    void save_ShouldGenerateNextScheduleCode_WhenPreviousSchedulesExist() {
        System.out.println("Starting test: Sequential code generation");
        
        // Arrange 
        // Se simula que ya existe el código HOR007
        DistributionSchedule existingSchedule = DistributionSchedule.builder()
                .scheduleCode("HOR007")
                .build();

        // Se construye el nuevo request
        DistributionScheduleCreateRequest request = new DistributionScheduleCreateRequest();
        request.setOrganizationId("org-1");
        request.setZoneId("zone-1");
        request.setScheduleName("Nuevo Horario");
        request.setDaysOfWeek(Arrays.asList("LUNES", "MARTES"));
        request.setStartTime("08:00");
        request.setEndTime("16:00");
        request.setDurationHours(8);

        // Mock del comportamiento
        when(scheduleRepository.findTopByOrderByScheduleCodeDesc()).thenReturn(Mono.just(existingSchedule));
        when(scheduleRepository.existsByScheduleCode("HOR008")).thenReturn(Mono.just(false));
        when(scheduleRepository.save(any(DistributionSchedule.class))).thenReturn(Mono.just(
                DistributionSchedule.builder().id("schedule-2").scheduleCode("HOR008").build()
        ));

        // Act & Assert
        // Se verifica que el nuevo código generado sea HOR008
        StepVerifier.create(scheduleService.save(request))
                .assertNext(response -> {
                    assertEquals("HOR008", response.getScheduleCode());
                    System.out.println("Code generated correctly: " + response.getScheduleCode());
                })
                .verifyComplete();

        System.out.println("Code generation test completed\n");
    }

    // ================================================================
    // ❌ Caso negativo: el código ya existe
    // ================================================================
    @Test
    void save_ShouldReturnError_WhenScheduleCodeAlreadyExists() {
        System.out.println("Starting negative test: Schedule code already exists");

        // Arrange - Se construye el request
        DistributionScheduleCreateRequest request = new DistributionScheduleCreateRequest();
        request.setOrganizationId("org-1");
        request.setZoneId("zone-1");
        request.setScheduleName("Horario Duplicado");
        request.setDaysOfWeek(Arrays.asList("LUNES"));
        request.setStartTime("09:00");
        request.setEndTime("17:00");
        request.setDurationHours(8);

        // Mock: el código HOR001 ya existe
        when(scheduleRepository.findTopByOrderByScheduleCodeDesc()).thenReturn(Mono.empty());
        when(scheduleRepository.existsByScheduleCode("HOR001")).thenReturn(Mono.just(true));

        // Act & Assert - Verificamos que se lanza la excepción esperada
        StepVerifier.create(scheduleService.save(request))
                .expectErrorSatisfies(error -> {
                    assertTrue(error instanceof CustomException);
                    CustomException ce = (CustomException) error;
                    assertEquals("Schedule code already exists", ce.getErrorMessage().getMessage());
                    System.out.println("Expected error: " + ce.getMessage());
                })
                .verify();

        // Se valida que no se intentó guardar nada
        verify(scheduleRepository, never()).save(any(DistributionSchedule.class));
        System.out.println("Negative test completed successfully\n");
    }

    // ================================================================
    // ❌ Caso negativo: error en la base de datos al guardar
    // ================================================================
    @Test
    void save_ShouldReturnError_WhenRepositoryFails() {
        System.out.println("Starting negative test: Repository failure when saving schedule");

        // Arrange
        DistributionScheduleCreateRequest request = new DistributionScheduleCreateRequest();
        request.setOrganizationId("org-1");
        request.setZoneId("zone-1");
        request.setScheduleName("Horario de Prueba");
        request.setDaysOfWeek(Arrays.asList("LUNES"));
        request.setStartTime("08:00");
        request.setEndTime("16:00");
        request.setDurationHours(8);

        // Mock de error simulado
        when(scheduleRepository.findTopByOrderByScheduleCodeDesc()).thenReturn(Mono.empty());
        when(scheduleRepository.existsByScheduleCode("HOR001")).thenReturn(Mono.just(false));
        when(scheduleRepository.save(any(DistributionSchedule.class)))
                .thenReturn(Mono.error(new RuntimeException("Database error")));

        // Act & Assert - Verificamos que lanza la excepción
        StepVerifier.create(scheduleService.save(request))
                .expectErrorSatisfies(error -> {
                    assertTrue(error instanceof RuntimeException);
                    assertEquals("Database error", error.getMessage());
                    System.out.println("Expected error: " + error.getMessage());
                })
                .verify();

        System.out.println("Negative test completed successfully\n");
    }

    // ================================================================
    // ✅ Activación de un horario existente
    // ================================================================
    @Test
    void activate_ShouldActivateSchedule_WhenScheduleExists() {
        System.out.println("Starting test: Schedule activation");

        // Arrange - Mock de un horario inactivo
        String scheduleId = "schedule-1";
        DistributionSchedule existingSchedule = DistributionSchedule.builder()
                .id(scheduleId)
                .status(Constants.INACTIVE.name())
                .build();

        when(scheduleRepository.findById(scheduleId)).thenReturn(Mono.just(existingSchedule));
        when(scheduleRepository.save(any(DistributionSchedule.class))).thenReturn(Mono.just(
                DistributionSchedule.builder().id(scheduleId).status(Constants.ACTIVE.name()).build()
        ));

        // Act & Assert - Se verifica que cambie a ACTIVO
        StepVerifier.create(scheduleService.activate(scheduleId))
                .assertNext(schedule -> {
                    assertEquals(Constants.ACTIVE.name(), schedule.getStatus());
                    System.out.println("Schedule activated correctly");
                })
                .verifyComplete();

        System.out.println("Activation test completed\n");
    }

    // ================================================================
    // ✅ Desactivación de un horario existente
    // ================================================================
    @Test
    void deactivate_ShouldDeactivateSchedule_WhenScheduleExists() {
        System.out.println("Starting test: Schedule deactivation");

        // Arrange - Mock de un horario activo
        String scheduleId = "schedule-1";
        DistributionSchedule existingSchedule = DistributionSchedule.builder()
                .id(scheduleId)
                .status(Constants.ACTIVE.name())
                .build();

        when(scheduleRepository.findById(scheduleId)).thenReturn(Mono.just(existingSchedule));
        when(scheduleRepository.save(any(DistributionSchedule.class))).thenReturn(Mono.just(
                DistributionSchedule.builder().id(scheduleId).status(Constants.INACTIVE.name()).build()
        ));

        // Act & Assert
        StepVerifier.create(scheduleService.deactivate(scheduleId))
                .assertNext(schedule -> {
                    assertEquals(Constants.INACTIVE.name(), schedule.getStatus());
                    System.out.println("Schedule deactivated correctly");
                })
                .verifyComplete();

        System.out.println("Deactivation test completed\n");
    }

    // ================================================================
    // ❌ Activación de horario inexistente
    // ================================================================
    @Test
    void activate_ShouldReturnError_WhenScheduleNotFound() {
        System.out.println("Starting negative test: Activation of non-existent schedule");

        // Arrange
        String scheduleId = "schedule-inexistente";
        when(scheduleRepository.findById(scheduleId)).thenReturn(Mono.empty());

        // Act & Assert
        StepVerifier.create(scheduleService.activate(scheduleId))
                .expectErrorSatisfies(error -> {
                    assertTrue(error instanceof CustomException);
                    CustomException ce = (CustomException) error;
                    assertEquals("Schedule not found", ce.getErrorMessage().getMessage());
                    System.out.println("Expected error: " + ce.getMessage());
                })
                .verify();

        System.out.println("Negative test completed successfully\n");
    }

    // ================================================================
    // 🔹 Validación con distintos días de la semana
    // ================================================================
    @Test
    void save_ShouldHandleDifferentDayConfigurations_WhenRequestIsValid() {
        System.out.println("Starting test: Different day configurations");
        
        // Arrange: se crea un horario que aplica solo para fin de semana
        List<String> weekendDays = Arrays.asList("SÁBADO", "DOMINGO");
        
        DistributionScheduleCreateRequest request = new DistributionScheduleCreateRequest();
        request.setOrganizationId("org-1");
        request.setZoneId("zone-2");
        request.setScheduleName("Horario Fin de Semana");
        request.setDaysOfWeek(weekendDays);
        request.setStartTime("10:00");
        request.setEndTime("18:00");
        request.setDurationHours(8);

        // Simulamos que no hay códigos previos y se generará HOR001
        when(scheduleRepository.findTopByOrderByScheduleCodeDesc()).thenReturn(Mono.empty());
        when(scheduleRepository.existsByScheduleCode("HOR001")).thenReturn(Mono.just(false));

        // Simulamos guardado exitoso del horario
        when(scheduleRepository.save(any(DistributionSchedule.class))).thenReturn(Mono.just(
                DistributionSchedule.builder()
                        .id("schedule-weekend")
                        .scheduleCode("HOR001")
                        .daysOfWeek(weekendDays)
                        .build()
        ));

        // Act & Assert: verificamos que se guarden los días correctamente
        StepVerifier.create(scheduleService.save(request))
                .assertNext(response -> {
                    assertEquals(2, response.getDaysOfWeek().size());
                    assertEquals("SÁBADO", response.getDaysOfWeek().get(0));
                    assertEquals("DOMINGO", response.getDaysOfWeek().get(1));
                    System.out.println("Weekend schedule created correctly");
                })
                .verifyComplete();

        System.out.println("Day configuration test completed\n");
    }

    // ================================================================
    // ✅ Listado de todos los horarios existentes
    // ================================================================
    @Test
    void getAll_ShouldReturnAllSchedules_WhenCalled() {
        System.out.println("Starting test: Get all schedules");
        
        // Arrange - simulamos 2 horarios (uno activo y uno inactivo)
        List<DistributionSchedule> schedules = Arrays.asList(
            DistributionSchedule.builder()
                .id("schedule-1")
                .scheduleCode("HOR001")
                .scheduleName("Horario Matutino")
                .status(Constants.ACTIVE.name())
                .build(),
            DistributionSchedule.builder()
                .id("schedule-2")
                .scheduleCode("HOR002")
                .scheduleName("Horario Vespertino")
                .status(Constants.INACTIVE.name())
                .build()
        );

        when(scheduleRepository.findAll()).thenReturn(Flux.fromIterable(schedules));

        // Act & Assert - verificamos que se obtengan ambos
        StepVerifier.create(scheduleService.getAll())
                .assertNext(schedule -> {
                    assertEquals("schedule-1", schedule.getId());
                    assertEquals("HOR001", schedule.getScheduleCode());
                    assertEquals("Horario Matutino", schedule.getScheduleName());
                    assertEquals(Constants.ACTIVE.name(), schedule.getStatus());
                    System.out.println("First schedule obtained correctly");
                })
                .assertNext(schedule -> {
                    assertEquals("schedule-2", schedule.getId());
                    assertEquals("HOR002", schedule.getScheduleCode());
                    assertEquals("Horario Vespertino", schedule.getScheduleName());
                    assertEquals(Constants.INACTIVE.name(), schedule.getStatus());
                    System.out.println("Second schedule obtained correctly");
                })
                .verifyComplete();

        verify(scheduleRepository).findAll();
        System.out.println("Get all schedules test completed\n");
    }

    // ================================================================
    // ✅ Listado de los horarios activos
    // ================================================================
    @Test
    void getAllActive_ShouldReturnActiveSchedules_WhenCalled() {
        System.out.println("Starting test: Get active schedules");
        
        // Arrange
        List<DistributionSchedule> activeSchedules = Arrays.asList(
            DistributionSchedule.builder()
                .id("schedule-1")
                .scheduleCode("HOR001")
                .scheduleName("Horario Matutino")
                .status(Constants.ACTIVE.name())
                .build(),
            DistributionSchedule.builder()
                .id("schedule-3")
                .scheduleCode("HOR003")
                .scheduleName("Horario Nocturno")
                .status(Constants.ACTIVE.name())
                .build()
        );

        when(scheduleRepository.findAllByStatus(Constants.ACTIVE.name()))
            .thenReturn(Flux.fromIterable(activeSchedules));

        // Act & Assert
        StepVerifier.create(scheduleService.getAllActive())
                .assertNext(schedule -> {
                    assertEquals("schedule-1", schedule.getId());
                    assertEquals(Constants.ACTIVE.name(), schedule.getStatus());
                    System.out.println("First active schedule obtained correctly");
                })
                .assertNext(schedule -> {
                    assertEquals("schedule-3", schedule.getId());
                    assertEquals(Constants.ACTIVE.name(), schedule.getStatus());
                    System.out.println("Second active schedule obtained correctly");
                })
                .verifyComplete();

        verify(scheduleRepository).findAllByStatus(Constants.ACTIVE.name());
        System.out.println("Get active schedules test completed\n");
    }

    // ================================================================
    // ✅ Listado de los horarios inactivos
    // ================================================================
    @Test
    void getAllInactive_ShouldReturnInactiveSchedules_WhenCalled() {
        System.out.println("Starting test: Get inactive schedules");
        
        // Arrange
        List<DistributionSchedule> inactiveSchedules = Arrays.asList(
            DistributionSchedule.builder()
                .id("schedule-2")
                .scheduleCode("HOR002")
                .scheduleName("Horario Vespertino")
                .status(Constants.INACTIVE.name())
                .build()
        );

        when(scheduleRepository.findAllByStatus(Constants.INACTIVE.name()))
            .thenReturn(Flux.fromIterable(inactiveSchedules));

        // Act & Assert
        StepVerifier.create(scheduleService.getAllInactive())
                .assertNext(schedule -> {
                    assertEquals("schedule-2", schedule.getId());
                    assertEquals(Constants.INACTIVE.name(), schedule.getStatus());
                    System.out.println("Inactive schedule obtained correctly");
                })
                .verifyComplete();

        verify(scheduleRepository).findAllByStatus(Constants.INACTIVE.name());
        System.out.println("Get inactive schedules test completed\n");
    }

    // ================================================================
    // ✅ Listado de un horario por ID existente
    // ================================================================
    @Test
    void getById_ShouldReturnSchedule_WhenScheduleExists() {
        System.out.println("Starting test: Get schedule by existing ID");
        
        // Arrange
        String scheduleId = "schedule-1";
        DistributionSchedule schedule = DistributionSchedule.builder()
                .id(scheduleId)
                .scheduleCode("HOR001")
                .scheduleName("Horario Matutino")
                .status(Constants.ACTIVE.name())
                .build();

        when(scheduleRepository.findById(scheduleId)).thenReturn(Mono.just(schedule));

        // Act & Assert
        StepVerifier.create(scheduleService.getById(scheduleId))
                .assertNext(foundSchedule -> {
                    assertEquals(scheduleId, foundSchedule.getId());
                    assertEquals("HOR001", foundSchedule.getScheduleCode());
                    assertEquals("Horario Matutino", foundSchedule.getScheduleName());
                    assertEquals(Constants.ACTIVE.name(), foundSchedule.getStatus());
                    System.out.println("Schedule found correctly");
                })
                .verifyComplete();

        verify(scheduleRepository).findById(scheduleId);
        System.out.println("Get schedule by ID test completed\n");
    }

    // ================================================================
    // ❌ Error si el horario no existe
    // ================================================================
    @Test
    void getById_ShouldReturnError_WhenScheduleNotFound() {
        System.out.println("Starting negative test: Get non-existent schedule");
        
        // Arrange
        String scheduleId = "schedule-inexistente";
        when(scheduleRepository.findById(scheduleId)).thenReturn(Mono.empty());

        // Act & Assert
        StepVerifier.create(scheduleService.getById(scheduleId))
                .expectErrorSatisfies(error -> {
                    assertTrue(error instanceof CustomException);
                    CustomException ce = (CustomException) error;
                    assertEquals("Schedule not found", ce.getErrorMessage().getMessage());
                    System.out.println("Expected error: " + ce.getMessage());
                })
                .verify();

        verify(scheduleRepository).findById(scheduleId);
        System.out.println("Negative test completed successfully\n");
    }

    // ================================================================
    // ✅ Eliminado correctamente de un horario existente
    // ================================================================
    @Test
    void delete_ShouldDeleteSchedule_WhenScheduleExists() {
        System.out.println("Starting test: Delete existing schedule");
        
        // Arrange
        String scheduleId = "schedule-1";
        DistributionSchedule schedule = DistributionSchedule.builder()
                .id(scheduleId)
                .scheduleCode("HOR001")
                .scheduleName("Horario a Eliminar")
                .status(Constants.ACTIVE.name())
                .build();

        when(scheduleRepository.findById(scheduleId)).thenReturn(Mono.just(schedule));
        when(scheduleRepository.delete(any(DistributionSchedule.class))).thenReturn(Mono.empty());

        // Act & Assert
        StepVerifier.create(scheduleService.delete(scheduleId))
                .verifyComplete();

        verify(scheduleRepository).findById(scheduleId);
        verify(scheduleRepository).delete(any(DistributionSchedule.class));
        System.out.println("Schedule deleted correctly");
        System.out.println("Deletion test completed\n");
    }

    // ================================================================
    // ❌ No permite eliminar si el horario no existe
    // ================================================================
    @Test
    void delete_ShouldReturnError_WhenScheduleNotFound() {
        System.out.println("Starting negative test: Delete non-existent schedule");
        
        // Arrange
        String scheduleId = "schedule-inexistente";
        when(scheduleRepository.findById(scheduleId)).thenReturn(Mono.empty());

        // Act & Assert
        StepVerifier.create(scheduleService.delete(scheduleId))
                .expectErrorSatisfies(error -> {
                    assertTrue(error instanceof CustomException);
                    CustomException ce = (CustomException) error;
                    assertEquals("Schedule not found", ce.getErrorMessage().getMessage());
                    System.out.println("Expected error: " + ce.getMessage());
                })
                .verify();

        verify(scheduleRepository).findById(scheduleId);
        verify(scheduleRepository, never()).delete(any(DistributionSchedule.class));
        System.out.println("Negative deletion test completed\n");
    }

    // ================================================================
    // ❌ Desactivar el horario si no existe
    // ================================================================
    @Test
    void deactivate_ShouldReturnError_WhenScheduleNotFound() {
        System.out.println("Starting negative test: Deactivate non-existent schedule");

        // Arrange
        String scheduleId = "schedule-inexistente";
        when(scheduleRepository.findById(scheduleId)).thenReturn(Mono.empty());

        // Act & Assert
        StepVerifier.create(scheduleService.deactivate(scheduleId))
                .expectErrorSatisfies(error -> {
                    assertTrue(error instanceof CustomException);
                    CustomException ce = (CustomException) error;
                    assertEquals("Schedule not found", ce.getErrorMessage().getMessage());
                    System.out.println("Expected error: " + ce.getMessage());
                })
                .verify();

        System.out.println("Negative deactivation test completed\n");
    }

    // ================================================================
    // 🔹 Validación con errores de formato al generar el código
    // ================================================================
    @Test
    void generateNextScheduleCode_ShouldHandleParsingError_WhenInvalidCodeFormat() {
        System.out.println("Starting test: Handling parsing error");
        
        // Arrange - Se simula un código previo con formato inválido
        DistributionSchedule existingSchedule = DistributionSchedule.builder()
                .scheduleCode("HORINVALID")
                .build();

        DistributionScheduleCreateRequest request = new DistributionScheduleCreateRequest();
        request.setOrganizationId("org-1");
        request.setZoneId("zone-1");
        request.setScheduleName("Horario de Prueba");
        request.setDaysOfWeek(Arrays.asList("LUNES"));
        request.setStartTime("08:00");
        request.setEndTime("16:00");
        request.setDurationHours(8);

         // Simulaciones del repositorio
        when(scheduleRepository.findTopByOrderByScheduleCodeDesc()).thenReturn(Mono.just(existingSchedule));
        when(scheduleRepository.existsByScheduleCode("HOR001")).thenReturn(Mono.just(false));
        when(scheduleRepository.save(any(DistributionSchedule.class))).thenReturn(Mono.just(
                DistributionSchedule.builder().id("schedule-1").scheduleCode("HOR001").build()
        ));

        // Act & Assert
        StepVerifier.create(scheduleService.save(request))
                .assertNext(response -> {
                    assertEquals("HOR001", response.getScheduleCode());
                    System.out.println("Code generated correctly after parsing error: " + response.getScheduleCode());
                })
                .verifyComplete();

        System.out.println("Parsing error handling test completed\n");
    }
}