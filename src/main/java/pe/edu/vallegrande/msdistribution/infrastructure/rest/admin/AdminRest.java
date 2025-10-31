package pe.edu.vallegrande.msdistribution.infrastructure.rest.admin;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import pe.edu.vallegrande.msdistribution.application.services.DistributionProgramService;
import pe.edu.vallegrande.msdistribution.application.services.DistributionRouteService;
import pe.edu.vallegrande.msdistribution.application.services.DistributionScheduleService;
import pe.edu.vallegrande.msdistribution.application.services.FareService;

import pe.edu.vallegrande.msdistribution.infrastructure.dto.ErrorMessage;
import pe.edu.vallegrande.msdistribution.infrastructure.dto.ResponseDto;
import pe.edu.vallegrande.msdistribution.infrastructure.dto.request.*;
import pe.edu.vallegrande.msdistribution.infrastructure.dto.response.*;
import pe.edu.vallegrande.msdistribution.infrastructure.dto.response.enriched.EnrichedDistributionProgramResponse;
import pe.edu.vallegrande.msdistribution.infrastructure.exception.CustomException;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/admin")
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Admin REST API", description = "Unified admin dashboard for distribution management")
@Slf4j
public class AdminRest {

        // Distribution Services
        private final DistributionProgramService programService;
        private final DistributionRouteService routeService;
        private final DistributionScheduleService scheduleService;
        private final FareService fareService;

        // ===============================
        // DASHBOARD & STATISTICS
        // ===============================

        @GetMapping("/dashboard/stats")
        @Operation(summary = "Get comprehensive dashboard statistics")
        public Mono<ResponseDto<Map<String, Object>>> getDashboardStats() {
                log.debug("Fetching comprehensive dashboard statistics");

                return Mono.zip(
                                programService.getAll().count(),
                                routeService.getAll().count(),
                                scheduleService.getAll().count(),
                                fareService.getAllF().count())
                                .map(tuple -> {
                                        Map<String, Object> stats = Map.of(
                                                        "totalPrograms", tuple.getT1(),
                                                        "totalRoutes", tuple.getT2(),
                                                        "totalSchedules", tuple.getT3(),
                                                        "totalFares", tuple.getT4(),
                                                        "lastUpdated", LocalDateTime.now(),
                                                        "systemStatus", "ACTIVE");
                                        return new ResponseDto<Map<String, Object>>(true, stats, null);
                                })
                                .onErrorResume(e -> {
                                        log.error("Error fetching dashboard stats: {}", e.getMessage());
                                        return Mono.just(new ResponseDto<Map<String, Object>>(false, null,
                                                        new ErrorMessage(500,
                                                                        "Error al obtener estadísticas del dashboard",
                                                                        e.getMessage())));
                                });
        }

        @GetMapping("/dashboard/summary")
        @Operation(summary = "Get distribution system summary")
        public Mono<ResponseDto<Map<String, Object>>> getSystemSummary() {
                log.debug("Fetching system summary for admin dashboard");

                return Mono.zip(
                                programService.getAll().collectList(),
                                routeService.getAllActive().count(),
                                scheduleService.getAllActive().count())
                                .map(tuple -> {
                                        var programs = tuple.getT1();
                                        long activePrograms = programs.stream()
                                                        .filter(p -> "ACTIVE".equals(p.getStatus())).count();
                                        long plannedPrograms = programs.stream()
                                                        .filter(p -> "PLANNED".equals(p.getStatus())).count();

                                        Map<String, Object> summary = Map.of(
                                                        "programs", Map.of(
                                                                        "total", programs.size(),
                                                                        "active", activePrograms,
                                                                        "planned", plannedPrograms),
                                                        "infrastructure", Map.of(
                                                                        "activeRoutes", tuple.getT2(),
                                                                        "activeSchedules", tuple.getT3()),
                                                        "timestamp", LocalDateTime.now());
                                        return new ResponseDto<Map<String, Object>>(true, summary, null);
                                })
                                .onErrorResume(e -> {
                                        log.error("Error fetching system summary: {}", e.getMessage());
                                        return Mono.just(new ResponseDto<Map<String, Object>>(false, null,
                                                        new ErrorMessage(500, "Error al obtener resumen del sistema",
                                                                        e.getMessage())));
                                });
        }

        // ===============================
        // DISTRIBUTION PROGRAM ENDPOINTS
        // ===============================

