package com.fimo.video_service.repository;

import com.fimo.video_service.model.Video;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface VideoRepository extends JpaRepository<Video, UUID> {
    Video findByCode(String code);
}
