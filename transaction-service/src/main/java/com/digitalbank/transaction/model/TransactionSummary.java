package com.digitalbank.transaction.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TransactionSummary {
    private BigDecimal totalSent;
    private BigDecimal totalReceived;
    private Long transactionCount;
    private BigDecimal netAmount;
}
