package pe.edu.vallegrande.msdistribution.application.services.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import pe.edu.vallegrande.msdistribution.domain.enums.Constants;
import pe.edu.vallegrande.msdistribution.domain.models.Fare;
import pe.edu.vallegrande.msdistribution.infrastructure.dto.request.FareCreateRequest;
import pe.edu.vallegrande.msdistribution.infrastructure.exception.CustomException;
import pe.edu.vallegrande.msdistribution.infrastructure.repository.FareRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Clase de pruebas unitarias para FareServiceImpl.
 * 
 * Se utiliza JUnit 5 + Mockito + Reactor Test.
 * - JUnit: para definir y ejecutar las pruebas.
 * - Mockito: para simular el comportamiento del repositorio (FareRepository).
 * - StepVerifier: para probar flujos reactivos (Mono y Flux).
 */
public class FareServiceImplTest {

    // Se simula el repositorio de tarifas (no se conecta a base de datos real)
    @Mock
    private FareRepository fareRepository;

    // Se inyecta el mock dentro del servicio a probar
    @InjectMocks
    private FareServiceImpl fareService;

    /**
     * Inicializa los mocks antes de cada prueba.
     */
    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    // ============================================================
    // 🔹 TEST: Buscar tarifa por ID inexistente
    // ============================================================
    @Test
    void getByIdF_ShouldError_WhenNotFound() {
        // Arrange
        String id = "fare-404";
        // Simula que no existe ninguna tarifa con ese ID
        when(fareRepository.findById(id)).thenReturn(Mono.empty());

        // Act & Assert
        // Se verifica que el servicio lance un CustomException con el mensaje esperado
        StepVerifier.create(fareService.getByIdF(id))
            .expectErrorSatisfies(err -> {
                assertTrue(err instanceof CustomException);
                CustomException ce = (CustomException) err;
                assertEquals("Fare not found", ce.getErrorMessage().getMessage());
            })
            .verify();
    }

    // ============================================================
    // 🔹 TEST: Actualización de tarifa con datos válidos
    // ============================================================
    @Test
    void updateF_ShouldUpdateFields_WhenRequestValid() {
        // Arrange
        // Datos iniciales (tarifa existente)
        String id = "fare-1";
        Fare existing = Fare.builder()
                .id(id)
                .organizationId("org-1")
                .fareCode("TAR001")
                .fareName("Old Name")
                .fareType("SEMANAL")
                .fareAmount(new BigDecimal("10"))
                .status(Constants.ACTIVE.name())
                .createdAt(Instant.now())
                .effectiveDate(new java.util.Date()) // Fix: Add effectiveDate
                .build();

        // Nuevos datos que llegan desde el request
        FareCreateRequest update = FareCreateRequest.builder()
                .fareName("New Name")
                .fareType("MENSUAL")
                .fareAmount(new BigDecimal("20"))
                .build();

        // Tarifa esperada después de guardar los cambios
        Fare saved = Fare.builder()
                .id(id)
                .organizationId(existing.getOrganizationId())
                .fareCode(existing.getFareCode())
                .fareName("New Name")
                .fareType("MENSUAL")
                .fareAmount(new BigDecimal("20"))
                .status(existing.getStatus())
                .createdAt(existing.getCreatedAt())
                .effectiveDate(existing.getEffectiveDate()) // Fix: Add effectiveDate
                .build();

        // Configuración del mock: primero encuentra, luego guarda
        when(fareRepository.findById(id)).thenReturn(Mono.just(existing));
        // Fix: Mock findAllByStatus to return empty Flux to avoid null pointer
        when(fareRepository.findAllByStatus(anyString())).thenReturn(Flux.empty());
        when(fareRepository.save(any(Fare.class))).thenReturn(Mono.just(saved));

        // Act & Assert
        // Verificación del resultado
        StepVerifier.create(fareService.updateF(id, update))
            .assertNext(result -> {
                assertEquals("New Name", result.getFareName());
                assertEquals("MENSUAL", result.getFareType());
                assertEquals(new BigDecimal("20"), result.getFareAmount());
            })
            .verifyComplete();
    }

