package com.digitalbank.account.model;

import com.digitalbank.account.model.common.WalletStatus;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class WalletResponse {
    private Long id;
    private Long userId;
    private BigDecimal balance;
    private String currency;
    private WalletStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}