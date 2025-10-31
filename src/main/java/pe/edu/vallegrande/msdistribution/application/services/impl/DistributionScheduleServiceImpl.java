package pe.edu.vallegrande.msdistribution.application.services.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import pe.edu.vallegrande.msdistribution.application.services.DistributionScheduleService;
import pe.edu.vallegrande.msdistribution.domain.models.DistributionSchedule;
import pe.edu.vallegrande.msdistribution.domain.enums.Constants;
import pe.edu.vallegrande.msdistribution.infrastructure.dto.request.DistributionScheduleCreateRequest;
import pe.edu.vallegrande.msdistribution.infrastructure.dto.response.DistributionScheduleResponse;
import pe.edu.vallegrande.msdistribution.infrastructure.dto.response.enriched.EnrichedDistributionScheduleResponse;
import pe.edu.vallegrande.msdistribution.infrastructure.exception.CustomException;
import pe.edu.vallegrande.msdistribution.infrastructure.repository.DistributionScheduleRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Instant;

@Service
@Slf4j
public class DistributionScheduleServiceImpl implements DistributionScheduleService {

    @Autowired
    private DistributionScheduleRepository repository;

    @Override
    public Flux<DistributionSchedule> getAll() {
        return repository.findAll();
    }

    @Override
    public Flux<DistributionSchedule> getAllActive() {
        return repository.findAllByStatus(Constants.ACTIVE.name());
    }

    @Override
    public Flux<DistributionSchedule> getAllInactive() {
        return repository.findAllByStatus(Constants.INACTIVE.name());
    }

    @Override
    public Mono<DistributionSchedule> getById(String id) {
        return repository.findById(id)
                .switchIfEmpty(Mono.error(new CustomException(
                        HttpStatus.NOT_FOUND.value(),
                        "Schedule not found",
                        "No schedule found with id " + id)));
    }

@Override
public Mono<DistributionScheduleResponse> save(DistributionScheduleCreateRequest request) {
    return generateNextScheduleCode() // ← usamos el generador
        .flatMap(generatedCode ->
            repository.existsByScheduleCode(generatedCode)
                .flatMap(exists -> {
                    if (exists) {
                        return Mono.error(new CustomException(
                                HttpStatus.BAD_REQUEST.value(),
                                "Schedule code already exists",
                                "Schedule code " + generatedCode + " already exists"));
                    }

                    DistributionSchedule schedule = DistributionSchedule.builder()
                            .organizationId(request.getOrganizationId())
                            .scheduleCode(generatedCode) // ← usamos el código generado
                            .zoneId(request.getZoneId())
                            .streetId(request.getStreetId())
                            .scheduleName(request.getScheduleName())
                            .daysOfWeek(request.getDaysOfWeek())
                            .startTime(request.getStartTime())
                            .endTime(request.getEndTime())
                            .durationHours(request.getDurationHours())
                            .status(Constants.ACTIVE.name())
                            .createdAt(Instant.now())
                            .build();

                    return repository.save(schedule)
                            .map(saved -> DistributionScheduleResponse.builder()
                                    .id(saved.getId())
                                    .organizationId(saved.getOrganizationId())
                                    .scheduleCode(saved.getScheduleCode())
                                    .scheduleName(saved.getScheduleName())
                                    .zoneId(saved.getZoneId())
                                    .streetId(saved.getStreetId()) // Fixed: was incorrectly using getZoneId()
                                    .daysOfWeek(saved.getDaysOfWeek())
                                    .startTime(saved.getStartTime())
                                    .endTime(saved.getEndTime())
                                    .durationHours(saved.getDurationHours())
                                    .status(saved.getStatus())
                                    .createdAt(saved.getCreatedAt())
                                    .build());
                })
        );
}


    private static final String SCHEDULE_PREFIX = "HOR";

private Mono<String> generateNextScheduleCode() {
    return repository.findTopByOrderByScheduleCodeDesc()
            .map(last -> {
                String lastCode = last.getScheduleCode(); // ej. HOR007
                int number = 0;
                try {
                    number = Integer.parseInt(lastCode.replace(SCHEDULE_PREFIX, ""));
                } catch (NumberFormatException ignored) {}
                return String.format(SCHEDULE_PREFIX + "%03d", number + 1);
            })
            .defaultIfEmpty(SCHEDULE_PREFIX + "001");
}


    @Override
    public Mono<DistributionSchedule> update(String id, DistributionSchedule schedule) {
        return repository.findById(id)
                .switchIfEmpty(Mono.error(new CustomException(
                        HttpStatus.NOT_FOUND.value(),
                        "Schedule not found",
                        "No schedule found with id " + id)))
                .flatMap(existing -> {
                    existing.setScheduleName(schedule.getScheduleName());
                    existing.setDaysOfWeek(schedule.getDaysOfWeek());
                    existing.setStartTime(schedule.getStartTime());
                    existing.setEndTime(schedule.getEndTime());
                    existing.setDurationHours(schedule.getDurationHours());
                    return repository.save(existing);
                });
    }

    @Override
    public Mono<Void> delete(String id) {
        return repository.findById(id)
                .switchIfEmpty(Mono.error(new CustomException(
                        HttpStatus.NOT_FOUND.value(),
                        "Schedule not found",
                        "Cannot delete schedule with id " + id)))
                .flatMap(repository::delete);
    }

