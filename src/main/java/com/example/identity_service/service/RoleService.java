package com.example.identity_service.service;

import java.util.HashSet;
import java.util.List;

import org.springframework.stereotype.Service;

import com.example.identity_service.dto.request.RoleRequest;
import com.example.identity_service.dto.response.RoleResponse;
import com.example.identity_service.entity.Role;
import com.example.identity_service.exception.AppException;
import com.example.identity_service.exception.ErrorCode;
import com.example.identity_service.mapper.RoleMapper;
import com.example.identity_service.repository.PermissionRepository;
import com.example.identity_service.repository.RoleRepository;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class RoleService {
    RoleRepository roleRepository;
    PermissionRepository permissionRepository;
    RoleMapper roleMapper;

    public RoleResponse create(RoleRequest req) {
        if (roleRepository.existsByName(req.getName())) {
            throw new AppException(ErrorCode.ROLE_EXISTED);
        }

        var permissions = permissionRepository.findAllById(req.getPermissions());

        Role role = roleMapper.toRole(req);
        role.setPermissions(new HashSet<>(permissions));

        return roleMapper.toRoleResponse(roleRepository.save(role));
    }

    public List<RoleResponse> getAll() {
        var roles = roleRepository.findAll();

        return roles.stream().map(roleMapper::toRoleResponse).toList();
    }

    public void delete(String permission) {
        roleRepository.deleteById(permission);
    }
}
