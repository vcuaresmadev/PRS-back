package pe.edu.vallegrande.msdistribution.domain.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder 
@Document(collection = "fare")
public class Fare {
    @Id
    private String id;
    private String organizationId;
    private String fareCode;
    private String fareName;
    private String fareType;
    private BigDecimal fareAmount;
    private Date effectiveDate; 
    private String status;
    private Instant createdAt;
}