package com.mock.user_service.controller;

import com.mock.user_service.dto.CreateUserRequestDTO;
import com.mock.user_service.dto.UpdateUserRequestDTO;
import com.mock.user_service.dto.UserResponseDTO;
import com.mock.user_service.model.User;
import com.mock.user_service.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.GroupSequence;
import jakarta.websocket.server.PathParam;
import net.minidev.json.JSONObject;
import org.apache.coyote.Response;
import org.hibernate.sql.Update;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.query.Param;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.parameters.P;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.*;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {
    private UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

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

    @Operation(summary = "List all users")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "List all user successfully",
                    content = { @Content(mediaType = "application/json") }
            ),
            @ApiResponse(responseCode = "500", description = "Server error", content = {
                    @Content(mediaType = "application/json")
            })
    })
    @GetMapping("")
    public ResponseEntity getAllUsers(@RequestParam(required = false) Boolean isDeleted, @RequestParam(required = false) String fullname, @RequestParam(required = false) String tier, @RequestParam(required = false) @DateTimeFormat(pattern = "MM-dd-yyyy") Date fromDate) {
        List<UserResponseDTO> res = new ArrayList<>();
        try {
            res = userService.getAllUsers(isDeleted, fullname, tier, fromDate);
        } catch (Exception e) {
            HashMap<String, String> error = new HashMap<>();
            error.put("message", "Server error");
            return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return new ResponseEntity<>(res, HttpStatus.OK);
    }

    @Operation(summary = "Get a user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Get detail from a specify user",
                    content = { @Content(mediaType = "application/json") }
            ),
            @ApiResponse(responseCode = "404", description = "User not found",
                    content = { @Content(mediaType = "application/json") }
            ),
            @ApiResponse(responseCode = "500", description = "Server error", content = {
                    @Content(mediaType = "application/json")
            })
    })
    @GetMapping("/{id}")
    public ResponseEntity getUser(@PathVariable String id) {
        System.out.println(id);
        UserResponseDTO res = null;
        try {
            res = userService.getUser(id);
        } catch (Exception e) {
            switch (e.getMessage()){
                case "Not found": {
                    HashMap<String, String> error = new HashMap<>();
                    error.put("message", "User not found");
                    return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
                }
                case "User deleted": {
                    HashMap<String, String> error = new HashMap<>();
                    error.put("message", "User is already deleted");
                    return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
                }
            }
        }
        return new ResponseEntity<>(res, HttpStatus.OK);
    }

    @Operation(summary = "Create new user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "User created successfully", content = {
                    @Content(mediaType = "application/json", examples = @ExampleObject(value = "{\n" +
                            "  \"id\": \"b037d6e9-5da7-458e-bfbb-f6166a5e2d03\",\n" +
                            "  \"code\": \"b037d6e9\",\n" +
                            "  \"username\": \"kreden\",\n" +
                            "  \"fullname\": \"kreden com\",\n" +
                            "  \"email\": \"gmail@kreden.com\",\n" +
                            "  \"tier\": \"free\",\n" +
                            "  \"createdAt\": \"05-11-2024 15:16:29\",\n" +
                            "  \"updatedAt\": \"05-11-2024 15:16:29\",\n" +
                            "  \"deleted\": false\n" +
                            "}"))
            }),
            @ApiResponse(responseCode = "409", description = "Already used field", content = {
                    @Content(mediaType = "application/json", examples = @ExampleObject(value = "{" +
                            "  \"message\": \" Email already existed \"" +
                            "}"))
            })

    })
    @PostMapping
    public ResponseEntity createUser(@RequestHeader("Idempotency-Key") String idemKey, @RequestBody CreateUserRequestDTO user) {
        UserResponseDTO res = null;
        try {
            res = userService.createUserWithIdempotency(idemKey, user);
        }
        catch (Exception e) {
            switch (e.getMessage()) {
                case "Email existed": {
                    HashMap<String, String> error = new HashMap<>();
                    error.put("message", "Email already existed");
                    return new ResponseEntity<> (error, HttpStatus.CONFLICT);
                }
            }
        }
        return new ResponseEntity<> (res, HttpStatus.CREATED);
    }

    @Operation(summary = "Update a user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Update user detail successfully",
                    content = { @Content(mediaType = "application/json") }
            ),
            @ApiResponse(responseCode = "404", description = "User not found",
                    content = { @Content(mediaType = "application/json") }
            ),
            @ApiResponse(responseCode = "409", description = "Email already exited",
                    content = { @Content(mediaType = "application/json") }
            ),
            @ApiResponse(responseCode = "500", description = "Server error", content = {
                    @Content(mediaType = "application/json")
            })
    })
    @PutMapping("/{id}")
    public ResponseEntity updateUser(@PathVariable String id, @RequestBody UpdateUserRequestDTO updatedUser) {
        UserResponseDTO res = null;
        try {
            res = userService.updateUser(id, updatedUser);
        } catch (Exception e) {
            switch (e.getMessage()){
                case "Not found": {
                    HashMap<String, String> error = new HashMap<>();
                    error.put("message", "User not found");
                    return new ResponseEntity<> (error, HttpStatus.NOT_FOUND);
                }
                case "Email existed": {
                    HashMap<String, String> error = new HashMap<>();
                    error.put("message", "Email already existed");
                    return new ResponseEntity<> (error, HttpStatus.CONFLICT);
                }
                case "User deleted": {
                    HashMap<String, String> error = new HashMap<>();
                    error.put("message", "User is already deleted");
                    return new ResponseEntity<> (error, HttpStatus.NOT_FOUND);
                }
            }
        }
        return new ResponseEntity<> (res, HttpStatus.OK);
    }

    @Operation(summary = "Soft delete user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Delete user successfully",
                    content = { @Content(mediaType = "application/json") }
            ),
            @ApiResponse(responseCode = "500", description = "Server error", content = {
                    @Content(mediaType = "application/json")
            })
    })
    @DeleteMapping("/{id}")
    public ResponseEntity deleteUser(@PathVariable String id) {
        HashMap<String, String> res = new HashMap<>();
        try {
            res = userService.deleteUser(id);
        } catch (Exception e) {
            switch (e.getMessage()){
                case "Not found": {
                    HashMap<String, String> error = new HashMap<>();
                    error.put("message", "User not found");
                    return new ResponseEntity<> (error, HttpStatus.NOT_FOUND);
                }
            }
        }
        return new ResponseEntity<> (res, HttpStatus.OK);
    }

}
