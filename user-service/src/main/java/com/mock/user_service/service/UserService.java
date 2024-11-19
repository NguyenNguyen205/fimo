package com.mock.user_service.service;

import com.mock.user_service.dto.CreateUserRequestDTO;
import com.mock.user_service.dto.UpdateUserRequestDTO;
import com.mock.user_service.dto.UserResponseDTO;
import com.mock.user_service.model.IdempotencyKey;
import com.mock.user_service.model.User;
import com.mock.user_service.repository.IdempotencyKeyRepository;
import com.mock.user_service.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import org.w3c.dom.stylesheets.LinkStyle;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ExecutionException;

@Service
public class UserService {

    private UserRepository userRepository;
    private IdempotencyKeyRepository idempotencyKeyRepository;

    @Autowired
    public UserService(UserRepository userRepository, IdempotencyKeyRepository idempotencyKeyRepository) {
        this.userRepository = userRepository;
        this.idempotencyKeyRepository = idempotencyKeyRepository;
    }

    public UserResponseDTO createUserWithIdempotency(String key, CreateUserRequestDTO createUserRequestDTO) throws Exception {
        // Check for idempotency
        IdempotencyKey savedKey = idempotencyKeyRepository.findById(key).orElse(null);
        if (savedKey != null) {
            if (savedKey.getExpires().isBefore(LocalDateTime.now())) {
                idempotencyKeyRepository.delete(savedKey);
            } else {
                System.out.println("Key found");
                User user = userRepository.findById(savedKey.getUserId()).orElse(null);
                UserResponseDTO userResponseDTO = UserResponseDTO.builder()
//                        .id(user.getId())
                        .code(user.getCode())
                        .username(user.getUsername())
                        .fullname(user.getFullname())
                        .email(user.getEmail())
                        .tier(user.getTier())
                        .createdAt(user.getCreatedDateTime())
                        .deleted(user.isDeleted())
                        .build();
                return userResponseDTO;
            }
        }

        // Validate field
        User checkUser = userRepository.findByEmail(createUserRequestDTO.getEmail());
        if (checkUser != null) {
            throw new Exception("Email existed");
        }

        // Create new user
        String code = UUID.randomUUID().toString().substring(0, 8);
        User user = User.builder()
                .code(code)
                .username(createUserRequestDTO.getUsername())
                .fullname(createUserRequestDTO.getFullName())
                .email(createUserRequestDTO.getEmail())
                .password(createUserRequestDTO.getPassword())
                .tier("free")
                .deleted(false)
                .build();
        user = userRepository.save(user);

        // Save idempotency key
        IdempotencyKey idempotencyKey = IdempotencyKey.builder()
                .keyId(key)
                .userId(user.getId())
                .expires(LocalDateTime.now().plusHours(24))
                .build();
        idempotencyKeyRepository.save(idempotencyKey);

        // Return value
        UserResponseDTO userResponseDTO = UserResponseDTO.builder()
//                .id(user.getId())
                .code(user.getCode())
                .username(user.getUsername())
                .fullname(user.getFullname())
                .email(user.getEmail())
                .tier(user.getTier())
                .createdAt(user.getCreatedDateTime())
                .deleted(user.isDeleted())
                .build();
        return userResponseDTO;
    }

    public List<UserResponseDTO> getAllUsers() {
        List<User> users = userRepository.findAll();
        List<UserResponseDTO> userResponseDTOs = new ArrayList<>();
        for (User u : users) {
            userResponseDTOs.add(UserResponseDTO.builder()
//                            .id(u.getId())
                            .code(u.getCode())
                            .username(u.getUsername())
                            .fullname(u.getFullname())
                            .email(u.getEmail())
                            .tier(u.getTier())
                            .createdAt(u.getCreatedDateTime())
                            .updatedAt(u.getUpdatedDateTime())
                            .deleted(u.isDeleted())
                            .build());
        }
        // by default list all user in descending date order
        Collections.sort(userResponseDTOs, new Comparator<UserResponseDTO>() {
            @Override
            public int compare(UserResponseDTO o1, UserResponseDTO o2) {
                return o2.getCreatedAt().compareTo(o1.getCreatedAt());
            }
        });
        return userResponseDTOs;
    }

    public UserResponseDTO getUser(String codeId) throws Exception {
        User user = userRepository.findByCode(codeId);
        if (user == null) {
            throw new Exception("Not found");
        }
        if (user.isDeleted()) {
            throw new Exception("User deleted");
        }
        UserResponseDTO userResponseDTO = UserResponseDTO.builder()
                .code(user.getCode())
                .username(user.getUsername())
                .fullname(user.getFullname())
                .email(user.getEmail())
                .tier(user.getTier())
                .createdAt(user.getCreatedDateTime())
                .updatedAt(user.getUpdatedDateTime())
                .deleted(user.isDeleted())
                .build();
        return userResponseDTO;
    }

    public UserResponseDTO updateUser(String codeId, UpdateUserRequestDTO updateUserRequestDTO) throws Exception {
        User user = userRepository.findByCode(codeId);
        if (user == null) {
            throw new Exception("Not found");
        }
        // Check email existed
        User checkEmail = userRepository.findByEmail(updateUserRequestDTO.getEmail());
        if (checkEmail != null) {
            throw new Exception("Email existed");
        }
        // Check if user is deleted
        if (user.isDeleted()) {
            throw new Exception("User deleted");
        }
        // Update user detail
        if (!updateUserRequestDTO.getUsername().isEmpty()) {
            user.setUsername(updateUserRequestDTO.getUsername());
        }
        if (!updateUserRequestDTO.getFullName().isEmpty()) {
            user.setFullname(updateUserRequestDTO.getFullName());
        }
        if (!updateUserRequestDTO.getEmail().isEmpty()) {
            user.setEmail(updateUserRequestDTO.getEmail());
        }
        // Save to database
        user = userRepository.save(user);
        UserResponseDTO userResponseDTO = UserResponseDTO.builder()
                .code(user.getCode())
                .username(user.getUsername())
                .fullname(user.getFullname())
                .email(user.getEmail())
                .tier(user.getTier())
                .createdAt(user.getCreatedDateTime())
                .updatedAt(user.getUpdatedDateTime())
                .deleted(user.isDeleted())
                .build();
        return userResponseDTO;
    }

    public HashMap<String, String> deleteUser(String code) throws Exception {
        User user = userRepository.findByCode(code);
        if (user == null) {
            throw new Exception("Not found");
        }
        // Soft delete
        user.setDeleted(true);
        user = userRepository.save(user);

        HashMap<String, String> res = new HashMap<>();
        res.put("code", user.getCode());
        res.put("deleted", "true");
        return res;
    }

}
