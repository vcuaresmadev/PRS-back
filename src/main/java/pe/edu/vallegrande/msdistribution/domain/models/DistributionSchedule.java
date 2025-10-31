package pe.edu.vallegrande.msdistribution.domain.models;

import java.time.Instant;
import java.util.ArrayList;
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
@Document(collection = "schedule")
public class DistributionSchedule {
    @Id
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