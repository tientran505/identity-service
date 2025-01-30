package com.example.identity_service.service;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import java.time.LocalDate;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.TestPropertySource;

import com.example.identity_service.dto.request.UserCreationRequest;
import com.example.identity_service.dto.response.UserResponse;
import com.example.identity_service.entity.User;
import com.example.identity_service.exception.AppException;
import com.example.identity_service.exception.ErrorCode;
import com.example.identity_service.repository.UserRepository;

@SpringBootTest
@TestPropertySource("/test.properties")
public class UserServiceTest {
    @Autowired
    private UserService userService;

    @MockBean
    private UserRepository userRepository;

    private UserCreationRequest mockRequest;
    private UserResponse mockResponse;
    private LocalDate dob;
    private User mockUser;

    @BeforeEach
    void initData() {
        dob = LocalDate.of(1990, 1, 15);

        mockRequest = UserCreationRequest.builder()
                .username("john_doe")
                .firstName("John")
                .lastName("Doe")
                .password("password")
                .birthDate(dob)
                .build();

        mockResponse = UserResponse.builder()
                .username("john_doe")
                .firstName("John")
                .lastName("Doe")
                .id("mock-user-id")
                .birthDate(dob)
                .build();

        mockUser = User.builder()
                .username("john_doe")
                .firstName("John")
                .lastName("Doe")
                .id("mock-user-id")
                .birthDate(dob)
                .build();
    }

    @Test
    void createUser_validRequest_success() {
        // GIVEN
        when(userRepository.existsByUsername(ArgumentMatchers.anyString())).thenReturn(false);
        when(userRepository.save(any())).thenReturn(mockUser);

        // WHEN
        var response = userService.createUser(mockRequest);

        // THEN
        Assertions.assertThat(response.getId()).isEqualTo("mock-user-id");
        Assertions.assertThat(response.getUsername()).isEqualTo("john_doe");
    }

    @Test
    void createUser_existByUsernameRequest_fail() {
        // GIVEN
        when(userRepository.existsByUsername(anyString())).thenReturn(true);

        // WHEN
        var exception = assertThrows(AppException.class, () -> userService.createUser(mockRequest));

        // THEN
        Assertions.assertThat(exception.getErrorCode().getCode()).isEqualTo(ErrorCode.USER_EXISTED.getCode());
    }
}
