package com.fimo.video_service.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/videos")
public class VideoController {

    public VideoController() {}

    @Operation(summary = "Check user service health")
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
}
