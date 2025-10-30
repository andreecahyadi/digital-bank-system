package com.digitalbank.transaction.model;

import com.digitalbank.transaction.model.common.TransactionStatus;
import com.digitalbank.transaction.model.common.TransactionType;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class TransactionResponse {
    private Long id;
    private String transactionRef;
    private Long senderUserId;
    private Long receiverUserId;
    private BigDecimal amount;
    private String currency;
    private TransactionType type;
    private TransactionStatus status;
    private String description;
    private LocalDateTime createdAt;
    private LocalDateTime completedAt;
}