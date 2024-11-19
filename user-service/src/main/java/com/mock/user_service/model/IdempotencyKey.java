package com.mock.user_service.model;

import com.mock.user_service.dto.UserResponseDTO;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "idempotencyKeys")
public class IdempotencyKey {
    @Id
    private String keyId;
    private UUID userId;
    private String jsonValue;
    private LocalDateTime expires;
}
