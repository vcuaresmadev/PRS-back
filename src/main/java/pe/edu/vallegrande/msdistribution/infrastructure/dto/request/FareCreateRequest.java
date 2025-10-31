package pe.edu.vallegrande.msdistribution.infrastructure.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Date;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FareCreateRequest {

    private String organizationId;
    private String fareName;
    private String fareType; // SEMANAL, MENSUAL, ANUAL
    private BigDecimal fareAmount;
    private Date effectiveDate; // New field for time-based fare changes
}