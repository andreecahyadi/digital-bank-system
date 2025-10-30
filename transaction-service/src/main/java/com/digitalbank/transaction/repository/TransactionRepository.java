package com.digitalbank.transaction.repository;

import com.digitalbank.transaction.entity.Transaction;
import com.digitalbank.transaction.model.common.TransactionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    
    List<Transaction> findBySenderUserId(Long senderUserId);
    List<Transaction> findByReceiverUserId(Long receiverUserId);
    List<Transaction> findByStatus(TransactionStatus status);
    
    @Query(value = "SELECT * FROM transactions WHERE sender_user_id = :userId OR receiver_user_id = :userId ORDER BY created_at DESC", nativeQuery = true)
    List<Transaction> findAllUserTransactions(@Param("userId") Long userId);
    
    @Query(value = "SELECT " +
                        "SUM(CASE WHEN sender_user_id = :userId THEN amount ELSE 0 END) as total_sent, " +
                        "SUM(CASE WHEN receiver_user_id = :userId THEN amount ELSE 0 END) as total_received, " +
                        "COUNT(*) as transaction_count " +
                   "FROM transactions " +
                   "WHERE (sender_user_id = :userId OR receiver_user_id = :userId) " +
                        "AND status = 'COMPLETED' " +
                        "AND created_at BETWEEN :startDate AND :endDate"
                   , nativeQuery = true)
    Object[] getTransactionSummary(@Param("userId") Long userId, 
                                   @Param("startDate") LocalDateTime startDate,
                                   @Param("endDate") LocalDateTime endDate);
    
    @Query(value = "SELECT TOP(:limit) receiver_user_id, " +
                        "COUNT(*) as transaction_count, " +
                        "SUM(amount) as total_amount " +
                   "FROM transactions " +
                   "WHERE sender_user_id = :userId AND status = 'COMPLETED' " +
                        "GROUP BY receiver_user_id " +
                        "ORDER BY transaction_count DESC"
                    , nativeQuery = true)
    List<Object[]> getTopReceivers(@Param("userId") Long userId, @Param("limit") int limit);
    
    @Query(value = "SELECT CONVERT(DATE, created_at) as transaction_date, " +
                        "COUNT(*) as transaction_count, " +
                        "SUM(amount) as total_volume " +
                   "FROM transactions " +
                   "WHERE status = 'COMPLETED' " +
                        "AND created_at >= DATEADD(DAY, -:days, GETDATE()) " +
                        "GROUP BY CONVERT(DATE, created_at) " +
                        "ORDER BY transaction_date DESC"
                    , nativeQuery = true)
    List<Object[]> getDailyTransactionVolume(@Param("days") int days);
    
    @Query(value = "SELECT TOP(:limit) t.*, " +
                        "(SELECT full_name FROM users WHERE id = t.sender_user_id) as sender_name, " +
                        "(SELECT full_name FROM users WHERE id = t.receiver_user_id) as receiver_name " +
                   "FROM transactions t " +
                   "WHERE t.amount >= :minAmount AND t.status = 'COMPLETED' " +
                        "ORDER BY t.amount DESC"
                    , nativeQuery = true)
    List<Object[]> findLargeTransactions(@Param("minAmount") BigDecimal minAmount, @Param("limit") int limit);
}