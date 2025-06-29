package com.nimesh.repository;

import com.nimesh.model.Invoice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface InvoiceRepository extends JpaRepository<Invoice, Long> {
    Invoice findByInvoiceNumber(String invoiceNumber);
    
    List<Invoice> findByDate(LocalDateTime date);
    
    List<Invoice> findByDateBetween(LocalDateTime start, LocalDateTime end);
    
    List<Invoice> findByCustomerId(Long customerId);
    
    List<Invoice> findByPaymentMethod(String paymentMethod);
    
    List<Invoice> findByPaymentStatus(String paymentStatus);
    
    List<Invoice> findByCustomerType(String customerType);
    
    List<Invoice> findTop5ByOrderByDateDesc();
    
    List<Invoice> findByCustomerIdAndDateBetween(Long customerId, LocalDateTime start, LocalDateTime end);
    
    @Query("SELECT MAX(i.id) FROM Invoice i")
    Long findMaxId();
    
    @Query("SELECT COUNT(i) FROM Invoice i WHERE i.date >= ?1 AND i.date < ?2")
    Long countInvoicesForDate(LocalDateTime start, LocalDateTime end);
}