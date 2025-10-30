package com.digitalbank.account.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.digitalbank.account.entity.Wallet;
import com.digitalbank.account.model.common.WalletStatus;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface WalletRepository extends JpaRepository<Wallet, Long> {
    
    Optional<Wallet> findByUserId(Long userId);
    List<Wallet> findByStatus(WalletStatus status);
    
    @Query(value = "SELECT * FROM wallets WHERE balance >= :minBalance AND status = 'ACTIVE' ORDER BY balance DESC", nativeQuery = true)
    List<Wallet> findWalletsAboveBalance(@Param("minBalance") BigDecimal minBalance);
    
    @Query(value = "SELECT ISNULL(SUM(balance), 0) FROM wallets WHERE status = 'ACTIVE'", nativeQuery = true)
    BigDecimal getTotalActiveBalance();
    
    @Query(value = "SELECT status, COUNT(*) as count FROM wallets GROUP BY status", nativeQuery = true)
    List<Object[]> countWalletsByStatus();
}