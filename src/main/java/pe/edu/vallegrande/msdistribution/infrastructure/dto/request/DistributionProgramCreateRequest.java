package pe.edu.vallegrande.msdistribution.infrastructure.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DistributionProgramCreateRequest {
    
    private String organizationId;
    // programCode se genera automáticamente, no se recibe en la solicitud
    private String scheduleId;
    private String routeId;
    private String zoneId;
    private String streetId;
    private LocalDate programDate;
    private String plannedStartTime;
    private String plannedEndTime;
    private String actualStartTime;
    private String actualEndTime;
    private String responsibleUserId;
    private String observations;
}