package pe.edu.vallegrande.msdistribution.infrastructure.client.dto;

import lombok.Data;
import java.util.List;

@Data
public class UserApiResponse {
    private boolean success;
    private String message;
    private List<ExternalUser> data;
}
