package pe.edu.vallegrande.msdistribution.infrastructure.dto.response.enriched;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import pe.edu.vallegrande.msdistribution.infrastructure.client.dto.ExternalOrganization;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EnrichedFareResponse {
    private String id;
    private String organizationId;
    private ExternalOrganization organization; // Organization details
    private String fareCode;
    private String fareName;
    private String fareType;
    private BigDecimal fareAmount;
    private String status;
    private Instant createdAt;
    private Date effectiveDate;
}