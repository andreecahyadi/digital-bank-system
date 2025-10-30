package com.digitalbank.transaction.model;

import lombok.Data;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;

@Data
public class TransferRequest {
    
    @NotNull(message = "Sender user ID is required")
    private Long senderUserId;
    
    @NotNull(message = "Receiver user ID is required")
    private Long receiverUserId;
    
    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be greater than 0")
    private BigDecimal amount;
    
    @NotBlank(message = "PIN is required")
    private String pin;
    
    private String description;
}
