package com.digitalbank.user.service;

import com.digitalbank.user.model.UserRegistrationDto;
import com.digitalbank.user.model.UserResponse;
import com.digitalbank.user.entity.User;
import com.digitalbank.user.model.common.UserStatus;
import com.digitalbank.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {
    
    private final UserRepository userRepository;
    
    @Transactional
    public UserResponse registerUser(UserRegistrationDto dto) {
        if (userRepository.findByEmail(dto.getEmail()).isPresent()) {
            throw new RuntimeException("Email already exists");
        }
        if (userRepository.findByPhoneNumber(dto.getPhoneNumber()).isPresent()) {
            throw new RuntimeException("Phone number already exists");
        }
        
        User user = new User();
        user.setEmail(dto.getEmail());
        user.setFullName(dto.getFullName());
        user.setPhoneNumber(dto.getPhoneNumber());
        user.setPin(dto.getPin());
        
        User savedUser = userRepository.save(user);
        return mapToResponseDto(savedUser);
    }
    
    public UserResponse getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return mapToResponseDto(user);
    }
    
    public UserResponse getUserByEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return mapToResponseDto(user);
    }
    
    // Java Stream: Filter and transform
    public List<UserResponse> getAllActiveUsers() {
        return userRepository.findByStatus(UserStatus.ACTIVE)
                .stream()
                .map(this::mapToResponseDto)
                .collect(Collectors.toList());
    }
    
    // Java Stream: Search with filtering and sorting
    public List<UserResponse> searchUsers(String keyword) {
        return userRepository.searchUsers(keyword)
                .stream()
                .filter(user -> user.getStatus() == UserStatus.ACTIVE)
                .map(this::mapToResponseDto)
                .sorted((u1, u2) -> u1.getFullName().compareTo(u2.getFullName()))
                .collect(Collectors.toList());
    }
    
    // Native SQL usage
    public List<UserResponse> getRecentActiveUsers(int days) {
        return userRepository.findRecentUsersByStatus("ACTIVE", days)
                .stream()
                .map(this::mapToResponseDto)
                .collect(Collectors.toList());
    }
    
    public boolean validatePin(Long userId, String pin) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return user.getPin().equals(pin);
    }
    
    private UserResponse mapToResponseDto(User user) {
        UserResponse dto = new UserResponse();
        dto.setId(user.getId());
        dto.setEmail(user.getEmail());
        dto.setFullName(user.getFullName());
        dto.setPhoneNumber(user.getPhoneNumber());
        dto.setStatus(user.getStatus());
        dto.setCreatedAt(user.getCreatedAt());
        return dto;
    }
}