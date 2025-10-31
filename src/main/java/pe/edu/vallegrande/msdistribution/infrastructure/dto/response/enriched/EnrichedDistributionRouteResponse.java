package pe.edu.vallegrande.msdistribution.infrastructure.dto.response.enriched;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import pe.edu.vallegrande.msdistribution.infrastructure.client.dto.ExternalOrganization;

import java.time.Instant;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EnrichedDistributionRouteResponse {
    private String id;
    private String organizationId;
    private ExternalOrganization organization; // Organization details
    private String routeCode;
    private String routeName;
    private String zoneId;
    private List<ZoneDetail> zones; // Nuevo campo para la lista de zonas
    private Integer totalEstimatedDuration;
    private String responsibleUserId;
    private String status;
    private Instant createdAt;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ZoneDetail {
        private String zoneId;
        private Integer order;
        private Integer estimatedDuration;
    }
}