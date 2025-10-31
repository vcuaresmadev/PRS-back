package pe.edu.vallegrande.msdistribution.application.services;

import pe.edu.vallegrande.msdistribution.domain.models.Fare;
import pe.edu.vallegrande.msdistribution.infrastructure.dto.request.FareCreateRequest;
import pe.edu.vallegrande.msdistribution.infrastructure.dto.response.FareResponse;
import pe.edu.vallegrande.msdistribution.infrastructure.dto.response.enriched.EnrichedFareResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface FareService {
    
    Flux<FareResponse> getAllF();
    
    Flux<FareResponse> getAllActiveF();
    
    Flux<FareResponse> getAllInactiveF();
    
    Mono<FareResponse> getByIdF(String id);
    
    Mono<FareResponse> saveF(FareCreateRequest request);
    
    Mono<FareResponse> updateF(String id, FareCreateRequest request);
    
    Mono<Void> deleteF(String id);
    
    Mono<FareResponse> activateF(String id);
    
    Mono<FareResponse> deactivateF(String id);
    
    // New methods for enriched fare data
    Mono<EnrichedFareResponse> getEnrichedById(String id);
    
    Flux<EnrichedFareResponse> getAllEnriched();
    
    Flux<EnrichedFareResponse> getAllActiveEnriched();
    
    Flux<EnrichedFareResponse> getAllInactiveEnriched();
    
    // Method to get current active fare based on effective date
    Mono<Fare> getCurrentActiveFare(String organizationId);
    
    // Method to get fares by organization ID
    Flux<Fare> getByOrganizationId(String organizationId);
    
    // Method to manually trigger fare transitions processing
    void triggerFareTransitions();
}