    // ============================================================
    // 🔹 TEST: Eliminar tarifa inexistente
    // ============================================================
    @Test
    void deleteF_ShouldError_WhenNotFound() {
        // Arrange
        String id = "fare-404";
        when(fareRepository.findById(id)).thenReturn(Mono.empty());

        // Act & Assert
        StepVerifier.create(fareService.deleteF(id))
            .expectErrorSatisfies(err -> {
                assertTrue(err instanceof CustomException);
                CustomException ce = (CustomException) err;
                assertEquals("Fare not found", ce.getErrorMessage().getMessage());
            })
            .verify();
    }

    // ============================================================
    // 🔹 TEST: Eliminar tarifa existente correctamente
    // ============================================================
    @Test
    void deleteF_ShouldComplete_WhenExists() {
        // Arrange
        String id = "fare-1";
        Fare existing = Fare.builder().id(id).status(Constants.ACTIVE.name()).build();
        when(fareRepository.findById(id)).thenReturn(Mono.just(existing));
        when(fareRepository.delete(existing)).thenReturn(Mono.empty());

        // Act & Assert
        StepVerifier.create(fareService.deleteF(id))
            .verifyComplete();
    }

    // ============================================================
    // 🔹 TEST: Activar tarifa (sin cambios) - should throw conflict error
    // ============================================================
    @Test
    void activateF_ShouldError_WhenSameStatus() {
        // Arrange
        String id = "fare-1";
        Fare existing = Fare.builder().id(id).status(Constants.ACTIVE.name()).build();
        when(fareRepository.findById(id)).thenReturn(Mono.just(existing));

        // Act & Assert
        StepVerifier.create(fareService.activateF(id))
            .expectErrorSatisfies(err -> {
                assertTrue(err instanceof CustomException);
                CustomException ce = (CustomException) err;
                assertEquals("Conflicto", ce.getErrorMessage().getMessage());
            })
            .verify();
    }

    // ============================================================
    // 🔹 TEST: Activar tarifa inactiva (cambia de estado)
    // ============================================================
    @Test
    void activateF_ShouldPersist_WhenDifferentStatus() {
        // Arrange
        String id = "fare-1";
        Fare existing = Fare.builder().id(id).status(Constants.INACTIVE.name()).build();
        Fare saved = Fare.builder()
                .id(id)
                .status(Constants.ACTIVE.name())
                .build();

        when(fareRepository.findById(id)).thenReturn(Mono.just(existing));
        // Fix: Mock findAllByStatus to return empty Flux to avoid null pointer
        when(fareRepository.findAllByStatus(anyString())).thenReturn(Flux.empty());
        when(fareRepository.save(any(Fare.class))).thenReturn(Mono.just(saved));

        // Act & Assert
        StepVerifier.create(fareService.activateF(id))
            .assertNext(result -> assertEquals(Constants.ACTIVE.name(), result.getStatus()))
            .verifyComplete();
    }

    // ============================================================
    // 🔹 TEST: Generar código secuencial al guardar nueva tarifa
    // ============================================================
    @Test
    void saveF_ShouldGenerateSequentialCode_FromLastFare() {
        // Arrange: último código TAR099 -> siguiente TAR100
        Fare last = Fare.builder().fareCode("TAR099").build();
        when(fareRepository.findTopByOrderByFareCodeDesc()).thenReturn(Mono.just(last));
        when(fareRepository.existsByFareCode("TAR100")).thenReturn(Mono.just(false));
        // Fix: Mock findAllByStatus to return empty Flux to avoid null pointer
        when(fareRepository.findAllByStatus(anyString())).thenReturn(Flux.empty());

        FareCreateRequest request = FareCreateRequest.builder()
                .organizationId("org-1")
                .fareName("Tarifa X")
                .fareType("MENSUAL")
                .fareAmount(new BigDecimal("30"))
                .build();

        // Captura el objeto que se guarda
        ArgumentCaptor<Fare> captor = ArgumentCaptor.forClass(Fare.class);
        when(fareRepository.save(any(Fare.class)))
                .thenAnswer(inv -> {
                    Fare arg = inv.getArgument(0);
                    return Mono.just(Fare.builder()
                            .id("id-1")
                            .organizationId(arg.getOrganizationId())
                            .fareCode(arg.getFareCode())
                            .fareName(arg.getFareName())
                            .fareType(arg.getFareType())
                            .fareAmount(arg.getFareAmount())
                            .status(arg.getStatus())
                            .createdAt(arg.getCreatedAt())
                            .build());
                });

        // Act & Assert
        // Se verifica que se haya generado correctamente el código TAR100
        StepVerifier.create(fareService.saveF(request))
            .assertNext(resp -> assertEquals("TAR100", resp.getFareCode()))
            .verifyComplete();

        verify(fareRepository).save(captor.capture());
        assertEquals("TAR100", captor.getValue().getFareCode());
    }

