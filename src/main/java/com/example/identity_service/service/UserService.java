package com.example.identity_service.service;

import java.util.HashSet;
import java.util.List;

import com.example.identity_service.dto.request.ProfileCreationRequest;
import com.example.identity_service.entity.Role;
import com.example.identity_service.mapper.ProfileMapper;
import com.example.identity_service.repository.http_client.ProfileClient;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.example.identity_service.dto.request.UserCreationRequest;
import com.example.identity_service.dto.request.UserUpdateRequest;
import com.example.identity_service.dto.response.UserResponse;
import com.example.identity_service.entity.User;
import com.example.identity_service.exception.AppException;
import com.example.identity_service.exception.ErrorCode;
import com.example.identity_service.mapper.UserMapper;
import com.example.identity_service.repository.RoleRepository;
import com.example.identity_service.repository.UserRepository;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class UserService {
    UserRepository userRepository;
    UserMapper userMapper;
    PasswordEncoder passwordEncoder;
    RoleRepository roleRepository;
    ProfileClient profileClient;
    ProfileMapper profileMapper;

    public UserResponse createUser(UserCreationRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new AppException(ErrorCode.USER_EXISTED);
        }
        User user = userMapper.toUser(request);
        user.setPassword(passwordEncoder.encode(request.getPassword()));

        HashSet<Role> roles = new HashSet<>();
        roleRepository.findById("USER").ifPresent(roles::add);

        user.setRoles(roles);

        user = userRepository.save(user);

        ProfileCreationRequest profileRequest = profileMapper.toProfileCreationRequest(request);
        profileRequest.setUserId(user.getId());
        profileClient.createProfile(profileRequest);

        return userMapper.toUserResponse(user);
    }

    @PreAuthorize("hasRole('ADMIN')")
    public List<UserResponse> getAllUsers() {
        var authentication = SecurityContextHolder.getContext().getAuthentication();

        log.info("Username: {}", authentication.getName());
        log.info("Roles: {}", authentication.getAuthorities());

        return userRepository.findAll().stream().map(userMapper::toUserResponse).toList();
    }

    @PostAuthorize("returnObject.username == authentication.name")
    public UserResponse getUserById(String id) {
        log.info("In method get user by id");
        return userMapper.toUserResponse(
                userRepository.findById(id).orElseThrow(() -> new AppException(ErrorCode.USER_INVALID)));
    }

    public UserResponse updateUser(String userId, UserUpdateRequest request) {
        User user = userRepository.findById(userId).orElseThrow(() -> new AppException(ErrorCode.USER_INVALID));

        log.info("user before mapping {}", user);

        userMapper.updateUser(user, request);

        log.info("user after mapping {}", user);

        user.setPassword(passwordEncoder.encode(request.getPassword()));

        var roles = roleRepository.findAllById(request.getRoles());

        user.setRoles(new HashSet<>(roles));

        return userMapper.toUserResponse(userRepository.save(user));
    }

    public UserResponse getMyInfo() {
        var ctx = SecurityContextHolder.getContext();
        String username = ctx.getAuthentication().getName();
        log.info("Username: {}", username);

        User user = userRepository.findByUsername(username).orElseThrow(() -> new AppException(ErrorCode.USER_INVALID));

        return userMapper.toUserResponse(user);
    }
}
