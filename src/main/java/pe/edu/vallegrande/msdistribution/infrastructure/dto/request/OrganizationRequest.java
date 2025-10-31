package pe.edu.vallegrande.msdistribution.infrastructure.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrganizationRequest {
    private String organizationCode;
    private String organizationName;
    private String legalRepresentative;
    private String address;
    private String phone;
    private String logo;
}