    // ============================================================
    // 🔹 TEST PARAMETRIZADO: Casos donde el último código es inválido
    // ============================================================

    /**
     * Test parametrizado que verifica el fallback a TAR001 en diferentes escenarios de código inválido.
     * Reemplaza 3 tests individuales similares para mejorar la mantenibilidad.
     */
    @ParameterizedTest
    @MethodSource("provideInvalidFareCodeScenarios")
    void saveF_ShouldFallbackToInitialCode_WhenLastFareCodeInvalid(String testName, String invalidFareCode, String expectedFareCode) {
        // Arrange: código inválido -> fallback TAR001
        // Handle null case specially
        if (invalidFareCode == null) {
            when(fareRepository.findTopByOrderByFareCodeDesc()).thenReturn(Mono.empty());
        } else {
            Fare last = Fare.builder().fareCode(invalidFareCode).build();
            when(fareRepository.findTopByOrderByFareCodeDesc()).thenReturn(Mono.just(last));
        }
        when(fareRepository.existsByFareCode(expectedFareCode)).thenReturn(Mono.just(false));
        // Fix: Mock findAllByStatus to return empty Flux to avoid null pointer
        when(fareRepository.findAllByStatus(anyString())).thenReturn(Flux.empty());

        FareCreateRequest request = FareCreateRequest.builder()
                .organizationId("org-1")
                .fareName("Tarifa Test")
                .fareType("MENSUAL")
                .fareAmount(new BigDecimal("30"))
                .build();

        // Simulación del guardado
        when(fareRepository.save(any(Fare.class)))
                .thenAnswer(inv -> {
                    Fare arg = inv.getArgument(0);
                    return Mono.just(Fare.builder()
                            .id("id-test")
                            .organizationId(arg.getOrganizationId())
                            .fareCode(arg.getFareCode())
                            .fareName(arg.getFareName())
                            .fareType(arg.getFareType())
                            .fareAmount(arg.getFareAmount())
                            .status(arg.getStatus())
                            .createdAt(arg.getCreatedAt())
                            .build());
                });

        // Act & Assert
        // Se espera que se genere TAR001
        StepVerifier.create(fareService.saveF(request))
            .assertNext(resp -> {
                assertEquals(expectedFareCode, resp.getFareCode());
                System.out.println("✅ " + testName + " - Código generado: " + resp.getFareCode());
            })
            .verifyComplete();
    }

    /**
     * Proporciona los datos para el test parametrizado de códigos de tarifa inválidos.
     */
    private static Stream<Arguments> provideInvalidFareCodeScenarios() {
        return Stream.of(
            Arguments.of("Código inválido", "BAD_CODE", "TAR001"),
            Arguments.of("Código null", null, "TAR001"),
            Arguments.of("Número demasiado grande", "TAR9999999999999999999999999", "TAR001")
        );
    }

    // ============================================================
    // 🔹 Otros casos adicionales de fallback
    // ============================================================
    @Test
    void saveF_ShouldFallbackToInitialCode_WhenNumericPartEmpty() {
        // Arrange: código "TAR" -> parte numérica vacía => 0 => TAR001
        Fare last = Fare.builder().fareCode("TAR").build();
        when(fareRepository.findTopByOrderByFareCodeDesc()).thenReturn(Mono.just(last));
        when(fareRepository.existsByFareCode("TAR001")).thenReturn(Mono.just(false));
        // Fix: Mock findAllByStatus to return empty Flux to avoid null pointer
        when(fareRepository.findAllByStatus(anyString())).thenReturn(Flux.empty());

        FareCreateRequest request = FareCreateRequest.builder()
                .organizationId("org-1")
                .fareName("Tarifa Empty")
                .fareType("MENSUAL")
                .fareAmount(new BigDecimal("30"))
                .build();

        when(fareRepository.save(any(Fare.class)))
                .thenAnswer(inv -> Mono.just(Fare.builder().id("id-6").fareCode("TAR001").build()));

        // Act & Assert
        StepVerifier.create(fareService.saveF(request))
            .assertNext(resp -> assertEquals("TAR001", resp.getFareCode()))
            .verifyComplete();
    }

