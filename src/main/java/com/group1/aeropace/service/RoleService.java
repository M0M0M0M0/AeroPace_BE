package com.group1.aeropace.service;

import com.group1.aeropace.dto.role.request.RoleRequest;
import com.group1.aeropace.dto.role.response.RoleResponse;
import com.group1.aeropace.entity.Role;
import com.group1.aeropace.shared.exception.AppException;
import com.group1.aeropace.shared.exception.ErrorCode;
import com.group1.aeropace.repository.RoleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class RoleService {

    @Autowired
    private RoleRepository roleRepository;

    public RoleResponse createRole(RoleRequest request) {
        roleRepository.findByName(request.getName())
                .ifPresent(role -> {
                    throw new AppException(ErrorCode.ROLE_ALREADY_EXISTS);
                });

        Role role = new Role();
        role.setName(request.getName());
        role.setDescription(request.getDescription());
        role.setCreatedAt(LocalDateTime.now());
        role.setUpdatedAt(LocalDateTime.now());

        Role savedRole = roleRepository.save(role);
        return mapToRoleResponse(savedRole);
    }

    public List<RoleResponse> getAllRoles() {
        return roleRepository.findAll().stream()
                .map(this::mapToRoleResponse)
                .toList();
    }

    public RoleResponse getRoleById(Long id) {
        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.ROLE_NOT_FOUND));

        return mapToRoleResponse(role);
    }

    public RoleResponse updateRole(Long id, RoleRequest request) {
        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.ROLE_NOT_FOUND));

        roleRepository.findByName(request.getName())
                .ifPresent(existingRole -> {
                    if (!existingRole.getId().equals(id)) {
                        throw new AppException(ErrorCode.ROLE_ALREADY_EXISTS);
                    }
                });

        role.setName(request.getName());
        role.setDescription(request.getDescription());
        role.setUpdatedAt(LocalDateTime.now());

        Role updatedRole = roleRepository.save(role);
        return mapToRoleResponse(updatedRole);
    }

    public void deleteRole(Long id) {
        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.ROLE_NOT_FOUND));

        roleRepository.delete(role);
    }

    private RoleResponse mapToRoleResponse(Role role) {
        return new RoleResponse(
                role.getId(),
                role.getName(),
                role.getDescription()
        );
    }
}
