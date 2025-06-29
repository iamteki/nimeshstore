package com.nimesh.service;

import com.nimesh.model.InvoiceItemBatch;
import com.nimesh.model.Product;
import com.nimesh.model.ProductBatch;
import com.nimesh.repository.InvoiceItemBatchRepository;
import com.nimesh.repository.ProductBatchRepository;
import com.nimesh.repository.ProductRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class BatchReportingService {
    
    @Autowired
    private ProductRepository productRepository;
    
    @Autowired
    private ProductBatchRepository productBatchRepository;
    
    @Autowired
    private InvoiceItemBatchRepository invoiceItemBatchRepository;
    
    /**
     * Get current inventory value based on batch costs
     */
    public BigDecimal getCurrentInventoryValue() {
        BigDecimal totalValue = BigDecimal.ZERO;
        
        List<Product> products = productRepository.findAll();
        for (Product product : products) {
            List<ProductBatch> batches = productBatchRepository.findAvailableBatchesByProductIdOrderByPurchaseDateAsc(product.getId());
            
            for (ProductBatch batch : batches) {
                BigDecimal batchValue = batch.getBuyingPrice().multiply(batch.getRemainingQuantity());
                totalValue = totalValue.add(batchValue);
            }
        }
        
        return totalValue;
    }
    
    /**
     * Get profit margin by product for a date range
     */
    public Map<Product, BigDecimal> getProfitMarginByProduct(LocalDateTime startDate, LocalDateTime endDate) {
        Map<Product, BigDecimal> result = new HashMap<>();
        List<Product> products = productRepository.findAll();
        
        for (Product product : products) {
            // Get all invoice item batches for this product in the date range
            List<InvoiceItemBatch> itemBatches = invoiceItemBatchRepository.findAll().stream()
                .filter(ib -> ib.getProductBatch().getProduct().getId().equals(product.getId()))
                .filter(ib -> {
                    LocalDateTime invoiceDate = ib.getInvoiceItem().getInvoice().getDate();
                    return !invoiceDate.isBefore(startDate) && !invoiceDate.isAfter(endDate);
                })
                .collect(Collectors.toList());
            
            // Calculate total profit for this product
            BigDecimal totalProfit = BigDecimal.ZERO;
            BigDecimal totalSales = BigDecimal.ZERO;
            
            for (InvoiceItemBatch itemBatch : itemBatches) {
                BigDecimal profit = itemBatch.calculateProfit();
                BigDecimal revenue = itemBatch.getUnitPrice().multiply(itemBatch.getQuantity());
                
                totalProfit = totalProfit.add(profit);
                totalSales = totalSales.add(revenue);
            }
            
            // Calculate profit margin (profit / sales)
            if (totalSales.compareTo(BigDecimal.ZERO) > 0) {
                BigDecimal profitMargin = totalProfit.divide(totalSales, 4, BigDecimal.ROUND_HALF_UP);
                result.put(product, profitMargin);
            }
        }
        
        return result;
    }
    
    /**
     * Get batch detail for a specific product
     */
    public List<Map<String, Object>> getBatchDetailForProduct(Long productId) {
        List<Map<String, Object>> result = new ArrayList<>();
        List<ProductBatch> batches = productBatchRepository.findByProductIdOrderByPurchaseDateAsc(productId);
        
        for (ProductBatch batch : batches) {
            Map<String, Object> batchDetail = new HashMap<>();
            batchDetail.put("batchNumber", batch.getBatchNumber());
            batchDetail.put("purchaseDate", batch.getPurchaseDate());
            batchDetail.put("expiryDate", batch.getExpiryDate());
            batchDetail.put("buyingPrice", batch.getBuyingPrice());
            batchDetail.put("sellingPrice", batch.getSellingPrice());
            batchDetail.put("initialQuantity", batch.getInitialQuantity());
            batchDetail.put("remainingQuantity", batch.getRemainingQuantity());
            batchDetail.put("value", batch.getBuyingPrice().multiply(batch.getRemainingQuantity()));
            
            result.add(batchDetail);
        }
        
        return result;
    }
    
    /**
     * Get batches that are near expiry (within the given days)
     */
    public List<ProductBatch> getBatchesNearExpiry(int daysThreshold) {
        LocalDateTime thresholdDate = LocalDateTime.now().plusDays(daysThreshold);
        
        List<ProductBatch> allBatches = productBatchRepository.findAll();
        return allBatches.stream()
                .filter(batch -> batch.getRemainingQuantity().compareTo(BigDecimal.ZERO) > 0)
                .filter(batch -> batch.getExpiryDate() != null)
                .filter(batch -> batch.getExpiryDate().isBefore(thresholdDate))
                .collect(Collectors.toList());
    }
}