        @GetMapping("/program")
        @Operation(summary = "Get all distribution programs")
        public Mono<ResponseDto<List<DistributionProgramResponse>>> getAllPrograms() {
                return programService.getAll()
                                .collectList()
                                .map(this::success);
        }

        @GetMapping(value = "/program", params = "organizationId")
        // @Operation(summary = "Get programs by organization ID")
        public Mono<ResponseDto<List<DistributionProgramResponse>>> getProgramsByOrganizationId(
                        @RequestParam String organizationId) {
                return programService.getByOrganizationId(organizationId)
                                .collectList()
                                .map(list -> new ResponseDto<List<DistributionProgramResponse>>(true, list, null));
        }

        @GetMapping("/program/enriched")
        // @Operation(summary = "Get all enriched distribution programs")
        public Mono<ResponseDto<List<EnrichedDistributionProgramResponse>>> getAllEnrichedPrograms() {
                return programService.getAllEnriched()
                                .collectList()
                                .map(list -> new ResponseDto<List<EnrichedDistributionProgramResponse>>(true, list,
                                                null));
        }

        @GetMapping("/program/{id}")
        // @Operation(summary = "Get distribution program by ID")
        public Mono<ResponseDto<DistributionProgramResponse>> getProgramById(@PathVariable String id) {
                return programService.getById(id)
                                .map(data -> new ResponseDto<DistributionProgramResponse>(true, data, null))
                                .switchIfEmpty(Mono.error(CustomException.notFound("DistributionProgram", id)));
        }

        @PostMapping("/program")
        @Operation(summary = "Create a new distribution program")
        public Mono<ResponseEntity<ResponseDto<DistributionProgramResponse>>> createProgram(
                        @RequestBody DistributionProgramCreateRequest request) {
                return programService.save(request)
                                .map(data -> ResponseEntity.status(HttpStatus.CREATED)
                                                .body(new ResponseDto<DistributionProgramResponse>(true, data, null)));
        }

        @PutMapping("/program/{id}")
        // @Operation(summary = "Update a distribution program")
        public Mono<ResponseDto<DistributionProgramResponse>> updateProgram(@PathVariable String id,
                        @RequestBody DistributionProgramCreateRequest request) {
                return programService.update(id, request)
                                .map(data -> new ResponseDto<DistributionProgramResponse>(true, data, null))
                                .switchIfEmpty(Mono.error(CustomException.notFound("DistributionProgram", id)));
        }

        @DeleteMapping("/program/{id}")
        // @Operation(summary = "Delete a distribution program")
        public Mono<ResponseDto<Void>> deleteProgram(@PathVariable String id) {
                return programService.delete(id)
                                .then(Mono.just(new ResponseDto<Void>(true, null, null)));
        }

        @PatchMapping("/program/activate/{id}")
        // @Operation(summary = "Activate a distribution program")
        public Mono<ResponseDto<DistributionProgramResponse>> activateProgram(@PathVariable String id) {
                return programService.activate(id)
                                .map(data -> new ResponseDto<DistributionProgramResponse>(true, data, null))
                                .switchIfEmpty(Mono.error(CustomException.notFound("DistributionProgram", id)));
        }

        @PatchMapping("/program/deactivate/{id}")
        // @Operation(summary = "Deactivate a distribution program")
        public Mono<ResponseDto<DistributionProgramResponse>> deactivateProgram(@PathVariable String id) {
                return programService.desactivate(id)
                                .map(data -> new ResponseDto<DistributionProgramResponse>(true, data, null))
                                .switchIfEmpty(Mono.error(CustomException.notFound("DistributionProgram", id)));
        }

        // ===============================
        // DISTRIBUTION ROUTE ENDPOINTS
        // ===============================

        @GetMapping("/route")
        // @Operation(summary = "Get all distribution routes")
        public Mono<ResponseDto<List<DistributionRouteResponse>>> getAllRoutes() {
                return routeService.getAll()
                                .map(this::convertToResponse)
                                .collectList()
                                .map(list -> new ResponseDto<List<DistributionRouteResponse>>(true, list, null));
        }

