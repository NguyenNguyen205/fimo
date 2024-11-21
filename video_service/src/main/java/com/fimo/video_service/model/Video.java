package com.fimo.video_service.model;

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
@Table(name = "videos")
@Entity
public class Video {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "uid")
    private UUID id;
    @Column(name = "code", unique = true, nullable = false, length = 50)
    private String code;
    @Column(name = "title", length = 50)
    private String title;
    @Column(name = "description", length = 500)
    private String description;
    @Column(name = "filename", length = 50)
    private String filename;
    @Column(name = "type", length = 50)
    private String filetype;
    @CreationTimestamp
    @Column(name = "create_date")
    private LocalDateTime createdDateTime;
    @UpdateTimestamp
    @Column(name = "update_date")
    private LocalDateTime updatedDateTime;
    @Column(name = "isDeleted")
    private boolean deleted;
}
