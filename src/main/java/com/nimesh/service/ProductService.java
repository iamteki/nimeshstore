package com.nimesh.service;

import com.nimesh.model.Category;
import com.nimesh.model.Product;
import com.nimesh.model.ProductBatch;
import com.nimesh.repository.ProductBatchRepository;
import com.nimesh.repository.ProductRepository;
import java.math.BigDecimal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProductService {
    
    
    
   

@Autowired
private SystemConfigService configService;
    
    
     @Autowired
    private ProductBatchService productBatchService;
     
       @Autowired
    private ProductBatchRepository productBatchRepository; 
    
    @Autowired
    private ProductRepository productRepository;
    
     @Autowired
    private ActivityLogService activityLogService;
    
    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }
    
    public Optional<Product> getProductById(Long id) {
        return productRepository.findById(id);
    }
    
    public Product getProductByBarcode(String barcode) {
        return productRepository.findByBarcode(barcode);
    }
    
    public List<Product> searchProductsByName(String name) {
        return productRepository.findByNameContainingIgnoreCase(name);
    }
    
    public List<Product> getProductsByCategory(Long categoryId) {
        return productRepository.findByCategoryId(categoryId);
    }
    
    public List<Product> getLowStockProducts() {
        return productRepository.findLowStockProducts();
    }
    
    public Product saveProduct(Product product) {
        if (product.getId() == null) {
            product.setDateAdded(LocalDateTime.now());
        }
        product.setLastUpdated(LocalDateTime.now());
        return productRepository.save(product);
    }
    
    public boolean deleteProduct(Long id) {
        if (productRepository.existsById(id)) {
            productRepository.deleteById(id);
            return true;
        }
        return false;
    }
    
    
    @Transactional
public boolean updateStock(Long id, BigDecimal quantity) {
    Optional<Product> optionalProduct = productRepository.findById(id);
    if (optionalProduct.isPresent()) {
        Product product = optionalProduct.get();
        
        BigDecimal oldStock = product.getCurrentStock();
        
        if (quantity.compareTo(BigDecimal.ZERO) > 0) {
            // Adding stock
            product.addStock(quantity);
            
            // When adding stock without a purchase order, create a batch
            // but only if it's a significant addition (to avoid creating batches for tiny adjustments)
            if (quantity.compareTo(new BigDecimal("0.1")) > 0) {
                // Get the batch service to create a new batch for this stock addition
                ProductBatch batch = new ProductBatch();
                batch.setProduct(product);
                batch.setBatchNumber(ProductBatch.generateBatchNumber(product, LocalDateTime.now()) + "-ADJ");
                batch.setPurchaseDate(LocalDateTime.now());
                batch.setBuyingPrice(product.getBuyingPrice());
                batch.setSellingPrice(product.getSellingPrice());
                batch.setWholesalePrice(product.getWholesalePrice());
                batch.setInitialQuantity(quantity);
                batch.setRemainingQuantity(quantity);
                
                // Save the batch
                productBatchRepository.save(batch);
                
                // Log batch creation
                String batchDescription = "Created adjustment batch #" + batch.getBatchNumber() + 
                        " for " + product.getName() + " - Qty: " + quantity;
                activityLogService.logActivity(batchDescription, "INVENTORY", null, product.getId(), "Product");
            }
        } else {
            // Reducing stock - let the batch service handle this
            try {
                // quantity is negative here, so we convert it to positive for the reduction
                BigDecimal absQuantity = quantity.abs();
                
                if (!product.hasEnoughStock(absQuantity)) {
                    return false; // Not enough stock
                }
                
                // Use the ProductBatchService to reduce from batches
                productBatchService.reduceBatchQuantitiesFIFO(product.getId(), absQuantity);
                
                // Update the product's stock too
                product.reduceStock(absQuantity);
            } catch (Exception e) {
                // If batch reduction fails for any reason
                return false;
            }
        }
        
        product.setLastUpdated(LocalDateTime.now());
        productRepository.save(product);
        
        // Log activity
        String action = quantity.compareTo(BigDecimal.ZERO) > 0 ? "added to" : "removed from";
        String activityDescription = quantity.abs() + " units " + action + " inventory for " + 
                product.getName() + ". Stock changed from " + oldStock + " to " + product.getCurrentStock();
        activityLogService.logActivity(activityDescription, "INVENTORY", null, product.getId(), "Product");
        
        return true;
    }
    return false;
}
  
    
    
    
/**
 * Gets count of products grouped by category
 */
