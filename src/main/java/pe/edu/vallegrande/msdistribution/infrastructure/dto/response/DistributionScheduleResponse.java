package pe.edu.vallegrande.msdistribution.infrastructure.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * Response DTO for Distribution Schedule entities.
 * Contains all the information related to a distribution schedule.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DistributionScheduleResponse {
    
    private String id;
    private String organizationId;
    private String scheduleCode;
    private String zoneId;
    private String streetId;
    private String scheduleName;
    @Builder.Default
    private List<String> daysOfWeek = new ArrayList<>();
    private String startTime;
    private String endTime;
    private Integer durationHours;
    private String status;
    private Instant createdAt;
}