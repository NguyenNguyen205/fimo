package com.fimo.video_service.service;

import com.fimo.video_service.dto.CreateVideoRequestDTO;
import com.fimo.video_service.dto.VideoResponseDTO;
import com.fimo.video_service.dto.WatchResponseDTO;
import com.fimo.video_service.model.IdempotencyKey;
import com.fimo.video_service.model.Video;
import com.fimo.video_service.repository.IdempotencyKeyRepository;
import com.fimo.video_service.repository.VideoRepository;
import jakarta.servlet.ServletContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class VideoService {

    private VideoRepository videoRepository;
    private IdempotencyKeyRepository idempotencyKeyRepository;

    @Autowired
    public VideoService(VideoRepository videoRepository, IdempotencyKeyRepository idempotencyKeyRepository) {
        this.videoRepository = videoRepository;
        this.idempotencyKeyRepository = idempotencyKeyRepository;
    }

    public VideoResponseDTO createVideoWithIdempotency(String key, CreateVideoRequestDTO createVideoRequestDTO) throws Exception {
// Check for idempotency
        IdempotencyKey savedKey = idempotencyKeyRepository.findById(key).orElse(null);
        if (savedKey != null) {
            if (savedKey.getExpires().isBefore(LocalDateTime.now())) {
                idempotencyKeyRepository.delete(savedKey);
            } else {
                System.out.println("Key found");
                Video video = videoRepository.findById(savedKey.getVideoId()).orElse(null);
                VideoResponseDTO videoResponseDTO = convertToVideoResponseDTO(video);
                return videoResponseDTO;
            }
        }

        // Create new user
        String code = UUID.randomUUID().toString().substring(0, 8);
        String extension = "";
        int i = createVideoRequestDTO.getFilename().lastIndexOf('.');
        if (i > 0) {
            extension = createVideoRequestDTO.getFilename().substring(i + 1);
        }
        else {
            throw new Exception("Filename not found");
        }
        Video video = Video.builder()
                .code(code)
                .title(createVideoRequestDTO.getTitle())
                .description(createVideoRequestDTO.getDescription())
                .filename(createVideoRequestDTO.getFilename())
                .filetype(extension)
                .deleted(false)
                .build();
        video = videoRepository.save(video);

        // Save idempotency key
        IdempotencyKey idempotencyKey = IdempotencyKey.builder()
                .keyId(key)
                .videoId(video.getId())
                .expires(LocalDateTime.now().plusHours(24))
                .build();
        idempotencyKeyRepository.save(idempotencyKey);

        // Return value
        VideoResponseDTO videoResponseDTO = convertToVideoResponseDTO(video);
        return videoResponseDTO;

    }

    public List<VideoResponseDTO> getAllVideos() throws Exception {
        List<Video> videos = videoRepository.findAll();
        List<VideoResponseDTO> videosDTO = new ArrayList<VideoResponseDTO>();
        for (Video video : videos) {
            videosDTO.add(convertToVideoResponseDTO(video));
        }
        // By default, sort list by created date
        Collections.sort(videosDTO, new Comparator<VideoResponseDTO>() {
            @Override
            public int compare(VideoResponseDTO o1, VideoResponseDTO o2) {
                return o2.getCreatedDateTime().compareTo(o1.getCreatedDateTime());
            }
        });
        return videosDTO;
    }

    public WatchResponseDTO watchVideo(String code) throws Exception {
        // Find video by code (id)
        Video video = videoRepository.findByCode(code);
        if (video == null) {
            throw new Exception("Video not found");
        }
        WatchResponseDTO watchResponseDTO = new WatchResponseDTO();
        watchResponseDTO.setVideoResponseDTO(convertToVideoResponseDTO(video));

        // Find source video path
        String rootPath = new File("").getAbsolutePath();
        String videoPath = Paths.get(rootPath.toString(), "..", "storage", "bucket_source", code, video.getFilename()).toString();
        String outputPath = Paths.get(rootPath.toString(), "..", "storage", "bucket_destination", code).toString();
        // Packaging to mpeg dash
        Files.createDirectories(Paths.get(outputPath));

        String createAudioCommand = String.format("ffmpeg -i \"%s\" -vn -acodec libvorbis -ab 128k -dash 1 \"%s\\my_audio.webm\"", videoPath, outputPath);
        String packagingCommand = String.format("ffmpeg -i \"%s\" -c:v libvpx-vp9 -keyint_min 150 -g 150 -tile-columns 4 -frame-parallel 1 -f webm -dash 1 -an -vf scale=160:90 -b:v 250k -dash 1 \"%s\\video_160x90_250k.webm\" -an -vf scale=320:180 -b:v 500k -dash 1 \"%s\\video_320x180_500k.webm\"", videoPath, outputPath, outputPath);
        String createMpdCommand = String.format("ffmpeg -f webm_dash_manifest -i video_160x90_250k.webm -f webm_dash_manifest -i video_320x180_500k.webm -f webm_dash_manifest -i my_audio.webm -c copy -map 0 -map 1 -map 2 -f webm_dash_manifest -adaptation_sets \"id=0,streams=0,1 id=1,streams=2\" video.mpd");

        ProcessBuilder processBuilder = new ProcessBuilder("cmd", "/c", createAudioCommand);
        processBuilder.inheritIO();
        Process process = processBuilder.start();
        int exit = process.waitFor();
        if (exit != 0) {
            throw new Exception("Error creating audio file");
        }

        ProcessBuilder processBuilder1 = new ProcessBuilder("cmd", "/c", packagingCommand);
        processBuilder1.inheritIO();
        Process process1 = processBuilder1.start();
        int exit1 = process1.waitFor();
        if (exit1 != 0) {
            throw new Exception("Error packaging file");
        }

        ProcessBuilder processBuilder2 = new ProcessBuilder("cmd", "/c", createMpdCommand);
        processBuilder2.directory(new File(outputPath));
        processBuilder2.inheritIO();
        Process process2 = processBuilder2.start();
        int exit2 = process2.waitFor();
        if (exit2 != 0) {
            throw new Exception("Error creating mpd file");
        }
        // Return watch object (including cdn url)
        String basecdnurl = "http://localhost/videos/";
        watchResponseDTO.setUrl(basecdnurl + code + "/video.mpd");
        return watchResponseDTO;
    }

    private VideoResponseDTO convertToVideoResponseDTO(Video video) {
        VideoResponseDTO videoResponseDTO = VideoResponseDTO.builder()
                .code(video.getCode())
                .title(video.getTitle())
                .description(video.getDescription())
                .filename(video.getFilename())
                .filetype(video.getFiletype())
                .createdDateTime(video.getCreatedDateTime())
                .updatedDateTime(video.getUpdatedDateTime())
                .build();
        return videoResponseDTO;
    }

}
