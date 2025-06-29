package com.nimesh.repository;

import com.nimesh.model.InvoiceItemBatch;
import java.time.LocalDateTime;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

@Repository
public interface InvoiceItemBatchRepository extends JpaRepository<InvoiceItemBatch, Long> {
    List<InvoiceItemBatch> findByInvoiceItemId(Long invoiceItemId);
    List<InvoiceItemBatch> findByProductBatchId(Long productBatchId);
    
    
    
    /**
     * Find invoice item batches by product batch and date range
     */
    @Query("SELECT iib FROM InvoiceItemBatch iib " +
           "JOIN iib.invoiceItem ii " +
           "JOIN ii.invoice i " +
           "WHERE iib.productBatch.id = :batchId " +
           "AND i.date BETWEEN :startDate AND :endDate")
    List<InvoiceItemBatch> findByProductBatchIdAndDateRange(
            @Param("batchId") Long batchId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );
    
    
    
}