    // ============================================================
    // 🔹 TEST: Parte numérica con letras (debe resetear a TAR001)
    // ============================================================
    @Test
    void saveF_ShouldFallbackToInitialCode_WhenNumericPartNonDigits() {
        // Arrange: código con letras tras prefijo => no dígitos => 0 => TAR001
        Fare last = Fare.builder().fareCode("TARABC").build();
        when(fareRepository.findTopByOrderByFareCodeDesc()).thenReturn(Mono.just(last));
        when(fareRepository.existsByFareCode("TAR001")).thenReturn(Mono.just(false));
        // Fix: Mock findAllByStatus to return empty Flux to avoid null pointer
        when(fareRepository.findAllByStatus(anyString())).thenReturn(Flux.empty());

        FareCreateRequest request = FareCreateRequest.builder()
                .organizationId("org-1")
                .fareName("Tarifa NonDigits")
                .fareType("MENSUAL")
                .fareAmount(new BigDecimal("30"))
                .build();

        when(fareRepository.save(any(Fare.class)))
                .thenAnswer(inv -> Mono.just(Fare.builder().id("id-7").fareCode("TAR001").build()));

        // Act & Assert
        StepVerifier.create(fareService.saveF(request))
            .assertNext(resp -> assertEquals("TAR001", resp.getFareCode()))
            .verifyComplete();
    }

    // ============================================================
    // 🔹 TEST: Manejo de excepción al obtener código → fallback
    // ============================================================
    @Test
    void saveF_ShouldFallback_WhenGetFareCodeThrows() {
        // Arrange: Simulate the service handling the exception and falling back to TAR001
        Fare last = Fare.builder().fareCode("TARINVALID").build();
        when(fareRepository.findTopByOrderByFareCodeDesc()).thenReturn(Mono.just(last));
        when(fareRepository.existsByFareCode("TAR001")).thenReturn(Mono.just(false));
        // Fix: Mock findAllByStatus to return empty Flux to avoid null pointer
        when(fareRepository.findAllByStatus(anyString())).thenReturn(Flux.empty());

        FareCreateRequest request = FareCreateRequest.builder()
                .organizationId("org-1")
                .fareName("Tarifa Boom")
                .fareType("MENSUAL")
                .fareAmount(new BigDecimal("30"))
                .build();

        when(fareRepository.save(any(Fare.class)))
                .thenAnswer(inv -> Mono.just(Fare.builder().id("id-8").fareCode("TAR001").build()));

        // Act & Assert
        StepVerifier.create(fareService.saveF(request))
            .assertNext(resp -> assertEquals("TAR001", resp.getFareCode()))
            .verifyComplete();
    }

    // ============================================================
    // 🔹 TEST: Desactivar tarifa correctamente
    // ============================================================
    @Test
    void deactivateF_ShouldPersist_WhenDifferentStatus() {
        String id = "fare-2";
        Fare existing = Fare.builder().id(id).status(Constants.ACTIVE.name()).build();
        Fare saved = Fare.builder().id(id).status(Constants.INACTIVE.name()).build();

        when(fareRepository.findById(id)).thenReturn(Mono.just(existing));
        // Fix: Mock findAllByStatus to return empty Flux to avoid null pointer
        when(fareRepository.findAllByStatus(anyString())).thenReturn(Flux.empty());
        when(fareRepository.save(any(Fare.class))).thenReturn(Mono.just(saved));

        StepVerifier.create(fareService.deactivateF(id))
            .assertNext(result -> assertEquals(Constants.INACTIVE.name(), result.getStatus()))
            .verifyComplete();
    }

