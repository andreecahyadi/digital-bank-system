package com.digitalbank.user.repository;

import com.digitalbank.user.entity.User;
import com.digitalbank.user.model.common.UserStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    
    Optional<User> findByEmail(String email);
    Optional<User> findByPhoneNumber(String phoneNumber);
    List<User> findByStatus(UserStatus status);
    
    @Query(value = "SELECT * FROM users WHERE status = :status AND created_at >= DATEADD(DAY, -:days, GETDATE())", nativeQuery = true)
    List<User> findRecentUsersByStatus(@Param("status") String status, @Param("days") int days);
    
    @Query(value = "SELECT u.* FROM users u WHERE u.full_name LIKE '%' + :keyword + '%' OR u.email LIKE '%' + :keyword + '%'", nativeQuery = true)
    List<User> searchUsers(@Param("keyword") String keyword);
}