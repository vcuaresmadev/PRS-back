package pe.edu.vallegrande.msdistribution.application.services.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import pe.edu.vallegrande.msdistribution.application.services.DistributionRouteService;
import pe.edu.vallegrande.msdistribution.domain.models.DistributionRoute;
import pe.edu.vallegrande.msdistribution.domain.enums.Constants;
import pe.edu.vallegrande.msdistribution.infrastructure.dto.request.DistributionRouteCreateRequest;
import pe.edu.vallegrande.msdistribution.infrastructure.dto.response.DistributionRouteResponse;
import pe.edu.vallegrande.msdistribution.infrastructure.dto.response.enriched.EnrichedDistributionRouteResponse;
import pe.edu.vallegrande.msdistribution.infrastructure.exception.CustomException;
import pe.edu.vallegrande.msdistribution.infrastructure.repository.DistributionRouteRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Instant;

@Service
@Slf4j
public class DistributionRouteServiceImpl implements DistributionRouteService {

    @Autowired
    private DistributionRouteRepository repository;

    @Override
    public Flux<DistributionRoute> getAll() {
        return repository.findAll();
    }

    @Override
    public Flux<DistributionRoute> getAllActive() {
        return repository.findAllByStatus(Constants.ACTIVE.name());
    }

    @Override
    public Flux<DistributionRoute> getAllInactive() {
        return repository.findAllByStatus(Constants.INACTIVE.name());
    }

    @Override
    public Mono<DistributionRoute> getById(String id) {
        return repository.findById(id)
                .switchIfEmpty(Mono.error(new CustomException(
                        HttpStatus.NOT_FOUND.value(),
                        "Route not found",
                        "No route found with id " + id)));
    }

    @Override
    public Mono<DistributionRouteResponse> save(DistributionRouteCreateRequest request) {
        return generateNextRouteCode()
                .flatMap(generatedCode ->
                        repository.existsByRouteCode(generatedCode)
                                .flatMap(exists -> {
                                    if (exists) {
                                        return Mono.error(new CustomException(
                                                HttpStatus.BAD_REQUEST.value(),
                                                "Route code already exists",
                                                "Route code " + generatedCode + " already exists"));
                                    }

                                    // Convertir las zonas del DTO al modelo
                                    java.util.List<DistributionRoute.ZoneOrder> zoneOrders = 
                                        java.util.Collections.emptyList();
                                    if (request.getZones() != null) {
                                        zoneOrders = request.getZones().stream()
                                            .map(zone -> new DistributionRoute.ZoneOrder(
                                                zone.getZoneId(),
                                                zone.getOrder() != null ? zone.getOrder() : 0,
                                                zone.getEstimatedDuration() != null ? zone.getEstimatedDuration() : 0))
                                            .collect(java.util.stream.Collectors.toList());
                                    }

                                    DistributionRoute route = DistributionRoute.builder()
                                            .organizationId(request.getOrganizationId())
                                            .routeCode(generatedCode)
                                            .routeName(request.getRouteName())
                                            .zones(zoneOrders)
                                            .totalEstimatedDuration(request.getTotalEstimatedDuration() != null ? 
                                                request.getTotalEstimatedDuration() : 0)
                                            .responsibleUserId(request.getResponsibleUserId())
                                            .status(Constants.ACTIVE.name())
                                            .createdAt(Instant.now())
                                            .build();

                                    return repository.save(route)
                                            .map(saved -> {
                                                // Convertir las zonas para la respuesta
                                                String firstZoneId = null;
                                                java.util.List<DistributionRouteResponse.ZoneDetail> zoneDetails = 
                                                    java.util.Collections.emptyList();
                                                    
                                                if (saved.getZones() != null && !saved.getZones().isEmpty()) {
                                                    // Primer zoneId para compatibilidad
                                                    firstZoneId = saved.getZones().get(0).getZoneId();
                                                    
                                                    // Lista completa de zonas
                                                    zoneDetails = saved.getZones().stream()
                                                        .map(zone -> new DistributionRouteResponse.ZoneDetail(
                                                            zone.getZoneId(),
                                                            zone.getOrder(),
                                                            zone.getEstimatedDuration()))
                                                        .collect(java.util.stream.Collectors.toList());
                                                }
                                                
                                                return DistributionRouteResponse.builder()
                                                        .id(saved.getId())
                                                        .organizationId(saved.getOrganizationId())
                                                        .routeCode(saved.getRouteCode())
                                                        .routeName(saved.getRouteName())
                                                        .zoneId(firstZoneId)
                                                        .zones(zoneDetails)
                                                        .totalEstimatedDuration(saved.getTotalEstimatedDuration())
                                                        .responsibleUserId(saved.getResponsibleUserId())
                                                        .status(saved.getStatus())
                                                        .createdAt(saved.getCreatedAt())
                                                        .build();
                                            });
                                })
                );
    }

