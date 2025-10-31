package pe.edu.vallegrande.msdistribution.infrastructure.client.dto;

import lombok.Data;

@Data
public class ExternalZone {
    private String zoneCode;
    private String zoneId;
    private String status;
    private String zoneName;
    private String description;
}