        private DistributionRouteResponse convertToResponse(
                        pe.edu.vallegrande.msdistribution.domain.models.DistributionRoute route) {
                // Convertir las zonas para la respuesta
                String firstZoneId = null;
                java.util.List<DistributionRouteResponse.ZoneDetail> zoneDetails = 
                    new java.util.ArrayList<>(); // Usar ArrayList en lugar de Collections.emptyList()
                    
                if (route.getZones() != null && !route.getZones().isEmpty()) {
                    try {
                        // Primer zoneId para compatibilidad
                        firstZoneId = route.getZones().get(0).getZoneId();
                        
                        // Lista completa de zonas
                        zoneDetails = route.getZones().stream()
                            .map(zone -> new DistributionRouteResponse.ZoneDetail(
                                zone.getZoneId(),
                                zone.getOrder(),
                                zone.getEstimatedDuration()))
                            .collect(java.util.stream.Collectors.toList());
                    } catch (Exception e) {
                        // Manejar cualquier excepción en la conversión de zonas
                        log.warn("Error al convertir zonas para la ruta {}: {}", route.getId(), e.getMessage());
                    }
                }
                
                return DistributionRouteResponse.builder()
                                .id(route.getId())
                                .organizationId(route.getOrganizationId())
                                .routeCode(route.getRouteCode())
                                .routeName(route.getRouteName())
                                .zoneId(firstZoneId)
                                .zones(zoneDetails)
                                .totalEstimatedDuration(route.getTotalEstimatedDuration())
                                .responsibleUserId(route.getResponsibleUserId())
                                .status(route.getStatus())
                                .createdAt(route.getCreatedAt())
                                .build();
        }

        private DistributionScheduleResponse convertToScheduleResponse(
                        pe.edu.vallegrande.msdistribution.domain.models.DistributionSchedule schedule) {
                return DistributionScheduleResponse.builder()
                                .id(schedule.getId())
                                .organizationId(schedule.getOrganizationId())
                                .scheduleCode(schedule.getScheduleCode())
                                .zoneId(schedule.getZoneId())
                                .streetId(schedule.getStreetId())
                                .scheduleName(schedule.getScheduleName())
                                .startTime(schedule.getStartTime())
                                .endTime(schedule.getEndTime())
                                .daysOfWeek(schedule.getDaysOfWeek())
                                .durationHours(schedule.getDurationHours())
                                .status(schedule.getStatus())
                                .createdAt(schedule.getCreatedAt())
                                .build();
        }

        private <T> ResponseDto<T> success(T data) {
                return new ResponseDto<>(true, data, null);
        }

        @GetMapping("/route/active")
        // @Operation(summary = "Get all active distribution routes")
        public Mono<ResponseDto<List<DistributionRouteResponse>>> getAllActiveRoutes() {
                return routeService.getAllActive()
                                .map(this::convertToResponse)
                                .collectList()
                                .map(list -> new ResponseDto<List<DistributionRouteResponse>>(true, list, null));
        }

        @GetMapping("/route/{id}")
        // @Operation(summary = "Get distribution route by ID")
        public Mono<ResponseDto<DistributionRouteResponse>> getRouteById(@PathVariable String id) {
                return routeService.getById(id)
                                .map(this::convertToResponse)
                                .map(data -> new ResponseDto<DistributionRouteResponse>(true, data, null))
                                .switchIfEmpty(Mono.error(CustomException.notFound("DistributionRoute", id)));
        }

        @PostMapping("/route")
        // @Operation(summary = "Create a new distribution route")
        public Mono<ResponseEntity<ResponseDto<DistributionRouteResponse>>> createRoute(
                        @RequestBody DistributionRouteCreateRequest request) {
                return routeService.save(request)
                                .map(data -> ResponseEntity.status(HttpStatus.CREATED)
                                                .body(new ResponseDto<DistributionRouteResponse>(true, data, null)));
        }

        @PutMapping("/route/{id}")
        // @Operation(summary = "Update a distribution route")
        public Mono<ResponseDto<DistributionRouteResponse>> updateRoute(@PathVariable String id,
                        @RequestBody DistributionRouteCreateRequest request) {
                return routeService.update(id, request)
                                .map(data -> new ResponseDto<DistributionRouteResponse>(true, data, null))
                                .switchIfEmpty(Mono.error(CustomException.notFound("DistributionRoute", id)));
        }

        @DeleteMapping("/route/{id}")
        // @Operation(summary = "Delete a distribution route")
        public Mono<ResponseDto<Void>> deleteRoute(@PathVariable String id) {
                return routeService.delete(id)
                                .then(Mono.just(new ResponseDto<Void>(true, null, null)));
        }

