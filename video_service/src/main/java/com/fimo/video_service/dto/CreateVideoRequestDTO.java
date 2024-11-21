package com.fimo.video_service.dto;

import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
// This class only create the metadata of the video file, the official file will be uploaded manually to the storage bucket
public class CreateVideoRequestDTO {
    @NotEmpty(message = "Required")
    private String title;
    private String description;
    @NotEmpty(message = "Required")
    private String filename;
}
