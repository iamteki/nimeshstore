package com.nimesh.repository;

import com.nimesh.model.ProductBatch;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductBatchRepository extends JpaRepository<ProductBatch, Long> {
    List<ProductBatch> findByProductIdOrderByPurchaseDateAsc(Long productId);
    List<ProductBatch> findByProductIdOrderByPurchaseDateDesc(Long productId);
    
    /**
 * Get batch valuation data (buying price and remaining quantity) for a product
     * @param productId
     * @return 
 */
    @Query("SELECT b.buyingPrice, b.remainingQuantity FROM ProductBatch b WHERE b.product.id = :productId AND b.remainingQuantity > 0")
    List<Object[]> getBatchValuationByProductId(@Param("productId") Long productId);
    
    @Query("SELECT pb FROM ProductBatch pb WHERE pb.product.id = :productId AND pb.remainingQuantity > 0 ORDER BY pb.purchaseDate ASC")
    List<ProductBatch> findAvailableBatchesByProductIdOrderByPurchaseDateAsc(@Param("productId") Long productId);
    
    @Query("SELECT SUM(pb.remainingQuantity) FROM ProductBatch pb WHERE pb.product.id = :productId")
    java.math.BigDecimal getTotalRemainingQuantityByProductId(@Param("productId") Long productId);
    
    
    /**
     * Find batches by product ID
     */
    @Query("SELECT pb FROM ProductBatch pb WHERE pb.product.id = :productId ORDER BY pb.purchaseDate DESC")
    List<ProductBatch> findByProductId(@Param("productId") Long productId);
    
    
}