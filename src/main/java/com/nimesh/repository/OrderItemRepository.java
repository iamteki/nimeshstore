package com.nimesh.repository;

import com.nimesh.model.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {
    List<OrderItem> findByPurchaseOrderId(Long purchaseOrderId);
    
    List<OrderItem> findByProductId(Long productId);
    
    @Query("SELECT COUNT(oi) FROM OrderItem oi WHERE oi.purchaseOrder.id = ?1")
    int countByPurchaseOrderId(Long purchaseOrderId);
}