package pe.edu.vallegrande.msdistribution.domain.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document(collection = "program")
public class DistributionProgram {

    @Id
    private String id;
    private String organizationId; 
    private String programCode;
    private String scheduleId;
    private String routeId; 
    private String zoneId;            
    private String streetId;    
    private LocalDate programDate;
    private String plannedStartTime;
    private String plannedEndTime;
    private String actualStartTime;
    private String actualEndTime;
    private String status;
    private String responsibleUserId;
    private String observations;
    private Instant createdAt;
}