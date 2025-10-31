package pe.edu.vallegrande.msdistribution.application.services.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import pe.edu.vallegrande.msdistribution.infrastructure.dto.response.enriched.EnrichedFareResponse;
import pe.edu.vallegrande.msdistribution.application.services.FareService;
import pe.edu.vallegrande.msdistribution.domain.models.Fare;
import pe.edu.vallegrande.msdistribution.domain.enums.Constants;
import pe.edu.vallegrande.msdistribution.infrastructure.dto.request.FareCreateRequest;
import pe.edu.vallegrande.msdistribution.infrastructure.dto.response.FareResponse;
import pe.edu.vallegrande.msdistribution.infrastructure.exception.CustomException;
import pe.edu.vallegrande.msdistribution.infrastructure.repository.FareRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.util.Date;

@Service
@RequiredArgsConstructor
@Slf4j
public class FareServiceImpl implements FareService {

    private final FareRepository fareRepository;

    @Override
    public Flux<FareResponse> getAllF() {
        return fareRepository.findAll()
                .map(this::toResponse)
                .doOnNext(fare -> System.out.println("Fare retrieved: " + fare));
    }

    @Override
    public Flux<FareResponse> getAllActiveF() {
        return fareRepository.findAllByStatus(Constants.ACTIVE.name())
                .map(this::toResponse);
    }

    @Override
    public Flux<FareResponse> getAllInactiveF() {
        return fareRepository.findAllByStatus(Constants.INACTIVE.name())
                .map(this::toResponse);
    }

    @Override
    public Mono<FareResponse> getByIdF(String id) {
        return fareRepository.findById(id)
                .map(this::toResponse)
                .switchIfEmpty(Mono.error(CustomException.notFound("Fare", id)));
    }

    @Override
    public Mono<FareResponse> saveF(FareCreateRequest request) {
        return generateNextFareCode()
                .flatMap(generatedCode -> fareRepository.existsByFareCode(generatedCode)
                        .flatMap(exists -> {
                            if (exists) {
                                return Mono.error(CustomException.conflict("Fare code already exists"));
                            }

                            Date now = new Date();
                            // Set fare amount based on current date
                            // Before November 1st: 15 soles, After November 1st: 20 soles
                            java.util.Calendar nov1 = java.util.Calendar.getInstance();
                            nov1.set(2025, java.util.Calendar.NOVEMBER, 1, 0, 0, 0);
                            nov1.set(java.util.Calendar.MILLISECOND, 0);
                            Date novFirst = nov1.getTime();

                            BigDecimal fareAmount = "MENSUAL".equalsIgnoreCase(request.getFareType())
                                    ? (now.before(novFirst)  // Before November 1st
                                        ? BigDecimal.valueOf(15.00)
                                        : BigDecimal.valueOf(20.00))
                                    : request.getFareAmount();

                            // Effective date is always November 1st for new fares
                            Date effectiveDate = novFirst;

                            // ⚡ Estado inicial según fecha de vigencia
                            String status = now.before(effectiveDate) || now.equals(effectiveDate)
                                    ? Constants.ACTIVE.name()
                                    : Constants.INACTIVE.name();

                            Fare fare = Fare.builder()
                                .organizationId(request.getOrganizationId())
                                .fareCode(generatedCode)
                                .fareName(request.getFareName())
                                .fareType(request.getFareType())
                                .fareAmount(fareAmount)
                                .effectiveDate(effectiveDate)
                                .status(status)
                                .createdAt(now.toInstant())
                                .build();

                            return fareRepository.save(fare)
                                    .flatMap(this::handleTimeBasedFareActivation)
                                    .map(this::toResponse);
                        }));
    }

    /**
     * Handles time-based fare activation:
     * - Deactivates current active fares that will be replaced by this new fare
     * - Schedules future activation if the effective date is in the future
     */
    private Mono<Fare> handleTimeBasedFareActivation(Fare newFare) {
        // For simplicity, we'll deactivate all active fares for the same organization
        // In a more complex system, you might want to filter by fare type or other criteria
        // But exclude the newly created fare
        return fareRepository.findAllByStatus(Constants.ACTIVE.name())
                .filter(fare -> fare.getOrganizationId().equals(newFare.getOrganizationId()))
                .filter(fare -> !fare.getId().equals(newFare.getId())) // Exclude the new fare
                .flatMap(fare -> {
                    // Deactivate the current fare
                    fare.setStatus(Constants.INACTIVE.name());
                    return fareRepository.save(fare);
                })
                .then(Mono.just(newFare));
    }

