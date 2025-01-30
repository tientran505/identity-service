package com.example.identity_service.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

import lombok.Getter;

@Getter
public enum ErrorCode {
    UNCATEGORIZED_EXCEPTION(9999, "Uncategorized exception", HttpStatus.INTERNAL_SERVER_ERROR),
    INVALID_KEY(1001, "Invalid message key", HttpStatus.BAD_REQUEST),
    USER_EXISTED(1002, "User existed", HttpStatus.BAD_REQUEST),
    PERMISSION_EXISTED(1002, "Permission existed", HttpStatus.BAD_REQUEST),
    ROLE_EXISTED(1002, "Role existed", HttpStatus.BAD_REQUEST),
    USERNAME_INVALID(1003, "Username must be at least 8 characters", HttpStatus.BAD_REQUEST),
    PASSWORD_INVALID(1004, "Password must be at least 8 characters", HttpStatus.BAD_REQUEST),
    USER_INVALID(1005, "User not existed", HttpStatus.NOT_FOUND),
    USER_UNAUTHENTICATED(1006, "User is unauthenticated", HttpStatus.UNAUTHORIZED),
    ACCESS_DENIED(1007, "Access denied", HttpStatus.FORBIDDEN),
    INVALID_DOB(1008, "Your age must be at least {min}", HttpStatus.BAD_REQUEST),
    ;

    ErrorCode(int code, String message, HttpStatusCode httpStatusCode) {
        this.code = code;
        this.message = message;
        this.statusCode = httpStatusCode;
    }

    @Getter
    private int code;

    private String message;
    private HttpStatusCode statusCode;
}
