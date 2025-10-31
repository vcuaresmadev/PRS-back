package pe.edu.vallegrande.msdistribution.infrastructure.dto.request;

import lombok.*;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DistributionScheduleCreateRequest {

    private String organizationId;
    private String scheduleCode;
    private String zoneId;
    private String streetId; 
    private String scheduleName;
    @Builder.Default
    private List<String> daysOfWeek = new ArrayList<>();
    private String startTime;   // Formato HH:mm
    private String endTime;     // Formato HH:mm
    private Integer durationHours;
}