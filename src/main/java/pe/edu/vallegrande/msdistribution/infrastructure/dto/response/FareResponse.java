package pe.edu.vallegrande.msdistribution.infrastructure.dto.response;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Date;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FareResponse {
    private String id;
    private String organizationId;

    private String fareCode;
    private String fareName;
    private String fareType;

    private BigDecimal fareAmount;

    private String status;
    private Instant createdAt;
    private Date effectiveDate; 
}
