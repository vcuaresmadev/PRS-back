package pe.edu.vallegrande.msdistribution.application.services;

import pe.edu.vallegrande.msdistribution.infrastructure.dto.request.DistributionProgramCreateRequest;
import pe.edu.vallegrande.msdistribution.infrastructure.dto.response.DistributionProgramResponse;
import pe.edu.vallegrande.msdistribution.infrastructure.dto.response.enriched.EnrichedDistributionProgramResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface DistributionProgramService {
    
    Flux<DistributionProgramResponse> getAll();
    
    Mono<DistributionProgramResponse> getById(String id);
    
    Mono<DistributionProgramResponse> save(DistributionProgramCreateRequest request);
    
    Mono<DistributionProgramResponse> update(String id, DistributionProgramCreateRequest request);
    
    Mono<Void> delete(String id);
    
    Mono<DistributionProgramResponse> activate(String id);
    
    Mono<DistributionProgramResponse> desactivate(String id);
    
    // Método para eliminado físico
    Mono<Void> physicalDelete(String id);
    
    // New methods for enriched distribution program data
    Mono<EnrichedDistributionProgramResponse> getEnrichedById(String id);
    
    Flux<EnrichedDistributionProgramResponse> getAllEnriched();
    
    // New method for saving and returning enriched response
    Mono<EnrichedDistributionProgramResponse> saveAndEnrich(DistributionProgramCreateRequest request);
    
    // Method to get programs by organization ID
    Flux<DistributionProgramResponse> getByOrganizationId(String organizationId);
}