public Map<Category, Long> getProductCountByCategory() {
    List<Product> allProducts = productRepository.findAll();
    
    Map<Category, Long> categoryCount = new HashMap<>();
    
    for (Product product : allProducts) {
        Category category = product.getCategory();
        if (category != null) {
            categoryCount.put(category, categoryCount.getOrDefault(category, 0L) + 1);
        }
    }
    
    return categoryCount;
}

/**
 * Gets products without a category assigned
 */
public List<Product> getProductsWithoutCategory() {
    List<Product> allProducts = productRepository.findAll();
    return allProducts.stream()
            .filter(product -> product.getCategory() == null)
            .collect(Collectors.toList());
}




//new codes 




/**
 * Updates product stock following the pricing strategy for stock reduction/addition
 */
@Transactional
public boolean updateProductStock(Long productId, BigDecimal quantity, String operationType) {
    Optional<Product> productOpt = productRepository.findById(productId);
    if (!productOpt.isPresent()) {
        return false;
    }
    
    Product product = productOpt.get();
    
    try {
        if ("ADD".equals(operationType)) {
            // Adding stock - create a new batch
            productBatchService.createBatch(
                productId,
                quantity,
                product.getBuyingPrice(),
                product.getSellingPrice(),
                product.getWholesalePrice(),
                null, // no purchase order
                null  // no expiry date
            );
            
            // The createBatch method automatically updates the product stock
            
            // Log activity if available
            if (activityLogService != null) {
                String description = "Added " + quantity + " units to " + product.getName() + 
                                   " - New batch created";
                activityLogService.logActivity(description, "INVENTORY", null, productId, "Product");
            }
            
        } else if ("REMOVE".equals(operationType)) {
            // Removing stock - use strategy-based reduction
            productBatchService.reduceBatchQuantitiesStrategy(productId, quantity);
            
            // Update the product's current stock
            product.setCurrentStock(product.getCurrentStock().subtract(quantity));
            productRepository.save(product);
            
            // Log activity if available
            if (activityLogService != null) {
                String description = "Removed " + quantity + " units from " + product.getName() + 
                                   " using " + configService.getPricingStrategy() + " strategy";
                activityLogService.logActivity(description, "INVENTORY", null, productId, "Product");
            }
        }
        
        return true;
    } catch (Exception e) {
        e.printStackTrace();
        return false;
    }
}

/**
 * Get available stock from batches (more accurate than product.currentStock)
 */
public BigDecimal getAvailableStock(Long productId) {
    BigDecimal batchStock = productBatchService.getTotalRemainingQuantity(productId);
    return batchStock != null ? batchStock : BigDecimal.ZERO;
}

/**
 * Checks if sufficient stock is available
 */
public boolean isStockAvailable(Long productId, BigDecimal requiredQuantity) {
    BigDecimal available = getAvailableStock(productId);
    return available.compareTo(requiredQuantity) >= 0;
}

/**
 * Synchronizes product stock with batch totals
 */
@Transactional
public void synchronizeProductStockWithBatches() {
    List<Product> allProducts = productRepository.findAll();
    
    for (Product product : allProducts) {
        BigDecimal batchTotal = getAvailableStock(product.getId());
        if (!batchTotal.equals(product.getCurrentStock())) {
            product.setCurrentStock(batchTotal);
            productRepository.save(product);
        }
    }
}



}