package pe.edu.vallegrande.msdistribution.domain.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document(collection = "user")
public class User {
    @Id
    private String id;
    private String organizationId;
    private String userCode;
    private String fullName;
    private String documentType;
    private String documentNumber;
    private String email;
    private String phone;
    private String role;
    private String status;
    private Instant createdAt;
}