    // ============================================================
    // 🔹 TESTS: Obtener listas de tarifas (todas, activas, inactivas)
    // ============================================================
    @Test
    void getAllF_ShouldReturnItems() {
        when(fareRepository.findAll()).thenReturn(Flux.just(
                Fare.builder().id("1").build(),
                Fare.builder().id("2").build()
        ));

        StepVerifier.create(fareService.getAllF())
            .expectNextCount(2)
            .verifyComplete();
    }

    @Test
    void getAllF_ShouldPropagateError() {
        when(fareRepository.findAll()).thenReturn(Flux.error(new RuntimeException("DB error")));

        StepVerifier.create(fareService.getAllF())
            .expectErrorMatches(e -> e.getMessage().contains("DB error"))
            .verify();
    }

    @Test
    void getAllActiveF_ShouldReturnItems() {
        when(fareRepository.findAllByStatus(Constants.ACTIVE.name())).thenReturn(Flux.just(
                Fare.builder().id("1").status(Constants.ACTIVE.name()).build()
        ));

        StepVerifier.create(fareService.getAllActiveF())
            .expectNextMatches(f -> Constants.ACTIVE.name().equals(f.getStatus()))
            .verifyComplete();
    }

    @Test
    void getAllInactiveF_ShouldReturnItems() {
        when(fareRepository.findAllByStatus(Constants.INACTIVE.name())).thenReturn(Flux.just(
                Fare.builder().id("1").status(Constants.INACTIVE.name()).build()
        ));

        StepVerifier.create(fareService.getAllInactiveF())
            .expectNextMatches(f -> Constants.INACTIVE.name().equals(f.getStatus()))
            .verifyComplete();
    }

    // ============================================================
    // 🔹 TESTS: Errores en consultas por estado
    // ============================================================
    @Test
    void getAllActiveF_ShouldPropagateError() {
        when(fareRepository.findAllByStatus(Constants.ACTIVE.name()))
                .thenReturn(Flux.error(new RuntimeException("DB error active")));

        StepVerifier.create(fareService.getAllActiveF())
            .expectErrorMatches(e -> e.getMessage().contains("DB error active"))
            .verify();
    }

    @Test
    void getAllInactiveF_ShouldPropagateError() {
        when(fareRepository.findAllByStatus(Constants.INACTIVE.name()))
                .thenReturn(Flux.error(new RuntimeException("DB error inactive")));

        StepVerifier.create(fareService.getAllInactiveF())
            .expectErrorMatches(e -> e.getMessage().contains("DB error inactive"))
            .verify();
    }

    // ============================================================
    // 🔹 TEST: Buscar tarifa por ID existente
    // ============================================================
    @Test
    void getByIdF_ShouldReturnItem_WhenExists() {
        String id = "fare-1";
        when(fareRepository.findById(id)).thenReturn(Mono.just(Fare.builder().id(id).build()));

        StepVerifier.create(fareService.getByIdF(id))
            .assertNext(f -> assertEquals(id, f.getId()))
            .verifyComplete();
    }

    // ============================================================
    // 🔹 TEST: Activar tarifa inexistente → error
    // ============================================================
    @Test
    void activateF_ShouldError_WhenNotFound() {
        String id = "fare-404";
        when(fareRepository.findById(id)).thenReturn(Mono.empty());

        StepVerifier.create(fareService.activateF(id))
            .expectError(CustomException.class)
            .verify();
    }

