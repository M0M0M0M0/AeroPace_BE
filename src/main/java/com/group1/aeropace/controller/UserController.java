package com.group1.aeropace.controller;

import com.group1.aeropace.dto.user.request.UserRequest;
import com.group1.aeropace.dto.user.response.UserResponse;
import com.group1.aeropace.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/users")
//@CrossOrigin(origins = "*")
public class UserController {

    @Autowired
    private UserService userService;

    // POST /api/v1/users
    @PostMapping
    public UserResponse createUser(@RequestBody @Valid UserRequest request) {
        return userService.createUser(request);
    }

    // GET /api/v1/users
    @GetMapping
    public List<UserResponse> getAllUsers() {
        return userService.getAllUsers();
    }

    // GET /api/v1/users/{id}
    @GetMapping("/{id}")
    public UserResponse getUserById(@PathVariable Long id) {
        return userService.getUserById(id);
    }

    // PUT /api/v1/users/{id}
    @PutMapping("/{id}")
    public UserResponse updateUser(@PathVariable Long id,
                                   @RequestBody @Valid UserRequest request) {
        return userService.updateUser(id, request);
    }

    // DELETE /api/v1/users/{id}
//    @DeleteMapping("/{id}")
//    public String deleteUser(@PathVariable Long id) {
//        userService.deleteUser(id);
//        return "Delete user successfully";
//    }
}
