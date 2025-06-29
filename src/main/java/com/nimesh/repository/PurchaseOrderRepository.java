package com.nimesh.repository;

import com.nimesh.model.PurchaseOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface PurchaseOrderRepository extends JpaRepository<PurchaseOrder, Long> {
    PurchaseOrder findByOrderNumber(String orderNumber);
    
    List<PurchaseOrder> findBySupplierId(Long supplierId);
    
    List<PurchaseOrder> findByStatus(String status);
    
    List<PurchaseOrder> findByOrderDateBetween(LocalDateTime start, LocalDateTime end);
    
    @Query("SELECT MAX(p.id) FROM PurchaseOrder p")
    Long findMaxId();
}