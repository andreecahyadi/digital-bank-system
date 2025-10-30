package com.digitalbank.user.controller;

import com.digitalbank.user.model.UserRegistrationDto;
import com.digitalbank.user.model.UserResponse;
import com.digitalbank.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {
    
    private final UserService userService;
    
    @PostMapping("/register")
    public ResponseEntity<UserResponse> register(@Valid @RequestBody UserRegistrationDto dto) {
        UserResponse response = userService.registerUser(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> getUser(@PathVariable Long id) {
        UserResponse user = userService.getUserById(id);
        return ResponseEntity.ok(user);
    }
    
    @GetMapping("/email/{email}")
    public ResponseEntity<UserResponse> getUserByEmail(@PathVariable String email) {
        UserResponse user = userService.getUserByEmail(email);
        return ResponseEntity.ok(user);
    }
    
    @GetMapping("/active")
    public ResponseEntity<List<UserResponse>> getActiveUsers() {
        List<UserResponse> users = userService.getAllActiveUsers();
        return ResponseEntity.ok(users);
    }
    
    @GetMapping("/search")
    public ResponseEntity<List<UserResponse>> searchUsers(@RequestParam String keyword) {
        List<UserResponse> users = userService.searchUsers(keyword);
        return ResponseEntity.ok(users);
    }
    
    @GetMapping("/recent")
    public ResponseEntity<List<UserResponse>> getRecentUsers(@RequestParam(defaultValue = "30") int days) {
        List<UserResponse> users = userService.getRecentActiveUsers(days);
        return ResponseEntity.ok(users);
    }
    
    @PostMapping("/validate-pin")
    public ResponseEntity<Map<String, Boolean>> validatePin(@RequestBody Map<String, Object> request) {
        Long userId = Long.valueOf(request.get("userId").toString());
        String pin = request.get("pin").toString();
        boolean isValid = userService.validatePin(userId, pin);
        return ResponseEntity.ok(Map.of("valid", isValid));
    }
}