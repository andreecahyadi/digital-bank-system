package com.digitalbank.transaction.controller;

import com.digitalbank.transaction.model.TransactionResponse;
import com.digitalbank.transaction.model.TransactionSummary;
import com.digitalbank.transaction.model.TransferRequest;
import com.digitalbank.transaction.model.common.TransactionStatus;
import com.digitalbank.transaction.service.TransactionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
public class TransactionController {
    
    private final TransactionService transactionService;
    
    @PostMapping("/transfer")
    public ResponseEntity<TransactionResponse> transfer(@Valid @RequestBody TransferRequest request) {
        TransactionResponse response = transactionService.transfer(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    @GetMapping("/history/{userId}")
    public ResponseEntity<List<TransactionResponse>> getHistory(
            @PathVariable Long userId,
            @RequestParam(required = false) TransactionStatus status) {
        List<TransactionResponse> transactions = transactionService.getUserTransactionHistory(userId, status);
        return ResponseEntity.ok(transactions);
    }
    
    @GetMapping("/summary/{userId}")
    public ResponseEntity<TransactionSummary> getSummary(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "30") int days) {
        TransactionSummary summary = transactionService.getTransactionSummary(userId, days);
        return ResponseEntity.ok(summary);
    }
    
    @GetMapping("/top-receivers/{userId}")
    public ResponseEntity<List<Map<String, Object>>> getTopReceivers(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "5") int limit) {
        List<Map<String, Object>> receivers = transactionService.getTopReceivers(userId, limit);
        return ResponseEntity.ok(receivers);
    }
    
    @GetMapping("/daily-volume")
    public ResponseEntity<List<Map<String, Object>>> getDailyVolume(@RequestParam(defaultValue = "7") int days) {
        List<Map<String, Object>> volume = transactionService.getDailyVolume(days);
        return ResponseEntity.ok(volume);
    }
    
    @GetMapping("/large-transactions")
    public ResponseEntity<List<Map<String, Object>>> getLargeTransactions(
            @RequestParam(defaultValue = "1000") BigDecimal minAmount,
            @RequestParam(defaultValue = "10") int limit) {
        List<Map<String, Object>> transactions = transactionService.getLargeTransactions(minAmount, limit);
        return ResponseEntity.ok(transactions);
    }
}
