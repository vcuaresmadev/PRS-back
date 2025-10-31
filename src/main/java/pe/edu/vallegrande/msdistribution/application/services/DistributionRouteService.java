package pe.edu.vallegrande.msdistribution.application.services;

import pe.edu.vallegrande.msdistribution.domain.models.DistributionRoute;
import pe.edu.vallegrande.msdistribution.infrastructure.dto.request.DistributionRouteCreateRequest;
import pe.edu.vallegrande.msdistribution.infrastructure.dto.response.DistributionRouteResponse;
import pe.edu.vallegrande.msdistribution.infrastructure.dto.response.enriched.EnrichedDistributionRouteResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface DistributionRouteService {
    
    Flux<DistributionRoute> getAll();
    
    Flux<DistributionRoute> getAllActive();
    
    Flux<DistributionRoute> getAllInactive();
    
    Mono<DistributionRoute> getById(String id);
    
    Mono<DistributionRouteResponse> save(DistributionRouteCreateRequest request);
    
    Mono<DistributionRoute> update(String id, DistributionRoute route);
    
    Mono<DistributionRouteResponse> update(String id, DistributionRouteCreateRequest request);
    
    Mono<Void> delete(String id);
    
    Mono<DistributionRoute> activate(String id);
    
    Mono<DistributionRoute> deactivate(String id);
    
    // New methods for enriched distribution route data
    Mono<EnrichedDistributionRouteResponse> getEnrichedById(String id);
    
    Flux<EnrichedDistributionRouteResponse> getAllEnriched();
    
    Flux<EnrichedDistributionRouteResponse> getAllActiveEnriched();
    
    Flux<EnrichedDistributionRouteResponse> getAllInactiveEnriched();
}