    private static final String FARE_PREFIX = "TAR";

    private Mono<String> generateNextFareCode() {
        return fareRepository.findTopByOrderByFareCodeDesc()
                .map(last -> {
                    String lastCode = last.getFareCode(); // ej. "TAR003"
                    int number = 0;
                    try {
                        number = Integer.parseInt(lastCode.replace(FARE_PREFIX, ""));
                    } catch (NumberFormatException e) {
                        // Si el código no sigue el patrón, asumimos 0
                    }
                    return String.format(FARE_PREFIX + "%03d", number + 1);
                })
                .defaultIfEmpty(FARE_PREFIX + "001");
    }

    @Override
    public Mono<FareResponse> updateF(String id, FareCreateRequest request) {
        return fareRepository.findById(id)
                .switchIfEmpty(Mono.error(CustomException.notFound("Fare", id)))
                .flatMap(existingFare -> {
                    Date now = new Date();
                    existingFare.setOrganizationId(request.getOrganizationId());
                    existingFare.setFareName(request.getFareName());
                    existingFare.setFareType(request.getFareType());
                    
                    // 💰 Recalcular monto automático si es MENSUAL
                    // Set fare amount based on current date
                    // Before November 1st: 15 soles, After November 1st: 20 soles
                    java.util.Calendar nov1 = java.util.Calendar.getInstance();
                    nov1.set(2025, java.util.Calendar.NOVEMBER, 1, 0, 0, 0);
                    nov1.set(java.util.Calendar.MILLISECOND, 0);
                    Date novFirst = nov1.getTime();
                    
                    BigDecimal fareAmount = "MENSUAL".equalsIgnoreCase(request.getFareType())
                            ? (now.before(novFirst)  // Before November 1st
                                ? BigDecimal.valueOf(15.00)
                                : BigDecimal.valueOf(20.00))
                            : request.getFareAmount();
                    existingFare.setFareAmount(fareAmount);

                    // 📅 Actualizar fecha de vigencia (mantener la anterior si no se envía)
                    Date effectiveDate = (request.getEffectiveDate() != null)
                            ? request.getEffectiveDate()
                            : existingFare.getEffectiveDate();
                    existingFare.setEffectiveDate(effectiveDate);

                    // 🟢 Actualizar estado según la fecha de vigencia
                    existingFare.setStatus(now.before(effectiveDate) || now.equals(effectiveDate)
                            ? Constants.ACTIVE.name()
                            : Constants.INACTIVE.name());

                    return fareRepository.save(existingFare)
                        .flatMap(this::handleTimeBasedFareActivation)
                        .map(this::toResponse);
                });
    }

    @Override
    public Mono<Void> deleteF(String id) {
        return fareRepository.findById(id)
                .switchIfEmpty(Mono.error(CustomException.notFound("Fare", id)))
                .flatMap(fareRepository::delete);
    }

    @Override
    public Mono<FareResponse> activateF(String id) {
        return changeStatus(id, Constants.ACTIVE.name())
                .map(this::toResponse);
    }

    @Override
    public Mono<FareResponse> deactivateF(String id) {
        return changeStatus(id, Constants.INACTIVE.name())
                .switchIfEmpty(Mono.error(CustomException.notFound("Fare", id)))
                .map(this::toResponse)
                .doOnError(e -> log.error("❌ Error al desactivar tarifa {}: {}", id, e.getMessage(), e))
                .onErrorMap(e -> {
                    if (e instanceof CustomException) return e;
                    return CustomException.internalServerError(
                            "Error interno al desactivar tarifa",
                            e.getMessage()
                    );
                });
    }

    private Mono<Fare> changeStatus(String id, String newStatus) {
    return fareRepository.findById(id)
            .switchIfEmpty(Mono.error(CustomException.notFound("Fare", id)))
            .flatMap(fare -> {
                // 🟡 Verificar si ya está en ese estado
                if (newStatus.equalsIgnoreCase(fare.getStatus())) {
                    return Mono.error(CustomException.conflict(
                            "La tarifa ya se encuentra en estado " + newStatus
                    ));
                }

                // ✅ Cambiar estado y guardar
                fare.setStatus(newStatus);
                return fareRepository.save(fare);
            })
            .doOnSuccess(f -> log.info("✅ Estado de tarifa {} actualizado a {}", id, newStatus))
            .doOnError(e -> log.error("❌ Error cambiando estado de tarifa {}: {}", id, e.getMessage(), e))
            .onErrorMap(e -> {
                if (e instanceof CustomException) return e;
                return CustomException.internalServerError(
                        "Error interno al cambiar estado de tarifa",
                        e.getMessage()
                );
            });
    }
    