    @Override
    public Mono<DistributionSchedule> activate(String id) {
        return changeStatus(id, Constants.ACTIVE.name());
    }

    @Override
    public Mono<DistributionSchedule> deactivate(String id) {
        return changeStatus(id, Constants.INACTIVE.name());
    }

    private Mono<DistributionSchedule> changeStatus(String id, String status) {
        return repository.findById(id)
                .switchIfEmpty(Mono.error(new CustomException(
                        HttpStatus.NOT_FOUND.value(),
                        "Schedule not found",
                        "Cannot change status of schedule with id " + id)))
                .flatMap(schedule -> {
                    schedule.setStatus(status);
                    return repository.save(schedule);
                });
    }
    
    // New methods for enriched distribution schedule data
    
    @Override
    public Mono<EnrichedDistributionScheduleResponse> getEnrichedById(String id) {
        return repository.findById(id)
                .switchIfEmpty(Mono.error(new CustomException(
                        HttpStatus.NOT_FOUND.value(),
                        "Schedule not found",
                        "No schedule found with id " + id)))
                .map(this::toEnrichedResponse);
    }
    
    @Override
    public Flux<EnrichedDistributionScheduleResponse> getAllEnriched() {
        return repository.findAll()
                .map(this::toEnrichedResponse);
    }
    
    @Override
    public Flux<EnrichedDistributionScheduleResponse> getAllActiveEnriched() {
        return repository.findAllByStatus(Constants.ACTIVE.name())
                .map(this::toEnrichedResponse);
    }
    
    @Override
    public Flux<EnrichedDistributionScheduleResponse> getAllInactiveEnriched() {
        return repository.findAllByStatus(Constants.INACTIVE.name())
                .map(this::toEnrichedResponse);
    }
    
    @Override
    public Mono<EnrichedDistributionScheduleResponse> saveAndEnrich(DistributionScheduleCreateRequest request) {
        return generateNextScheduleCode()
            .flatMap(generatedCode ->
                repository.existsByScheduleCode(generatedCode)
                    .flatMap(exists -> {
                        if (exists) {
                            return Mono.error(new CustomException(
                                    HttpStatus.BAD_REQUEST.value(),
                                    "Schedule code already exists",
                                    "Schedule code " + generatedCode + " already exists"));
                        }

                        DistributionSchedule schedule = DistributionSchedule.builder()
                                .organizationId(request.getOrganizationId())
                                .scheduleCode(generatedCode)
                                .zoneId(request.getZoneId())
                                .streetId(request.getStreetId())
                                .scheduleName(request.getScheduleName())
                                .daysOfWeek(request.getDaysOfWeek())
                                .startTime(request.getStartTime())
                                .endTime(request.getEndTime())
                                .durationHours(request.getDurationHours())
                                .status(Constants.ACTIVE.name())
                                .createdAt(Instant.now())
                                .build();

                        return repository.save(schedule)
                                .map(this::toEnrichedResponse);
                    })
            );
    }
    
    private EnrichedDistributionScheduleResponse toEnrichedResponse(DistributionSchedule schedule) {
        return EnrichedDistributionScheduleResponse.builder()
                .id(schedule.getId())
                .organizationId(schedule.getOrganizationId())
                .scheduleCode(schedule.getScheduleCode())
                .scheduleName(schedule.getScheduleName())
                .zoneId(schedule.getZoneId())
                .streetId(schedule.getStreetId())
                .daysOfWeek(schedule.getDaysOfWeek())
                .startTime(schedule.getStartTime())
                .endTime(schedule.getEndTime())
                .durationHours(schedule.getDurationHours())
                .status(schedule.getStatus())
                .createdAt(schedule.getCreatedAt())
                .build();
    }

    @Override
    public Mono<DistributionScheduleResponse> update(String id, DistributionScheduleCreateRequest request) {
        return repository.findById(id)
                .switchIfEmpty(Mono.error(new CustomException(
                        HttpStatus.NOT_FOUND.value(),
                        "Schedule not found",
                        "No schedule found with id " + id)))
                .flatMap(existing -> {
                    existing.setScheduleName(request.getScheduleName());
                    existing.setDaysOfWeek(request.getDaysOfWeek());
                    existing.setStartTime(request.getStartTime());
                    existing.setEndTime(request.getEndTime());
                    existing.setDurationHours(request.getDurationHours());
                    existing.setZoneId(request.getZoneId());
                    existing.setStreetId(request.getStreetId());
                    existing.setOrganizationId(request.getOrganizationId());
                    return repository.save(existing)
                            .map(saved -> DistributionScheduleResponse.builder()
                                    .id(saved.getId())
                                    .organizationId(saved.getOrganizationId())
                                    .scheduleCode(saved.getScheduleCode())
                                    .scheduleName(saved.getScheduleName())
                                    .zoneId(saved.getZoneId())
                                    .streetId(saved.getStreetId())
                                    .daysOfWeek(saved.getDaysOfWeek())
                                    .startTime(saved.getStartTime())
                                    .endTime(saved.getEndTime())
                                    .durationHours(saved.getDurationHours())
                                    .status(saved.getStatus())
                                    .createdAt(saved.getCreatedAt())
                                    .build());
                });
    }
}