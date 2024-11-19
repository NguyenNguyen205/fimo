package com.mock.user_service.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "users")
@Entity
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "uid")
    private UUID id;
    @Column(name = "code", unique = true, nullable = false, length = 50)
    private String code;
    @Column(name = "username", length = 50)
    private String username;
    @Column(name = "password", length = 32)
    private String password;
    @Column(name = "fullname", length = 50)
    private String fullname;
    @Column(name = "email", unique = true, length = 50)
    private String email;
    @Column(name = "tier", length = 14)
    private String tier;
    @CreationTimestamp
    @Column(name = "create_date")
    private LocalDateTime createdDateTime;
    @UpdateTimestamp
    @Column(name = "update_date")
    private LocalDateTime updatedDateTime;
    @Column(name = "isDeleted")
    private boolean deleted;
}
