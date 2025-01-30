package com.example.identity_service.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.example.identity_service.dto.request.PermissionRequest;
import com.example.identity_service.dto.response.PermissionResponse;
import com.example.identity_service.entity.Permission;
import com.example.identity_service.exception.AppException;
import com.example.identity_service.exception.ErrorCode;
import com.example.identity_service.mapper.PermissionMapper;
import com.example.identity_service.repository.PermissionRepository;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class PermissionService {
    PermissionRepository permissionRepository;
    private final PermissionMapper permissionMapper;

    public PermissionResponse create(PermissionRequest req) {
        if (permissionRepository.existsByName(req.getName())) {
            throw new AppException(ErrorCode.PERMISSION_EXISTED);
        }

        Permission permission = permissionMapper.toPermission(req);
        return permissionMapper.toPermissionResponse(permissionRepository.save(permission));
    }

    public List<PermissionResponse> getAll() {
        var permissions = permissionRepository.findAll();
        return permissions.stream().map(permissionMapper::toPermissionResponse).toList();
    }

    public void delete(String permission) {
        permissionRepository.deleteById(permission);
    }
}