    // ============================================================
    // 🔹 TEST POSITIVO: Crear tarifa válida desde cero
    // ============================================================
    @Test
    void saveF_ShouldCreateFare_WhenRequestIsValid() {
        System.out.println("Starting test: Creating valid fare");
        // Request válido
        FareCreateRequest request = FareCreateRequest.builder()
                .organizationId("6896b2ecf3e398570ffd99d3")
                .fareName("Tarifa Básica")
                .fareType("SEMANAL")
                .fareAmount(new BigDecimal("15"))
                .build();

        // No hay tarifas previas → genera TAR001
        when(fareRepository.findTopByOrderByFareCodeDesc()).thenReturn(Mono.empty());
        when(fareRepository.existsByFareCode("TAR001")).thenReturn(Mono.just(false));
        // Fix: Mock findAllByStatus to return empty Flux to avoid null pointer
        when(fareRepository.findAllByStatus(anyString())).thenReturn(Flux.empty());

        // Captura de lo que se va a guardar
        ArgumentCaptor<Fare> fareCaptor = ArgumentCaptor.forClass(Fare.class);

        // Simula lo que devuelve el guardado
        Fare savedFare = Fare.builder()
                .id("fare-1")
                .organizationId("6896b2ecf3e398570ffd99d3")
                .fareCode("TAR001")
                .fareName("Tarifa Básica")
                .fareType("SEMANAL")
                .fareAmount(new BigDecimal("15"))
                .status(Constants.ACTIVE.name())
                .createdAt(Instant.now())
                .build();

        when(fareRepository.save(any(Fare.class))).thenReturn(Mono.just(savedFare));

        // Verificación del flujo
        StepVerifier.create(fareService.saveF(request))
                .assertNext(response -> {
                    System.out.println("Fare created correctly with code: " + response.getFareCode());
                    assertNotNull(response);
                    assertEquals("fare-1", response.getId());
                    assertEquals("6896b2ecf3e398570ffd99d3", response.getOrganizationId());
                    assertEquals("TAR001", response.getFareCode());
                    assertEquals("Tarifa Básica", response.getFareName());
                    assertEquals("SEMANAL", response.getFareType());
                    assertEquals(new BigDecimal("15"), response.getFareAmount());
                    assertEquals(Constants.ACTIVE.name(), response.getStatus());
                    assertNotNull(response.getCreatedAt());
                })
                .verifyComplete();

        // Se verifican las llamadas al repositorio
        verify(fareRepository).findTopByOrderByFareCodeDesc();
        verify(fareRepository).existsByFareCode("TAR001");
        verify(fareRepository).save(fareCaptor.capture());

        // Validación del objeto capturado
        Fare fareToSave = fareCaptor.getValue();
        System.out.println("Data sent to repository:");
        System.out.println("   Organization: " + fareToSave.getOrganizationId());
        System.out.println("   Code: " + fareToSave.getFareCode());
        System.out.println("   Name: " + fareToSave.getFareName());
        System.out.println("   Type: " + fareToSave.getFareType());
        System.out.println("   Amount: " + fareToSave.getFareAmount());
        System.out.println("   Status: " + fareToSave.getStatus());

        assertEquals("6896b2ecf3e398570ffd99d3", fareToSave.getOrganizationId());
        assertEquals("TAR001", fareToSave.getFareCode());
        assertEquals("Tarifa Básica", fareToSave.getFareName());
        assertEquals("SEMANAL", fareToSave.getFareType());
        assertEquals(new BigDecimal("15"), fareToSave.getFareAmount());
        assertEquals(Constants.ACTIVE.name(), fareToSave.getStatus());
        assertNotNull(fareToSave.getCreatedAt());

        System.out.println("Test completed successfully\n");
    }

    // ============================================================
    // 🔹 TEST NEGATIVO: No crear si el código ya existe
    // ============================================================
    @Test
    void saveF_ShouldReturnError_WhenFareCodeAlreadyExists() {
        System.out.println("Starting negative test: Fare code already exists");
        // Arrange
        FareCreateRequest request = FareCreateRequest.builder()
                .organizationId("6896b2ecf3e398570ffd99d3")
                .fareName("Tarifa Duplicada")
                .fareType("SEMANAL")
                .fareAmount(new BigDecimal("20"))
                .build();

        // Simula que TAR001 ya existe
        when(fareRepository.findTopByOrderByFareCodeDesc()).thenReturn(Mono.empty());
        when(fareRepository.existsByFareCode("TAR001")).thenReturn(Mono.just(true));
        // Fix: Mock findAllByStatus to return empty Flux to avoid null pointer
        when(fareRepository.findAllByStatus(anyString())).thenReturn(Flux.empty());

        // Act & Assert
        StepVerifier.create(fareService.saveF(request))
                .expectErrorSatisfies(error -> {
                    assertTrue(error instanceof CustomException);
                    CustomException ce = (CustomException) error;
                    assertEquals("Conflicto", ce.getErrorMessage().getMessage());
                    assertEquals("Fare code already exists", ce.getErrorMessage().getDetails());
                    System.out.println("Expected error: " + ce.getErrorMessage().getMessage());
                })
                .verify();

        System.out.println("Negative test completed successfully\n");
    }
}