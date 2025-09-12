package net.xrftech.flowstep.example.repository;

import net.xrftech.flowstep.example.model.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * JPA Repository for OrderItem entity.
 */
@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {
    
    List<OrderItem> findByOrderId(Long orderId);
    
    void deleteByOrderId(Long orderId);
}