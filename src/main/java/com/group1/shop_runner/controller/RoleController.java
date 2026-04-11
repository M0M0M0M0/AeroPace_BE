package com.group1.shop_runner.controller;

import com.group1.shop_runner.dto.role.request.RoleRequest;
import com.group1.shop_runner.dto.role.response.RoleResponse;
import com.group1.shop_runner.service.RoleService;
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

    // =========================================================
    // API 1: Tạo role
    // POST /api/v1/roles
    // =========================================================
    @PostMapping
    public RoleResponse createRole(@RequestBody @Valid RoleRequest request) {
        return roleService.createRole(request);
    }

    // =========================================================
    // API 2: Lấy tất cả role
    // GET /api/v1/roles
    // =========================================================
    @GetMapping
    public List<RoleResponse> getAllRoles() {
        return roleService.getAllRoles();
    }

    // =========================================================
    // API 3: Lấy role theo id
    // GET /api/v1/roles/{id}
    // =========================================================
    @GetMapping("/{id}")
    public RoleResponse getRoleById(@PathVariable Long id) {
        return roleService.getRoleById(id);
    }

    // =========================================================
    // API 4: Cập nhật role
    // PUT /api/v1/roles/{id}
    // =========================================================
    @PutMapping("/{id}")
    public RoleResponse updateRole(@PathVariable Long id, @RequestBody @Valid RoleRequest request) {
        return roleService.updateRole(id, request);
    }

    // =========================================================
    // API 5: Xóa role
    // DELETE /api/v1/roles/{id}
    // =========================================================
    @DeleteMapping("/{id}")
    public String deleteRole(@PathVariable Long id) {
        roleService.deleteRole(id);
        return "Delete role successfully";
    }
}