    private static final String ROUTE_PREFIX = "RUT";

    private Mono<String> generateNextRouteCode() {
        return repository.findTopByOrderByRouteCodeDesc()
                .map(last -> {
                    String lastCode = last.getRouteCode();
                    int number = 0;
                    try {
                        number = Integer.parseInt(lastCode.replace(ROUTE_PREFIX, ""));
                    } catch (NumberFormatException ignored) {}
                    return String.format(ROUTE_PREFIX + "%03d", number + 1);
                })
                .defaultIfEmpty(ROUTE_PREFIX + "001");
    }

    @Override
    public Mono<DistributionRoute> update(String id, DistributionRoute route) {
        return repository.findById(id)
                .switchIfEmpty(Mono.error(new CustomException(
                        HttpStatus.NOT_FOUND.value(),
                        "Route not found",
                        "No route found with id " + id)))
                .flatMap(existing -> {
                    existing.setRouteName(route.getRouteName());
                    existing.setZones(route.getZones());
                    existing.setTotalEstimatedDuration(route.getTotalEstimatedDuration());
                    existing.setResponsibleUserId(route.getResponsibleUserId());
                    return repository.save(existing);
                });
    }

    @Override
    public Mono<Void> delete(String id) {
        return repository.findById(id)
                .switchIfEmpty(Mono.error(new CustomException(
                        HttpStatus.NOT_FOUND.value(),
                        "Route not found",
                        "Cannot delete route with id " + id)))
                .flatMap(repository::delete);
    }

    @Override
    public Mono<DistributionRoute> activate(String id) {
        return changeStatus(id, Constants.ACTIVE.name());
    }

    @Override
    public Mono<DistributionRoute> deactivate(String id) {
        return changeStatus(id, Constants.INACTIVE.name());
    }

    private Mono<DistributionRoute> changeStatus(String id, String status) {
        return repository.findById(id)
                .switchIfEmpty(Mono.error(new CustomException(
                        HttpStatus.NOT_FOUND.value(),
                        "Route not found",
                        "Cannot change status of route with id " + id)))
                .flatMap(route -> {
                    route.setStatus(status);
                    return repository.save(route);
                });
    }
    
    // New methods for enriched distribution route data
    
    @Override
    public Mono<EnrichedDistributionRouteResponse> getEnrichedById(String id) {
        return repository.findById(id)
                .switchIfEmpty(Mono.error(new CustomException(
                        HttpStatus.NOT_FOUND.value(),
                        "Route not found",
                        "No route found with id " + id)))
                .map(this::toEnrichedResponse);
    }
    
    @Override
    public Flux<EnrichedDistributionRouteResponse> getAllEnriched() {
        return repository.findAll()
                .map(this::toEnrichedResponse);
    }
    
    @Override
    public Flux<EnrichedDistributionRouteResponse> getAllActiveEnriched() {
        return repository.findAllByStatus(Constants.ACTIVE.name())
                .map(this::toEnrichedResponse);
    }
    
    @Override
    public Flux<EnrichedDistributionRouteResponse> getAllInactiveEnriched() {
        return repository.findAllByStatus(Constants.INACTIVE.name())
                .map(this::toEnrichedResponse);
    }
    
