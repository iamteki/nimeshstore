package com.nimesh.repository;

import com.nimesh.model.InvoiceItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InvoiceItemRepository extends JpaRepository<InvoiceItem, Long> {
    List<InvoiceItem> findByInvoiceId(Long invoiceId);
    
    List<InvoiceItem> findByProductId(Long productId);
    
    @Query("SELECT SUM(ii.quantity) FROM InvoiceItem ii WHERE ii.product.id = ?1")
    Double getSoldQuantityForProduct(Long productId);
}