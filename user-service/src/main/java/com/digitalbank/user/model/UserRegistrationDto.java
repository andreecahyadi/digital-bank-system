package com.digitalbank.user.model;

import lombok.Data;
import jakarta.validation.constraints.*;

@Data
public class UserRegistrationDto {
    
    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;
    
    @NotBlank(message = "Full name is required")
    @Size(min = 3, max = 100)
    private String fullName;
    
    @NotBlank(message = "Phone number is required")
    @Pattern(regexp = "^\\+?[1-9]\\d{9,14}$", message = "Invalid phone number")
    private String phoneNumber;
    
    @NotBlank(message = "PIN is required")
    @Pattern(regexp = "^\\d{6}$", message = "PIN must be 6 digits")
    private String pin;
}