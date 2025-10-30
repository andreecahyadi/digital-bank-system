package com.digitalbank.account.controller;

import com.digitalbank.account.model.WalletTopup;
import com.digitalbank.account.model.WalletCreate;
import com.digitalbank.account.model.WalletResponse;
import com.digitalbank.account.service.WalletService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/wallets")
@RequiredArgsConstructor
public class WalletController {
    
    private final WalletService walletService;
    
    @PostMapping("/create")
    public ResponseEntity<WalletResponse> createWallet(@RequestBody WalletCreate dto) {
        WalletResponse response = walletService.createWallet(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    @PostMapping("/topup")
    public ResponseEntity<WalletResponse> topUp(@Valid @RequestBody WalletTopup dto) {
        WalletResponse response = walletService.topUp(dto);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/user/{userId}")
    public ResponseEntity<WalletResponse> getWalletByUserId(@PathVariable Long userId) {
        WalletResponse wallet = walletService.getWalletByUserId(userId);
        return ResponseEntity.ok(wallet);
    }
    
    @GetMapping("/balance/{userId}")
    public ResponseEntity<Map<String, BigDecimal>> getBalance(@PathVariable Long userId) {
        BigDecimal balance = walletService.getBalance(userId);
        return ResponseEntity.ok(Map.of("balance", balance));
    }
    
    @GetMapping("/wealthy")
    public ResponseEntity<List<WalletResponse>> getWealthyWallets(
            @RequestParam(defaultValue = "1000") BigDecimal minBalance) {
        List<WalletResponse> wallets = walletService.getWealthyWallets(minBalance);
        return ResponseEntity.ok(wallets);
    }
    
    @GetMapping("/statistics")
    public ResponseEntity<Map<String, Object>> getStatistics() {
        Map<String, Object> stats = walletService.getWalletStatistics();
        return ResponseEntity.ok(stats);
    }
    
    @PostMapping("/deduct")
    public ResponseEntity<Void> deductBalance(@RequestBody Map<String, Object> request) {
        Long userId = Long.valueOf(request.get("userId").toString());
        BigDecimal amount = new BigDecimal(request.get("amount").toString());
        walletService.deductBalance(userId, amount);
        return ResponseEntity.ok().build();
    }
    
    @PostMapping("/add")
    public ResponseEntity<Void> addBalance(@RequestBody Map<String, Object> request) {
        Long userId = Long.valueOf(request.get("userId").toString());
        BigDecimal amount = new BigDecimal(request.get("amount").toString());
        walletService.addBalance(userId, amount);
        return ResponseEntity.ok().build();
    }
}