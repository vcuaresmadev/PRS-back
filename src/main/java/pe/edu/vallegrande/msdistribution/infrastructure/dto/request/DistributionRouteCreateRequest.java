package pe.edu.vallegrande.msdistribution.infrastructure.dto.request;

import lombok.*;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DistributionRouteCreateRequest {

    private String organizationId;
    private String routeCode;
    private String routeName;
    private List<ZoneEntry> zones;
    private Integer totalEstimatedDuration; // en horas
    private String responsibleUserId;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ZoneEntry {
        private String zoneId;
        private Integer order;
        private Integer estimatedDuration; // en horas
    }
}