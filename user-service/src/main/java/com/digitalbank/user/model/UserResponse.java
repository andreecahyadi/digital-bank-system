package com.digitalbank.user.model;

import com.digitalbank.user.model.common.UserStatus;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class UserResponse {
    private Long id;
    private String email;
    private String fullName;
    private String phoneNumber;
    private UserStatus status;
    private LocalDateTime createdAt;
}