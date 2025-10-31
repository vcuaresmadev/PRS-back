package pe.edu.vallegrande.msdistribution.application.services.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import pe.edu.vallegrande.msdistribution.application.services.DistributionProgramService;
import pe.edu.vallegrande.msdistribution.domain.models.DistributionProgram;
import pe.edu.vallegrande.msdistribution.infrastructure.dto.request.DistributionProgramCreateRequest;
import pe.edu.vallegrande.msdistribution.infrastructure.dto.response.DistributionProgramResponse;
import pe.edu.vallegrande.msdistribution.infrastructure.dto.response.enriched.EnrichedDistributionProgramResponse;
import pe.edu.vallegrande.msdistribution.infrastructure.repository.DistributionProgramRepository;

import org.springframework.stereotype.Service;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Instant;

@Service
@RequiredArgsConstructor
@Slf4j
public class DistributionProgramServiceImpl implements DistributionProgramService {

    private final DistributionProgramRepository repository;
    
    private static final String PROGRAM_PREFIX = "PRG";

    @Override
    public Flux<DistributionProgramResponse> getAll() {
        return repository.findAll()
                .map(this::toResponse);
    }
    
    @Override
    public Flux<EnrichedDistributionProgramResponse> getAllEnriched() {
        return repository.findAll()
                .map(this::toEnrichedResponse);
    }
    
    @Override
    public Flux<DistributionProgramResponse> getByOrganizationId(String organizationId) {
        return repository.findByOrganizationId(organizationId)
                .map(this::toResponse);
    }
    
    // Método auxiliar para obtener todos los programas activos (no eliminados)
    public Flux<DistributionProgramResponse> getAllActive() {
        return repository.findAllByStatus("ACTIVE")
                .map(this::toResponse);
    }
    
    // Método auxiliar para obtener todos los programas inactivos (no eliminados)
    public Flux<DistributionProgramResponse> getAllInactive() {
        return repository.findAllByStatus("INACTIVE")
                .map(this::toResponse);
    }

    @Override
    public Mono<DistributionProgramResponse> getById(String id) {
        return repository.findById(id)
                .map(this::toResponse);
    }

    @Override
    public Mono<DistributionProgramResponse> save(DistributionProgramCreateRequest request) {
        return generateNextProgramCode()
                .flatMap(generatedCode -> {
                    DistributionProgram program = DistributionProgram.builder()
                            .organizationId(request.getOrganizationId())
                            .programCode(generatedCode) // Usar el código generado automáticamente
                            .scheduleId(request.getScheduleId())
                            .routeId(request.getRouteId())
                            .zoneId(request.getZoneId())
                            .streetId(request.getStreetId())
                            .programDate(request.getProgramDate())
                            .plannedStartTime(request.getPlannedStartTime())
                            .plannedEndTime(request.getPlannedEndTime())
                            .actualStartTime(request.getActualStartTime())
                            .actualEndTime(request.getActualEndTime())
                            .status(request.getActualStartTime() != null || request.getActualEndTime() != null ? "IN_PROGRESS" : "PLANNED")
                            .responsibleUserId(request.getResponsibleUserId())
                            .observations(request.getObservations())
                            .createdAt(Instant.now())
                            .build();

                    return repository.save(program)
                            .map(this::toResponse);
                });
    }

    @Override
    public Mono<DistributionProgramResponse> update(String id, DistributionProgramCreateRequest request) {
        return repository.findById(id)
                .flatMap(existing -> {
                    existing.setOrganizationId(request.getOrganizationId());
                    // No actualizar programCode ya que se genera automáticamente
                    existing.setScheduleId(request.getScheduleId());
                    existing.setRouteId(request.getRouteId());
                    existing.setZoneId(request.getZoneId());
                    existing.setStreetId(request.getStreetId());
                    existing.setProgramDate(request.getProgramDate());
                    existing.setPlannedStartTime(request.getPlannedStartTime());
                    existing.setPlannedEndTime(request.getPlannedEndTime());
                    existing.setResponsibleUserId(request.getResponsibleUserId());
                    existing.setObservations(request.getObservations());
                    
                    return repository.save(existing);
                })
                .map(this::toResponse);
    }

    @Override
    public Mono<Void> delete(String id) {
        return repository.deleteById(id);
    }