        @PatchMapping("/route/activate/{id}")
        // @Operation(summary = "Activate a distribution route")
        public Mono<ResponseDto<DistributionRouteResponse>> activateRoute(@PathVariable String id) {
                return routeService.activate(id)
                                .map(this::convertToResponse)
                                .map(data -> new ResponseDto<DistributionRouteResponse>(true, data, null))
                                .switchIfEmpty(Mono.error(CustomException.notFound("DistributionRoute", id)));
        }

        @PatchMapping("/route/deactivate/{id}")
        // @Operation(summary = "Deactivate a distribution route")
        public Mono<ResponseDto<DistributionRouteResponse>> deactivateRoute(@PathVariable String id) {
                return routeService.deactivate(id)
                                .map(this::convertToResponse)
                                .map(data -> new ResponseDto<DistributionRouteResponse>(true, data, null))
                                .switchIfEmpty(Mono.error(CustomException.notFound("DistributionRoute", id)));
        }

        // ===============================
        // DISTRIBUTION SCHEDULE ENDPOINTS
        // ===============================

        @GetMapping("/schedule")
        // @Operation(summary = "Get all distribution schedules")
        public Mono<ResponseDto<List<DistributionScheduleResponse>>> getAllSchedules() {
                return scheduleService.getAll()
                                .map(this::convertToScheduleResponse)
                                .collectList()
                                .map(list -> new ResponseDto<List<DistributionScheduleResponse>>(true, list, null));
        }

        @GetMapping("/schedule/active")
        // @Operation(summary = "Get all active distribution schedules")
        public Mono<ResponseDto<List<DistributionScheduleResponse>>> getAllActiveSchedules() {
                return scheduleService.getAllActive()
                                .map(this::convertToScheduleResponse)
                                .collectList()
                                .map(list -> new ResponseDto<List<DistributionScheduleResponse>>(true, list, null));
        }

        @GetMapping("/schedule/{id}")
        // @Operation(summary = "Get distribution schedule by ID")
        public Mono<ResponseDto<DistributionScheduleResponse>> getScheduleById(@PathVariable String id) {
                return scheduleService.getById(id)
                                .map(this::convertToScheduleResponse)
                                .map(data -> new ResponseDto<DistributionScheduleResponse>(true, data, null))
                                .switchIfEmpty(Mono.error(CustomException.notFound("DistributionSchedule", id)));
        }

        @PostMapping("/schedule")
        // @Operation(summary = "Create a new distribution schedule")
        public Mono<ResponseEntity<ResponseDto<DistributionScheduleResponse>>> createSchedule(
                        @RequestBody DistributionScheduleCreateRequest request) {
                return scheduleService.save(request)
                                .map(data -> ResponseEntity.status(HttpStatus.CREATED)
                                                .body(new ResponseDto<DistributionScheduleResponse>(true, data, null)));
        }

        @PutMapping("/schedule/{id}")
        // @Operation(summary = "Update a distribution schedule")
        public Mono<ResponseDto<DistributionScheduleResponse>> updateSchedule(@PathVariable String id,
                        @RequestBody DistributionScheduleCreateRequest request) {
                log.debug("Received update request for schedule id: {} with data: {}", id, request);
                
                return scheduleService.update(id, request)
                                .map(data -> {
                                log.debug("Schedule updated successfully: {}", data);
                                return new ResponseDto<DistributionScheduleResponse>(true, data, null);
                                })
                                .switchIfEmpty(Mono.error(CustomException.notFound("DistributionSchedule", id)))
                                .onErrorResume(throwable -> {
                                        // Log the error for debugging
                                        log.error("Error updating schedule with id: " + id, throwable);
                                        // Return a proper error response
                                        String errorMessage = "Error al actualizar el horario: " + 
                                        (throwable.getMessage() != null ? throwable.getMessage() : "Error interno del servidor");
                                        return Mono.just(new ResponseDto<DistributionScheduleResponse>(false, null, 
                                                new ErrorMessage(500, errorMessage, throwable.getClass().getSimpleName())));
                                });
        }

        @DeleteMapping("/schedule/{id}")
        // @Operation(summary = "Delete a distribution schedule")
        public Mono<ResponseDto<Void>> deleteSchedule(@PathVariable String id) {
                return scheduleService.delete(id)
                                .then(Mono.just(new ResponseDto<Void>(true, null, null)));
        }

