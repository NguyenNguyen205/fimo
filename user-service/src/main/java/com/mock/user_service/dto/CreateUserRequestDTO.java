package com.mock.user_service.dto;

import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CreateUserRequestDTO {
    @NotEmpty(message = "Required")
    private String username;
    @NotEmpty(message = "Required")
    private String fullName;
    @NotEmpty(message = "Required")
    private String password;
    @NotEmpty(message = "Required")
    private String email;
}
