package pe.edu.vallegrande.msdistribution.domain.models;

import java.time.Instant;
import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document(collection = "route")
public class DistributionRoute {
    @Id
    private String id;
    private String organizationId;
    private String routeCode;
    private String routeName;
    private List<ZoneOrder> zones;
    private int totalEstimatedDuration; 
    private String responsibleUserId;
    private String status;
    private Instant createdAt;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ZoneOrder {
        private String zoneId;
        private int order;
        private int estimatedDuration; // en horas
    }
}