    private EnrichedDistributionRouteResponse toEnrichedResponse(DistributionRoute route) {
        // Convertir las zonas para la respuesta (usar el primer zoneId)
        String firstZoneId = null;
        java.util.List<EnrichedDistributionRouteResponse.ZoneDetail> zoneDetails = 
            java.util.Collections.emptyList();
            
        if (route.getZones() != null && !route.getZones().isEmpty()) {
            // Primer zoneId para compatibilidad
            firstZoneId = route.getZones().get(0).getZoneId();
            
            // Lista completa de zonas
            zoneDetails = route.getZones().stream()
                .map(zone -> new EnrichedDistributionRouteResponse.ZoneDetail(
                    zone.getZoneId(),
                    zone.getOrder(),
                    zone.getEstimatedDuration()))
                .collect(java.util.stream.Collectors.toList());
        }
        
        return EnrichedDistributionRouteResponse.builder()
                .id(route.getId())
                .organizationId(route.getOrganizationId())
                .routeCode(route.getRouteCode())
                .routeName(route.getRouteName())
                .zoneId(firstZoneId)
                .zones(zoneDetails) // Agregar la lista de zonas
                .totalEstimatedDuration(route.getTotalEstimatedDuration())
                .responsibleUserId(route.getResponsibleUserId())
                .status(route.getStatus())
                .createdAt(route.getCreatedAt())
                .build();
    }

    @Override
    public Mono<DistributionRouteResponse> update(String id, DistributionRouteCreateRequest request) {
        return repository.findById(id)
                .switchIfEmpty(Mono.error(new CustomException(
                        HttpStatus.NOT_FOUND.value(),
                        "Route not found",
                        "No route found with id " + id)))
                .flatMap(existing -> {
                    // Convertir las zonas del DTO al modelo
                    java.util.List<DistributionRoute.ZoneOrder> zoneOrders = 
                        java.util.Collections.emptyList();
                    if (request.getZones() != null) {
                        zoneOrders = request.getZones().stream()
                            .map(zone -> new DistributionRoute.ZoneOrder(
                                zone.getZoneId(),
                                zone.getOrder() != null ? zone.getOrder() : 0,
                                zone.getEstimatedDuration() != null ? zone.getEstimatedDuration() : 0))
                            .collect(java.util.stream.Collectors.toList());
                    }
                    
                    existing.setRouteName(request.getRouteName());
                    existing.setZones(zoneOrders);
                    existing.setTotalEstimatedDuration(request.getTotalEstimatedDuration() != null ? 
                        request.getTotalEstimatedDuration() : 0);
                    existing.setResponsibleUserId(request.getResponsibleUserId());
                    existing.setOrganizationId(request.getOrganizationId());
                    
                    return repository.save(existing)
                            .map(updated -> {
                                // Convertir las zonas para la respuesta
                                String firstZoneId = null;
                                java.util.List<DistributionRouteResponse.ZoneDetail> zoneDetails = 
                                    java.util.Collections.emptyList();
                                    
                                if (updated.getZones() != null && !updated.getZones().isEmpty()) {
                                    // Primer zoneId para compatibilidad
                                    firstZoneId = updated.getZones().get(0).getZoneId();
                                    
                                    // Lista completa de zonas
                                    zoneDetails = updated.getZones().stream()
                                        .map(zone -> new DistributionRouteResponse.ZoneDetail(
                                            zone.getZoneId(),
                                            zone.getOrder(),
                                            zone.getEstimatedDuration()))
                                        .collect(java.util.stream.Collectors.toList());
                                }
                                
                                return DistributionRouteResponse.builder()
                                        .id(updated.getId())
                                        .organizationId(updated.getOrganizationId())
                                        .routeCode(updated.getRouteCode())
                                        .routeName(updated.getRouteName())
                                        .zoneId(firstZoneId)
                                        .zones(zoneDetails)
                                        .totalEstimatedDuration(updated.getTotalEstimatedDuration())
                                        .responsibleUserId(updated.getResponsibleUserId())
                                        .status(updated.getStatus())
                                        .createdAt(updated.getCreatedAt())
                                        .build();
                            });
                });
    }
}