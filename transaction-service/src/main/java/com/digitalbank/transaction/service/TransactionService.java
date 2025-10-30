package com.digitalbank.transaction.service;

import com.digitalbank.transaction.model.TransactionResponse;
import com.digitalbank.transaction.model.TransactionSummary;
import com.digitalbank.transaction.model.TransferRequest;
import com.digitalbank.transaction.entity.Transaction;
import com.digitalbank.transaction.model.common.TransactionStatus;
import com.digitalbank.transaction.model.common.TransactionType;
import com.digitalbank.transaction.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TransactionService {
    
    private final TransactionRepository transactionRepository;
    private final RestTemplate restTemplate;
    
    @Value("${wallet.service.url}")
    private String walletServiceUrl;
    
    @Value("${user.service.url}")
    private String userServiceUrl;
    
    @Transactional
    public TransactionResponse transfer(TransferRequest request) {
        // Validate users exist
        validateUser(request.getSenderUserId());
        validateUser(request.getReceiverUserId());
        
        // Validate PIN
        if (!validatePin(request.getSenderUserId(), request.getPin())) {
            throw new RuntimeException("Invalid PIN");
        }
        
        // Check sender balance
        BigDecimal senderBalance = getWalletBalance(request.getSenderUserId());
        if (senderBalance.compareTo(request.getAmount()) < 0) {
            throw new RuntimeException("Insufficient balance");
        }
        
        // Create transaction
        Transaction transaction = new Transaction();
        transaction.setTransactionRef(generateTransactionRef());
        transaction.setSenderUserId(request.getSenderUserId());
        transaction.setReceiverUserId(request.getReceiverUserId());
        transaction.setAmount(request.getAmount());
        transaction.setCurrency("USD");
        transaction.setType(TransactionType.TRANSFER);
        transaction.setStatus(TransactionStatus.PENDING);
        transaction.setDescription(request.getDescription());
        
        Transaction savedTransaction = transactionRepository.save(transaction);
        
        try {
            // Deduct from sender
            deductBalance(request.getSenderUserId(), request.getAmount());
            
            // Add to receiver
            addBalance(request.getReceiverUserId(), request.getAmount());
            
            // Update transaction status
            savedTransaction.setStatus(TransactionStatus.COMPLETED);
            savedTransaction.setCompletedAt(LocalDateTime.now());
            transactionRepository.save(savedTransaction);
            
        } catch (Exception e) {
            savedTransaction.setStatus(TransactionStatus.FAILED);
            transactionRepository.save(savedTransaction);
            throw new RuntimeException("Transfer failed: " + e.getMessage());
        }
        
        return mapToResponseDto(savedTransaction);
    }
    
    // Java Stream: Get filtered transaction history
    public List<TransactionResponse> getUserTransactionHistory(Long userId, 
                                                                   TransactionStatus status) {
        List<Transaction> transactions = transactionRepository.findAllUserTransactions(userId);
        
        return transactions.stream()
                .filter(t -> status == null || t.getStatus() == status)
                .sorted((t1, t2) -> t2.getCreatedAt().compareTo(t1.getCreatedAt()))
                .map(this::mapToResponseDto)
                .collect(Collectors.toList());
    }
    
    // Java Stream: Calculate transaction statistics
    public TransactionSummary getTransactionSummary(Long userId, int days) {
        LocalDateTime startDate = LocalDateTime.now().minusDays(days);
        LocalDateTime endDate = LocalDateTime.now();
        
        Object[] result = transactionRepository.getTransactionSummary(userId, startDate, endDate);
        
        BigDecimal totalSent = result[0] != null ? (BigDecimal) result[0] : BigDecimal.ZERO;
        BigDecimal totalReceived = result[1] != null ? (BigDecimal) result[1] : BigDecimal.ZERO;
        Long count = result[2] != null ? ((Number) result[2]).longValue() : 0L;
        
        TransactionSummary summary = new TransactionSummary();
        summary.setTotalSent(totalSent);
        summary.setTotalReceived(totalReceived);
        summary.setTransactionCount(count);
        summary.setNetAmount(totalReceived.subtract(totalSent));
        
        return summary;
    }
    
    // Java Stream: Get top receivers with Stream operations
    public List<Map<String, Object>> getTopReceivers(Long userId, int limit) {
        List<Object[]> results = transactionRepository.getTopReceivers(userId, limit);
        
        return results.stream()
                .map(row -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("receiverUserId", ((Number) row[0]).longValue());
                    map.put("transactionCount", ((Number) row[1]).longValue());
                    map.put("totalAmount", (BigDecimal) row[2]);
                    return map;
                })
                .collect(Collectors.toList());
    }
    
    // Native SQL usage: Daily transaction volume
    public List<Map<String, Object>> getDailyVolume(int days) {
        List<Object[]> results = transactionRepository.getDailyTransactionVolume(days);
        
        return results.stream()
                .map(row -> {
                    Map<String, Object> map = new LinkedHashMap<>();
                    map.put("date", row[0].toString());
                    map.put("transactionCount", ((Number) row[1]).longValue());
                    map.put("totalVolume", (BigDecimal) row[2]);
                    return map;
                })
                .collect(Collectors.toList());
    }
    
    // Java Stream: Filter and analyze large transactions
    public List<Map<String, Object>> getLargeTransactions(BigDecimal minAmount, int limit) {
        List<Transaction> allTransactions = transactionRepository.findByStatus(TransactionStatus.COMPLETED);
        
        return allTransactions.stream()
                .filter(t -> t.getAmount().compareTo(minAmount) >= 0)
                .sorted((t1, t2) -> t2.getAmount().compareTo(t1.getAmount()))
                .limit(limit)
                .map(t -> {
                    Map<String, Object> map = new LinkedHashMap<>();
                    map.put("transactionRef", t.getTransactionRef());
                    map.put("senderUserId", t.getSenderUserId());
                    map.put("receiverUserId", t.getReceiverUserId());
                    map.put("amount", t.getAmount());
                    map.put("createdAt", t.getCreatedAt());
                    return map;
                })
                .collect(Collectors.toList());
    }
    
    private void validateUser(Long userId) {
        try {
            restTemplate.getForObject(userServiceUrl + "/api/users/" + userId, Object.class);
        } catch (Exception e) {
            throw new RuntimeException("User not found: " + userId);
        }
    }
    
    private boolean validatePin(Long userId, String pin) {
        try {
            Map<String, Object> request = Map.of("userId", userId, "pin", pin);
            Map response = restTemplate.postForObject(
                userServiceUrl + "/api/users/validate-pin", 
                request, 
                Map.class
            );
            return (Boolean) response.get("valid");
        } catch (Exception e) {
            return false;
        }
    }
    
    private BigDecimal getWalletBalance(Long userId) {
        try {
            Map response = restTemplate.getForObject(
                walletServiceUrl + "/api/wallets/balance/" + userId, 
                Map.class
            );
            return new BigDecimal(response.get("balance").toString());
        } catch (Exception e) {
            throw new RuntimeException("Could not get wallet balance");
        }
    }
    
    private void deductBalance(Long userId, BigDecimal amount) {
        try {
            Map<String, Object> request = Map.of("userId", userId, "amount", amount);
            restTemplate.postForObject(
                walletServiceUrl + "/api/wallets/deduct", 
                request, 
                Void.class
            );
        } catch (Exception e) {
            throw new RuntimeException("Could not deduct balance");
        }
    }
    
    private void addBalance(Long userId, BigDecimal amount) {
        try {
            Map<String, Object> request = Map.of("userId", userId, "amount", amount);
            restTemplate.postForObject(
                walletServiceUrl + "/api/wallets/add", 
                request, 
                Void.class
            );
        } catch (Exception e) {
            throw new RuntimeException("Could not add balance");
        }
    }
    
    private String generateTransactionRef() {
        return "TXN" + System.currentTimeMillis() + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
    
    private TransactionResponse mapToResponseDto(Transaction transaction) {
        TransactionResponse dto = new TransactionResponse();
        dto.setId(transaction.getId());
        dto.setTransactionRef(transaction.getTransactionRef());
        dto.setSenderUserId(transaction.getSenderUserId());
        dto.setReceiverUserId(transaction.getReceiverUserId());
        dto.setAmount(transaction.getAmount());
        dto.setCurrency(transaction.getCurrency());
        dto.setType(transaction.getType());
        dto.setStatus(transaction.getStatus());
        dto.setDescription(transaction.getDescription());
        dto.setCreatedAt(transaction.getCreatedAt());
        dto.setCompletedAt(transaction.getCompletedAt());
        return dto;
    }
}