    // New methods for enriched fare data
    
    @Override
    public Mono<EnrichedFareResponse> getEnrichedById(String id) {
        return fareRepository.findById(id)
                .switchIfEmpty(Mono.error(CustomException.notFound("Fare", id)))
                .map(this::toEnrichedResponse);
    }
    
    @Override
    public Flux<EnrichedFareResponse> getAllEnriched() {
        return fareRepository.findAll()
                .map(this::toEnrichedResponse);
    }
    
    @Override
    public Flux<EnrichedFareResponse> getAllActiveEnriched() {
        return fareRepository.findAllByStatus(Constants.ACTIVE.name())
                .map(this::toEnrichedResponse);
    }
    
    @Override
    public Flux<EnrichedFareResponse> getAllInactiveEnriched() {
        return fareRepository.findAllByStatus(Constants.INACTIVE.name())
                .map(this::toEnrichedResponse);
    }
    
    private EnrichedFareResponse toEnrichedResponse(Fare fare) {
        return EnrichedFareResponse.builder()
                .id(fare.getId())
                .organizationId(fare.getOrganizationId())
                .fareCode(fare.getFareCode())
                .fareName(fare.getFareName())
                .fareType(fare.getFareType())
                .fareAmount(fare.getFareAmount())
                .status(fare.getStatus())
                .createdAt(fare.getCreatedAt())
                .build();
    }
    
    private FareResponse toResponse(Fare fare) {
        return FareResponse.builder()
                .id(fare.getId())
                .organizationId(fare.getOrganizationId())
                .fareCode(fare.getFareCode())
                .fareName(fare.getFareName())
                .fareType(fare.getFareType())
                .fareAmount(fare.getFareAmount())
                .effectiveDate(fare.getEffectiveDate())
                .status(fare.getStatus())
                .createdAt(fare.getCreatedAt())
                .build();
    }
    
    /**
     * Gets the current active fare based on the effective date
     * @param organizationId the organization ID
     * @return the current active fare
     */
    public Mono<Fare> getCurrentActiveFare(String organizationId) {
        Date now = new Date();
        return fareRepository.findAllByStatus(Constants.ACTIVE.name())
                .filter(fare -> fare.getOrganizationId().equals(organizationId))
                .filter(fare -> fare.getEffectiveDate() == null || fare.getEffectiveDate().after(now) || fare.getEffectiveDate().equals(now))
                .sort((f1, f2) -> f2.getEffectiveDate().compareTo(f1.getEffectiveDate()))
                .next();
    }
    
    @Override
    public Flux<Fare> getByOrganizationId(String organizationId) {
        return fareRepository.findAll()
                .filter(fare -> fare.getOrganizationId().equals(organizationId));
    }
    
    @Override
    public void triggerFareTransitions() {
        // Implementation would go here
    }
    
    /**
     * Automatically deactivate old fares after November 1st
     * and set new fares to active with updated pricing
     */
    public void processAutomaticFareTransitions() {
        Date now = new Date();
        
        // Set the transition date (November 1st, 2025)
        java.util.Calendar nov1 = java.util.Calendar.getInstance();
        nov1.set(2025, java.util.Calendar.NOVEMBER, 1, 0, 0, 0);
        nov1.set(java.util.Calendar.MILLISECOND, 0);
        Date novFirst = nov1.getTime();
        
        // If we're past November 1st, deactivate old fares
        if (now.after(novFirst)) {
            // Find all active fares with effective date before November 1st
            fareRepository.findAllByStatus(Constants.ACTIVE.name())
                .filter(fare -> fare.getEffectiveDate() != null && fare.getEffectiveDate().before(novFirst))
                .flatMap(fare -> {
                    // Deactivate old fares
                    fare.setStatus(Constants.INACTIVE.name());
                    return fareRepository.save(fare);
                })
                .subscribe();
        }
    }
}