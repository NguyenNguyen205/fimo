package com.fimo.video_service.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

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
    private UUID videoId;
    private String jsonValue;
    private LocalDateTime expires;
}
