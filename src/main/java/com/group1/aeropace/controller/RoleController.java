package com.group1.aeropace.controller;

import com.group1.aeropace.dto.role.request.RoleRequest;
import com.group1.aeropace.dto.role.response.RoleResponse;
import com.group1.aeropace.service.RoleService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/roles")
//@CrossOrigin(origins = "*")
public class RoleController {

    @Autowired
    private RoleService roleService;

    // POST /api/v1/roles
    @PostMapping
    public RoleResponse createRole(@RequestBody @Valid RoleRequest request) {
        return roleService.createRole(request);
    }

    // GET /api/v1/roles
    @GetMapping
    public List<RoleResponse> getAllRoles() {
        return roleService.getAllRoles();
    }

    // GET /api/v1/roles/{id}
    @GetMapping("/{id}")
    public RoleResponse getRoleById(@PathVariable Long id) {
        return roleService.getRoleById(id);
    }

    // PUT /api/v1/roles/{id}
    @PutMapping("/{id}")
    public RoleResponse updateRole(@PathVariable Long id, @RequestBody @Valid RoleRequest request) {
        return roleService.updateRole(id, request);
    }

    // DELETE /api/v1/roles/{id}
    @DeleteMapping("/{id}")
    public String deleteRole(@PathVariable Long id) {
        roleService.deleteRole(id);
        return "Delete role successfully";
    }
}