        @PatchMapping("/schedule/activate/{id}")
        // @Operation(summary = "Activate a distribution schedule")
        public Mono<ResponseDto<DistributionScheduleResponse>> activateSchedule(@PathVariable String id) {
                return scheduleService.activate(id)
                                .map(this::convertToScheduleResponse)
                                .map(data -> new ResponseDto<DistributionScheduleResponse>(true, data, null))
                                .switchIfEmpty(Mono.error(CustomException.notFound("DistributionSchedule", id)));
        }

        @PatchMapping("/schedule/deactivate/{id}")
        // @Operation(summary = "Deactivate a distribution schedule")
        public Mono<ResponseDto<DistributionScheduleResponse>> deactivateSchedule(@PathVariable String id) {
                return scheduleService.deactivate(id)
                                .map(this::convertToScheduleResponse)
                                .map(data -> new ResponseDto<DistributionScheduleResponse>(true, data, null))
                                .switchIfEmpty(Mono.error(CustomException.notFound("DistributionSchedule", id)));
        }

        // ===============================
        // FARE ENDPOINTS
        // ===============================

        @GetMapping("/fare")
        // @Operation(summary = "Get all fares")
        public Mono<ResponseDto<List<FareResponse>>> getAllFares() {
                return fareService.getAllF()
                                .collectList()
                                .map(list -> new ResponseDto<List<FareResponse>>(true, list, null));
        }

        @GetMapping("/fare/active")
        // @Operation(summary = "Get all active fares")
        public Mono<ResponseDto<List<FareResponse>>> getAllActiveFares() {
                return fareService.getAllActiveF()
                                .collectList()
                                .map(list -> new ResponseDto<List<FareResponse>>(true, list, null));
        }

        @GetMapping("/fare/{id}")
        // @Operation(summary = "Get fare by ID")
        public Mono<ResponseDto<FareResponse>> getFareById(@PathVariable String id) {
                return fareService.getByIdF(id)
                                .map(data -> new ResponseDto<FareResponse>(true, data, null))
                                .switchIfEmpty(Mono.error(CustomException.notFound("Fare", id)));
        }

        @PostMapping("/fare")
        // @Operation(summary = "Create a new fare")
        public Mono<ResponseEntity<ResponseDto<FareResponse>>> createFare(@RequestBody FareCreateRequest request) {
                return fareService.saveF(request)
                                .map(data -> ResponseEntity.status(HttpStatus.CREATED)
                                                .body(new ResponseDto<FareResponse>(true, data, null)));
        }

        @PutMapping("/fare/{id}")
        // @Operation(summary = "Update a fare")
        public Mono<ResponseDto<FareResponse>> updateFare(@PathVariable String id,
                        @RequestBody FareCreateRequest request) {
                return fareService.updateF(id, request)
                                .map(data -> new ResponseDto<FareResponse>(true, data, null))
                                .switchIfEmpty(Mono.error(CustomException.notFound("Fare", id)));
        }

        @DeleteMapping("/fare/{id}")
        // @Operation(summary = "Delete a fare")
        public Mono<ResponseDto<Void>> deleteFare(@PathVariable String id) {
                return fareService.deleteF(id)
                                .then(Mono.just(new ResponseDto<Void>(true, null, null)));
        }

        @PatchMapping("/fare/{id}/activate")
        // @Operation(summary = "Activate a fare")
        public Mono<ResponseDto<FareResponse>> activateFare(@PathVariable String id) {
                return fareService.activateF(id)
                                .map(data -> new ResponseDto<FareResponse>(true, data, null))
                                .switchIfEmpty(Mono.error(CustomException.notFound("Fare", id)));
        }

        @PatchMapping("/fare/{id}/deactivate")
        // @Operation(summary = "Deactivate a fare")
        public Mono<ResponseDto<FareResponse>> deactivateFare(@PathVariable String id) {
                return fareService.deactivateF(id)
                                .map(data -> new ResponseDto<FareResponse>(true, data, null))
                                .switchIfEmpty(Mono.error(CustomException.notFound("Fare", id)));
        }
        
        @PostMapping("/fare/process-transitions")
        @Operation(summary = "Process fare transitions based on effective dates")
        public Mono<ResponseDto<String>> processFareTransitions() {
                fareService.triggerFareTransitions();
                return Mono.just(new ResponseDto<String>(true, "Fare transitions processing triggered successfully", null));
        }
}