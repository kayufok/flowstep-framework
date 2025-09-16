package net.xrftech.flowstep.example.repository;

import net.xrftech.flowstep.example.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * JPA Repository for Order entity.
 * Demonstrates complex queries for multi-step operations.
 */
@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    
    List<Order> findByUserIdOrderByOrderDateDesc(Long userId);
    
    @Query("SELECT o FROM Order o WHERE o.userId = :userId AND o.orderDate BETWEEN :startDate AND :endDate ORDER BY o.orderDate DESC")
    List<Order> findUserOrdersInDateRange(
        @Param("userId") Long userId,
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate
    );
    
    @Query("SELECT SUM(o.totalAmount) FROM Order o WHERE o.userId = :userId")
    BigDecimal getTotalSpentByUser(@Param("userId") Long userId);
    
    @Query("SELECT COUNT(o) FROM Order o WHERE o.userId = :userId")
    Integer getTotalOrderCountByUser(@Param("userId") Long userId);
    
    @Query("SELECT AVG(o.totalAmount) FROM Order o WHERE o.userId = :userId")
    BigDecimal getAverageOrderValueByUser(@Param("userId") Long userId);
    
    @Query(value = "SELECT status, COUNT(*) as count FROM orders WHERE user_id = :userId GROUP BY status ORDER BY count DESC LIMIT 1", nativeQuery = true)
    Object[] getMostCommonOrderStatusByUser(@Param("userId") Long userId);
}