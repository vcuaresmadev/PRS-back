package pe.edu.vallegrande.msdistribution.application.services;

import pe.edu.vallegrande.msdistribution.domain.models.DistributionSchedule;
import pe.edu.vallegrande.msdistribution.infrastructure.dto.request.DistributionScheduleCreateRequest;
import pe.edu.vallegrande.msdistribution.infrastructure.dto.response.DistributionScheduleResponse;
import pe.edu.vallegrande.msdistribution.infrastructure.dto.response.enriched.EnrichedDistributionScheduleResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface DistributionScheduleService {
    
    Flux<DistributionSchedule> getAll();
    
    Flux<DistributionSchedule> getAllActive();
    
    Flux<DistributionSchedule> getAllInactive();
    
    Mono<DistributionSchedule> getById(String id);
    
    Mono<DistributionScheduleResponse> save(DistributionScheduleCreateRequest request);
    
    Mono<DistributionSchedule> update(String id, DistributionSchedule schedule);
    
    Mono<DistributionScheduleResponse> update(String id, DistributionScheduleCreateRequest request);
    
    Mono<Void> delete(String id);
    
    Mono<DistributionSchedule> activate(String id);
    
    Mono<DistributionSchedule> deactivate(String id);
    
    // New methods for enriched distribution schedule data
    Mono<EnrichedDistributionScheduleResponse> getEnrichedById(String id);
    
    Flux<EnrichedDistributionScheduleResponse> getAllEnriched();
    
    Flux<EnrichedDistributionScheduleResponse> getAllActiveEnriched();
    
    Flux<EnrichedDistributionScheduleResponse> getAllInactiveEnriched();
    
    // New method for saving and returning enriched response
    Mono<EnrichedDistributionScheduleResponse> saveAndEnrich(DistributionScheduleCreateRequest request);
}