    @Override
    public Mono<DistributionProgramResponse> activate(String id) {
        return repository.findById(id)
                .flatMap(program -> {
                    program.setStatus("ACTIVE");
                    return repository.save(program);
                })
                .map(this::toResponse);
    }

    @Override
    public Mono<DistributionProgramResponse> desactivate(String id) {
        return repository.findById(id)
                .flatMap(program -> {
                    program.setStatus("INACTIVE");
                    return repository.save(program);
                })
                .map(this::toResponse);
    }

    @Override
    public Mono<Void> physicalDelete(String id) {
        return repository.deleteById(id);
    }

    private DistributionProgramResponse toResponse(DistributionProgram program) {
        return DistributionProgramResponse.builder()
                .id(program.getId())
                .organizationId(program.getOrganizationId())
                .programCode(program.getProgramCode())
                .scheduleId(program.getScheduleId())
                .routeId(program.getRouteId())
                .zoneId(program.getZoneId())
                .streetId(program.getStreetId())
                .programDate(program.getProgramDate())
                .plannedStartTime(program.getPlannedStartTime())
                .plannedEndTime(program.getPlannedEndTime())
                .actualStartTime(program.getActualStartTime())
                .actualEndTime(program.getActualEndTime())
                .status(program.getStatus())
                .responsibleUserId(program.getResponsibleUserId())
                .observations(program.getObservations())
                .createdAt(program.getCreatedAt())
                .build();
    }
    
    // New methods for enriched distribution program data
    
    @Override
    public Mono<EnrichedDistributionProgramResponse> getEnrichedById(String id) {
        return repository.findById(id)
                .map(this::toEnrichedResponse);
    }
    
    @Override
    public Mono<EnrichedDistributionProgramResponse> saveAndEnrich(DistributionProgramCreateRequest request) {
        return generateNextProgramCode()
                .flatMap(generatedCode -> {
                    DistributionProgram program = DistributionProgram.builder()
                            .organizationId(request.getOrganizationId())
                            .programCode(generatedCode) // Usar el código generado automáticamente
                            .scheduleId(request.getScheduleId())
                            .routeId(request.getRouteId())
                            .zoneId(request.getZoneId())
                            .streetId(request.getStreetId())
                            .programDate(request.getProgramDate())
                            .plannedStartTime(request.getPlannedStartTime())
                            .plannedEndTime(request.getPlannedEndTime())
                            .actualStartTime(request.getActualStartTime())
                            .actualEndTime(request.getActualEndTime())
                            .status(request.getActualStartTime() != null || request.getActualEndTime() != null ? "IN_PROGRESS" : "PLANNED")
                            .responsibleUserId(request.getResponsibleUserId())
                            .observations(request.getObservations())
                            .createdAt(Instant.now())
                            .build();

                    return repository.save(program)
                            .map(this::toEnrichedResponse);
                });
    }
    
    private EnrichedDistributionProgramResponse toEnrichedResponse(DistributionProgram program) {
        return EnrichedDistributionProgramResponse.builder()
                .id(program.getId())
                .organizationId(program.getOrganizationId())
                .programCode(program.getProgramCode())
                .scheduleId(program.getScheduleId())
                .routeId(program.getRouteId())
                .zoneId(program.getZoneId())
                .streetId(program.getStreetId())
                .programDate(program.getProgramDate())
                .plannedStartTime(program.getPlannedStartTime())
                .plannedEndTime(program.getPlannedEndTime())
                .actualStartTime(program.getActualStartTime())
                .actualEndTime(program.getActualEndTime())
                .status(program.getStatus())
                .responsibleUserId(program.getResponsibleUserId())
                .observations(program.getObservations())
                .createdAt(program.getCreatedAt())
                .build();
    }
    
    private Mono<String> generateNextProgramCode() {
        return repository.findTopByOrderByProgramCodeDesc()
                .map(last -> {
                    String lastCode = last.getProgramCode();
                    int number = 0;
                    try {
                        number = Integer.parseInt(lastCode.replace(PROGRAM_PREFIX, ""));
                    } catch (NumberFormatException ignored) {}
                    return String.format(PROGRAM_PREFIX + "%03d", number + 1);
                })
                .defaultIfEmpty(PROGRAM_PREFIX + "001");
    }
}