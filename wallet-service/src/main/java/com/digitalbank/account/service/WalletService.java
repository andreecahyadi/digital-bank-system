package com.digitalbank.account.service;

import com.digitalbank.account.model.WalletTopup;
import com.digitalbank.account.model.WalletCreate;
import com.digitalbank.account.model.WalletResponse;
import com.digitalbank.account.entity.Wallet;
import com.digitalbank.account.model.common.WalletStatus;
import com.digitalbank.account.repository.WalletRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class WalletService {
    
    private final WalletRepository walletRepository;
    
    @Transactional
    public WalletResponse createWallet(WalletCreate dto) {
        if (walletRepository.findByUserId(dto.getUserId()).isPresent()) {
            throw new RuntimeException("Wallet already exists for this user");
        }
        
        Wallet wallet = new Wallet();
        wallet.setUserId(dto.getUserId());
        wallet.setCurrency(dto.getCurrency() != null ? dto.getCurrency() : "IDR");
        
        Wallet savedWallet = walletRepository.save(wallet);
        return mapToResponseDto(savedWallet);
    }
    
    @Transactional
    public WalletResponse topUp(WalletTopup dto) {
        Wallet wallet = walletRepository.findByUserId(dto.getUserId())
                .orElseThrow(() -> new RuntimeException("Wallet not found"));
        
        if (wallet.getStatus() != WalletStatus.ACTIVE) {
            throw new RuntimeException("Wallet is not active");
        }
        
        wallet.setBalance(wallet.getBalance().add(dto.getAmount()));
        Wallet updatedWallet = walletRepository.save(wallet);
        
        return mapToResponseDto(updatedWallet);
    }
    
    @Transactional
    public void deductBalance(Long userId, BigDecimal amount) {
        Wallet wallet = walletRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Wallet not found"));
        
        if (wallet.getStatus() != WalletStatus.ACTIVE) {
            throw new RuntimeException("Wallet is not active");
        }
        
        if (wallet.getBalance().compareTo(amount) < 0) {
            throw new RuntimeException("Insufficient balance");
        }
        
        wallet.setBalance(wallet.getBalance().subtract(amount));
        walletRepository.save(wallet);
    }
    
    @Transactional
    public void addBalance(Long userId, BigDecimal amount) {
        Wallet wallet = walletRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Wallet not found"));
        
        wallet.setBalance(wallet.getBalance().add(amount));
        walletRepository.save(wallet);
    }
    
    public WalletResponse getWalletByUserId(Long userId) {
        Wallet wallet = walletRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Wallet not found"));
        return mapToResponseDto(wallet);
    }
    
    public BigDecimal getBalance(Long userId) {
        Wallet wallet = walletRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Wallet not found"));
        return wallet.getBalance();
    }
    
    // Java Stream: Filter and transform wealthy wallets
    public List<WalletResponse> getWealthyWallets(BigDecimal minBalance) {
        return walletRepository.findWalletsAboveBalance(minBalance)
                .stream()
                .map(this::mapToResponseDto)
                .collect(Collectors.toList());
    }
    
    // Native SQL usage: Get statistics
    public Map<String, Object> getWalletStatistics() {
        BigDecimal totalBalance = walletRepository.getTotalActiveBalance();
        List<Object[]> statusCounts = walletRepository.countWalletsByStatus();
        
        Map<String, Long> statusMap = statusCounts.stream()
                .collect(Collectors.toMap(
                    row -> (String) row[0],
                    row -> ((Number) row[1]).longValue()
                ));
        
        return Map.of(
            "totalActiveBalance", totalBalance,
            "walletsByStatus", statusMap
        );
    }
    
    private WalletResponse mapToResponseDto(Wallet wallet) {
        WalletResponse dto = new WalletResponse();
        dto.setId(wallet.getId());
        dto.setUserId(wallet.getUserId());
        dto.setBalance(wallet.getBalance());
        dto.setCurrency(wallet.getCurrency());
        dto.setStatus(wallet.getStatus());
        dto.setCreatedAt(wallet.getCreatedAt());
        dto.setUpdatedAt(wallet.getUpdatedAt());
        return dto;
    }
}