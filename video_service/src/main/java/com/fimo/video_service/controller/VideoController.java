package com.fimo.video_service.controller;

import com.fimo.video_service.dto.CreateVideoRequestDTO;
import com.fimo.video_service.dto.VideoResponseDTO;
import com.fimo.video_service.dto.WatchResponseDTO;
import com.fimo.video_service.model.Video;
import com.fimo.video_service.service.VideoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/videos")
public class VideoController {
    private VideoService videoService;

    @Autowired
    public VideoController(VideoService videoService) {
        this.videoService = videoService;
    }

    @Operation(summary = "Check video service health")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Service is Ok",
                    content = { @Content(mediaType = "application/json", examples = @ExampleObject(value = "{ \"status\":\"Ok\" }"))})}
    )
    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> checkApiHealth() {
        HashMap<String, String> res = new HashMap<>();
        res.put("status", "OK");
        return new ResponseEntity<> (res, HttpStatus.OK);
    }

    @Operation(summary = "List all videos")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "List all video successfully",
                    content = { @Content(mediaType = "application/json") }
            ),
            @ApiResponse(responseCode = "500", description = "Server error", content = {
                    @Content(mediaType = "application/json")
            })
    })
    @GetMapping("/")
    public ResponseEntity getAllVideos() {
        List<VideoResponseDTO> res = new ArrayList<>();
        try {
            res = videoService.getAllVideos();
        } catch (Exception e) {
            HashMap<String, String> error = new HashMap<>();
            error.put("message", "Server error");
            return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return new ResponseEntity<>(res, HttpStatus.OK);
    }

    // Maybe more handy to upload file from here, and create file in bucket source
    @Operation(summary = "Create video metadata")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Video metadata created successfully", content = {
                    @Content(mediaType = "application/json")
            })
    })
    @PostMapping
    public ResponseEntity createVideo(@RequestHeader("Idempotency-Key") String idemKey, @RequestBody CreateVideoRequestDTO video) {
        VideoResponseDTO res = null;
        try {
            res = videoService.createVideoWithIdempotency(idemKey, video);
        }
        catch (Exception e) {
            HashMap<String, String> error = new HashMap<>();
            error.put("message", e.getMessage());
            return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return new ResponseEntity<> (res, HttpStatus.CREATED);
    }

    @Operation(summary = "Watch a video from the provided code")
    @GetMapping("/watch")
    public ResponseEntity watchVideo(@RequestParam String id) {
        WatchResponseDTO res = null;
        try {
            res = videoService.watchVideo(id);
        }
        catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return new ResponseEntity<> (res, HttpStatus.OK);
    }
}
