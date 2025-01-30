package com.example.identity_service.dto.request;

import java.time.LocalDate;

import jakarta.validation.constraints.Size;

import com.example.identity_service.validator.DobConstraint;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserCreationRequest {
    String id;

    @Size(min = 8, message = "USERNAME_INVALID")
    String username;

    @Size(min = 8, max = 16, message = "PASSWORD_INVALID")
    String password;

    String firstName;
    String lastName;

    @DobConstraint(min = 18, message = "INVALID_DOB")
    LocalDate birthDate;
}
