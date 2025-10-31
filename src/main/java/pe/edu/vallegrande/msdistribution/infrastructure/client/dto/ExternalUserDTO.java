package pe.edu.vallegrande.msdistribution.infrastructure.client.dto;

import lombok.Data;

@Data
public class ExternalUserDTO {
    private String id;
    private String name;
    private String email;
    private String role;
}
