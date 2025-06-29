package com.nimesh.service;

import com.nimesh.model.Product;
import com.nimesh.model.ProductBatch;
import com.nimesh.model.PurchaseOrder;
import com.nimesh.repository.ProductBatchRepository;
import com.nimesh.repository.ProductRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class ProductBatchService {
    
    @Autowired
    private ProductBatchRepository productBatchRepository;
    
    @Autowired
    private ProductRepository productRepository;
    
    @Autowired
    private ActivityLogService activityLogService;
    
     @Autowired
    private SystemConfigService configService;
     
     
     
      /**
     * Gets the current selling price from the oldest batch (FIFO)
     */
    public BigDecimal getFIFOSellingPrice(Long productId, boolean isWholesale) {
        List<ProductBatch> batches = productBatchRepository.findAvailableBatchesByProductIdOrderByPurchaseDateAsc(productId);
        if (!batches.isEmpty()) {
            // Get the oldest batch with remaining quantity
            ProductBatch oldestBatch = batches.get(0);
            return isWholesale && oldestBatch.getWholesalePrice() != null
                   ? oldestBatch.getWholesalePrice()
                   : oldestBatch.getSellingPrice();
        }
        
        // Fallback to product's default price if no batches found
        Optional<Product> productOpt = productRepository.findById(productId);
        if (productOpt.isPresent()) {
            Product product = productOpt.get();
            return isWholesale && product.getWholesalePrice() != null
                   ? product.getWholesalePrice()
                   : product.getSellingPrice();
        }
        
        return BigDecimal.ZERO;
    }
    
    /**
     * Gets the weighted average selling price across all batches
     */
    public BigDecimal getAverageSellingPrice(Long productId, boolean isWholesale) {
        List<ProductBatch> batches = productBatchRepository.findAvailableBatchesByProductIdOrderByPurchaseDateAsc(productId);
        if (batches.isEmpty()) {
            // Fallback to product's default price if no batches found
            Optional<Product> productOpt = productRepository.findById(productId);
            if (productOpt.isPresent()) {
                Product product = productOpt.get();
                return isWholesale && product.getWholesalePrice() != null
                      ? product.getWholesalePrice()
                      : product.getSellingPrice();
            }
            return BigDecimal.ZERO;
        }
        
        // Calculate weighted average selling price
        BigDecimal totalValue = BigDecimal.ZERO;
        BigDecimal totalQuantity = BigDecimal.ZERO;
        
        for (ProductBatch batch : batches) {
            BigDecimal price = isWholesale && batch.getWholesalePrice() != null
                  ? batch.getWholesalePrice()
                  : batch.getSellingPrice();
                  
            totalValue = totalValue.add(price.multiply(batch.getRemainingQuantity()));
            totalQuantity = totalQuantity.add(batch.getRemainingQuantity());
        }
        
        if (totalQuantity.compareTo(BigDecimal.ZERO) > 0) {
            return totalValue.divide(totalQuantity, 2, BigDecimal.ROUND_HALF_UP);
        }
        
        // Fallback to first batch price if calculation fails
        ProductBatch firstBatch = batches.get(0);
        return isWholesale && firstBatch.getWholesalePrice() != null
              ? firstBatch.getWholesalePrice()
              : firstBatch.getSellingPrice();
    }
    
    /**
     * Gets the product selling price based on the configured pricing strategy
     */
    public BigDecimal getStrategyBasedSellingPrice(Long productId, boolean isWholesale) {
        String pricingStrategy = configService.getPricingStrategy();
        
        switch (pricingStrategy) {
            case SystemConfigService.FIFO_STRATEGY:
                return getFIFOSellingPrice(productId, isWholesale);
            case SystemConfigService.LIFO_STRATEGY:
                return getCurrentSellingPrice(productId, isWholesale);
            case SystemConfigService.AVERAGE_STRATEGY:
                return getAverageSellingPrice(productId, isWholesale);
            default:
                return getFIFOSellingPrice(productId, isWholesale);
        }
    }
     
     
     
     
    
    /**
     * Create a new product batch
     */
    @Transactional
    public ProductBatch createBatch(Long productId, BigDecimal quantity, BigDecimal buyingPrice, 
                                   BigDecimal sellingPrice, BigDecimal wholesalePrice, 
                                   PurchaseOrder purchaseOrder, LocalDateTime expiryDate) {
        Optional<Product> productOptional = productRepository.findById(productId);
        if (!productOptional.isPresent()) {
            throw new IllegalArgumentException("Product not found with ID: " + productId);
        }
        
        Product product = productOptional.get();
        
        ProductBatch batch = new ProductBatch();
        batch.setProduct(product);
        batch.setBatchNumber(ProductBatch.generateBatchNumber(product, LocalDateTime.now()));
        batch.setPurchaseDate(LocalDateTime.now());
        batch.setExpiryDate(expiryDate);
        batch.setBuyingPrice(buyingPrice);
        batch.setSellingPrice(sellingPrice);
        batch.setWholesalePrice(wholesalePrice);
        batch.setInitialQuantity(quantity);
        batch.setRemainingQuantity(quantity);
        batch.setPurchaseOrder(purchaseOrder);
        
        if (purchaseOrder != null) {
            batch.setSupplierReference(purchaseOrder.getOrderNumber());
        }
        
        // Add to product's stock
        product.addStock(quantity);
        productRepository.save(product);
        
        // Log activity
        String activityDescription = "New batch #" + batch.getBatchNumber() + " created for " + 
                product.getName() + " - Qty: " + quantity + ", Cost: " + buyingPrice;
        activityLogService.logActivity(activityDescription, "INVENTORY", null, product.getId(), "Product");
        
        return productBatchRepository.save(batch);
    }
    
    /**
     * Find batches for a product ordered by purchase date (FIFO)
     */
    public List<ProductBatch> getBatchesByProductId(Long productId) {
        return productBatchRepository.findByProductIdOrderByPurchaseDateAsc(productId);
    }
    
    /**
     * Find available batches (with remaining quantity) for a product
     */
    public List<ProductBatch> getAvailableBatchesByProductId(Long productId) {
        return productBatchRepository.findAvailableBatchesByProductIdOrderByPurchaseDateAsc(productId);
    }
    
    /**
     * Get total remaining quantity across all batches for a product
     */
    public BigDecimal getTotalRemainingQuantity(Long productId) {
        BigDecimal total = productBatchRepository.getTotalRemainingQuantityByProductId(productId);
        return total != null ? total : BigDecimal.ZERO;
    }
    
    /**
     * Reduces quantity from batches using FIFO method
     * @param productId The product ID
     * @param quantityToReduce The quantity to be reduced
     * @return List of batches and quantities used
     */
   @Transactional
public List<BatchUsage> reduceBatchQuantitiesFIFO(Long productId, BigDecimal quantityToReduce) {
    // Check if we have enough total quantity
    BigDecimal totalAvailable = getTotalRemainingQuantity(productId);
    
    // Get the product
    Optional<Product> productOpt = productRepository.findById(productId);
    if (!productOpt.isPresent()) {
        throw new IllegalStateException("Product not found with ID: " + productId);
    }
    Product product = productOpt.get();
    
    // Check for discrepancy between product.currentStock and sum of batch quantities
    BigDecimal productStock = product.getCurrentStock();
    BigDecimal unbatchedQuantity = productStock.subtract(totalAvailable);
    
    // If there's unbatched inventory (productStock > sum of batch quantities)
    if (unbatchedQuantity.compareTo(BigDecimal.ZERO) > 0) {
        // Create a new batch for the unbatched quantity
        ProductBatch newBatch = new ProductBatch();
        newBatch.setProduct(product);
        newBatch.setBatchNumber(ProductBatch.generateBatchNumber(product, LocalDateTime.now()) + "-AUTO");
        newBatch.setPurchaseDate(LocalDateTime.now());
        newBatch.setBuyingPrice(product.getBuyingPrice());
        newBatch.setSellingPrice(product.getSellingPrice());
        newBatch.setWholesalePrice(product.getWholesalePrice());
        newBatch.setInitialQuantity(unbatchedQuantity);
        newBatch.setRemainingQuantity(unbatchedQuantity);
        
        // Save the batch
        productBatchRepository.save(newBatch);
        
        // Recalculate total available
        totalAvailable = totalAvailable.add(unbatchedQuantity);
        
        // Log this automatic batch creation
        String activityDescription = "Auto-created batch #" + newBatch.getBatchNumber() + 
                " for " + product.getName() + " - Qty: " + unbatchedQuantity;
        activityLogService.logActivity(activityDescription, "INVENTORY", null, product.getId(), "Product");
    }
    
    // Now check if we have enough after potential auto-batch creation
    if (totalAvailable.compareTo(quantityToReduce) < 0) {
        throw new IllegalStateException("Not enough quantity available. Requested: " + 
                quantityToReduce + ", Available: " + totalAvailable);
    }
    
    List<ProductBatch> batches = getAvailableBatchesByProductId(productId);
    List<BatchUsage> usages = new ArrayList<>();
    BigDecimal remainingToReduce = quantityToReduce;
    
    for (ProductBatch batch : batches) {
        if (remainingToReduce.compareTo(BigDecimal.ZERO) <= 0) {
            break;
        }
        
        BigDecimal batchAvailable = batch.getRemainingQuantity();
        BigDecimal quantityFromBatch;
        
        if (batchAvailable.compareTo(remainingToReduce) >= 0) {
            // This batch has enough to fulfill the remaining quantity
            quantityFromBatch = remainingToReduce;
            remainingToReduce = BigDecimal.ZERO;
        } else {
            // Use all available from this batch
            quantityFromBatch = batchAvailable;
            remainingToReduce = remainingToReduce.subtract(batchAvailable);
        }
        
        // Reduce from the batch
        batch.setRemainingQuantity(batchAvailable.subtract(quantityFromBatch));
        productBatchRepository.save(batch);
        
        // Record usage
        BatchUsage usage = new BatchUsage(batch, quantityFromBatch);
        usages.add(usage);
        
        // Log activity
        String activityDescription = "Reduced " + quantityFromBatch + " units from batch #" + 
                batch.getBatchNumber() + " - Remaining: " + batch.getRemainingQuantity();
        activityLogService.logActivity(activityDescription, "INVENTORY", null, batch.getId(), "ProductBatch");
    }
    
    return usages;
}
    
    /**
 * Gets the current selling price from the most recent batch
 */
public BigDecimal getCurrentSellingPrice(Long productId, boolean isWholesale) {
    List<ProductBatch> batches = productBatchRepository.findByProductIdOrderByPurchaseDateDesc(productId);
    if (!batches.isEmpty()) {
        ProductBatch latestBatch = batches.get(0);
        return isWholesale && latestBatch.getWholesalePrice() != null
               ? latestBatch.getWholesalePrice()
               : latestBatch.getSellingPrice();
    }
    
    // Fallback to product's default price if no batches found
    Optional<Product> productOpt = productRepository.findById(productId);
    if (productOpt.isPresent()) {
        Product product = productOpt.get();
        return isWholesale && product.getWholesalePrice() != null
               ? product.getWholesalePrice()
               : product.getSellingPrice();
    }
    
    return BigDecimal.ZERO;
}
    
    /**
 * Updates an existing product batch
 */
@Transactional
public ProductBatch updateBatch(ProductBatch batch) {
    // Log activity
    String activityDescription = "Updated batch #" + batch.getBatchNumber() + 
            " for " + batch.getProduct().getName() + 
            " - Selling Price: " + batch.getSellingPrice();
    activityLogService.logActivity(activityDescription, "INVENTORY", null, batch.getId(), "ProductBatch");
    
    return productBatchRepository.save(batch);
}
    
    /**
     * Class to represent batch usage for a sale
     */
    public static class BatchUsage {
        private ProductBatch batch;
        private BigDecimal quantityUsed;
        
        public BatchUsage(ProductBatch batch, BigDecimal quantityUsed) {
            this.batch = batch;
            this.quantityUsed = quantityUsed;
        }
        
        public ProductBatch getBatch() {
            return batch;
        }
        
        public BigDecimal getQuantityUsed() {
            return quantityUsed;
        }
    }
    
    
    /**
 * Get batch availability information for a product
 */
public List<BatchInfo> getBatchAvailabilityInfo(Long productId) {
    List<ProductBatch> batches = productBatchRepository.findAvailableBatchesByProductIdOrderByPurchaseDateAsc(productId);
    List<BatchInfo> batchInfoList = new ArrayList<>();
    
    for (ProductBatch batch : batches) {
        if (batch.getRemainingQuantity().compareTo(BigDecimal.ZERO) > 0) {
            BatchInfo info = new BatchInfo(
                batch.getBatchNumber(), 
                batch.getRemainingQuantity(),
                batch.getSellingPrice(),
                batch.getWholesalePrice(),
                batch.getPurchaseDate()
            );
            batchInfoList.add(info);
        }
    }
    
    return batchInfoList;
}

/**
 * Class to hold batch availability information
 */
public static class BatchInfo {
    private String batchNumber;
    private BigDecimal availableQuantity;
    private BigDecimal sellingPrice;
    private BigDecimal wholesalePrice;
    private LocalDateTime purchaseDate;
    
    public BatchInfo(String batchNumber, BigDecimal availableQuantity, 
                     BigDecimal sellingPrice, BigDecimal wholesalePrice,
                     LocalDateTime purchaseDate) {
        this.batchNumber = batchNumber;
        this.availableQuantity = availableQuantity;
        this.sellingPrice = sellingPrice;
        this.wholesalePrice = wholesalePrice;
        this.purchaseDate = purchaseDate;
    }
    
    public String getBatchNumber() {
        return batchNumber;
    }
    
    public BigDecimal getAvailableQuantity() {
        return availableQuantity;
    }
    
    public BigDecimal getSellingPrice() {
        return sellingPrice;
    }
    
    public BigDecimal getWholesalePrice() {
        return wholesalePrice;
    }
    
    public LocalDateTime getPurchaseDate() {
        return purchaseDate;
    }
}


/**
 * Synchronize batches with product stock levels
 * @return Number of batches created
 */
@Transactional
public int synchronizeBatchesWithProductStock() {
    List<Product> products = productRepository.findAll();
    int batchesCreated = 0;
    
    for (Product product : products) {
        BigDecimal productStock = product.getCurrentStock();
        if (productStock.compareTo(BigDecimal.ZERO) <= 0) {
            continue; // Skip products with no stock
        }
        
        BigDecimal batchTotal = getTotalRemainingQuantity(product.getId());
        if (batchTotal == null) {
            batchTotal = BigDecimal.ZERO;
        }
        
        // If there's a discrepancy
        BigDecimal difference = productStock.subtract(batchTotal);
        if (difference.compareTo(BigDecimal.ZERO) > 0) {
            // Create a new batch for the unbatched quantity
            ProductBatch newBatch = new ProductBatch();
            newBatch.setProduct(product);
            newBatch.setBatchNumber(ProductBatch.generateBatchNumber(product, LocalDateTime.now()) + "-SYNC");
            newBatch.setPurchaseDate(LocalDateTime.now());
            newBatch.setBuyingPrice(product.getBuyingPrice());
            newBatch.setSellingPrice(product.getSellingPrice());
            newBatch.setWholesalePrice(product.getWholesalePrice());
            newBatch.setInitialQuantity(difference);
            newBatch.setRemainingQuantity(difference);
            
            productBatchRepository.save(newBatch);
            batchesCreated++;
            
            // Log the synchronization
            String activityDescription = "Created synchronization batch #" + newBatch.getBatchNumber() + 
                    " for " + product.getName() + " - Qty: " + difference;
            activityLogService.logActivity(activityDescription, "INVENTORY", null, product.getId(), "Product");
        } else if (difference.compareTo(BigDecimal.ZERO) < 0) {
            // Batches have more stock than the product - update product stock
            product.setCurrentStock(batchTotal);
            productRepository.save(product);
            
            // Log the synchronization
            String activityDescription = "Updated product stock for " + product.getName() + 
                    " from " + productStock + " to " + batchTotal + " to match batches";
            activityLogService.logActivity(activityDescription, "INVENTORY", null, product.getId(), "Product");
        }
    }
    
    return batchesCreated;
}
    
// Add these methods to your ProductBatchService class

/**
 * Gets the current buying price from the oldest batch (FIFO)
 */
public BigDecimal getFIFOBuyingPrice(Long productId) {
    List<ProductBatch> batches = productBatchRepository.findAvailableBatchesByProductIdOrderByPurchaseDateAsc(productId);
    if (!batches.isEmpty()) {
        return batches.get(0).getBuyingPrice();
    }
    
    // Fallback to product's default buying price if no batches found
    Optional<Product> productOpt = productRepository.findById(productId);
    if (productOpt.isPresent()) {
        return productOpt.get().getBuyingPrice();
    }
    
    return BigDecimal.ZERO;
}

/**
 * Gets the buying price from the most recent batch (LIFO)
 */
public BigDecimal getLIFOBuyingPrice(Long productId) {
    List<ProductBatch> batches = productBatchRepository.findByProductIdOrderByPurchaseDateDesc(productId);
    if (!batches.isEmpty()) {
        return batches.get(0).getBuyingPrice();
    }
    
    // Fallback to product's default buying price if no batches found
    Optional<Product> productOpt = productRepository.findById(productId);
    if (productOpt.isPresent()) {
        return productOpt.get().getBuyingPrice();
    }
    
    return BigDecimal.ZERO;
}

/**
 * Gets the weighted average buying price across all batches
 */
public BigDecimal getAverageBuyingPrice(Long productId) {
    List<ProductBatch> batches = productBatchRepository.findAvailableBatchesByProductIdOrderByPurchaseDateAsc(productId);
    if (batches.isEmpty()) {
        // Fallback to product's default buying price if no batches found
        Optional<Product> productOpt = productRepository.findById(productId);
        if (productOpt.isPresent()) {
            return productOpt.get().getBuyingPrice();
        }
        return BigDecimal.ZERO;
    }
    
    // Calculate weighted average buying price
    BigDecimal totalCost = BigDecimal.ZERO;
    BigDecimal totalQuantity = BigDecimal.ZERO;
    
    for (ProductBatch batch : batches) {
        totalCost = totalCost.add(batch.getBuyingPrice().multiply(batch.getRemainingQuantity()));
        totalQuantity = totalQuantity.add(batch.getRemainingQuantity());
    }
    
    if (totalQuantity.compareTo(BigDecimal.ZERO) > 0) {
        return totalCost.divide(totalQuantity, 2, BigDecimal.ROUND_HALF_UP);
    }
    
    // Fallback to first batch price if calculation fails
    return batches.get(0).getBuyingPrice();
}

/**
 * Gets the product buying price based on the configured pricing strategy
 */
public BigDecimal getStrategyBasedBuyingPrice(Long productId) {
    String pricingStrategy = configService.getPricingStrategy();
    
    switch (pricingStrategy) {
        case SystemConfigService.FIFO_STRATEGY:
            return getFIFOBuyingPrice(productId);
        case SystemConfigService.LIFO_STRATEGY:
            return getLIFOBuyingPrice(productId);
        case SystemConfigService.AVERAGE_STRATEGY:
            return getAverageBuyingPrice(productId);
        default:
            return getFIFOBuyingPrice(productId);
    }
}




// Add these methods to ProductBatchService.java

/**
 * Reduces quantity from batches using LIFO method (most recent batches first)
 * @param productId The product ID
 * @param quantityToReduce The quantity to be reduced
 * @return List of batches and quantities used
 */
@Transactional
public List<BatchUsage> reduceBatchQuantitiesLIFO(Long productId, BigDecimal quantityToReduce) {
    // Check if we have enough total quantity
    BigDecimal totalAvailable = getTotalRemainingQuantity(productId);
    
    // Get the product
    Optional<Product> productOpt = productRepository.findById(productId);
    if (!productOpt.isPresent()) {
        throw new IllegalStateException("Product not found with ID: " + productId);
    }
    Product product = productOpt.get();
    
    // Check for discrepancy between product.currentStock and sum of batch quantities
    BigDecimal productStock = product.getCurrentStock();
    BigDecimal unbatchedQuantity = productStock.subtract(totalAvailable);
    
    // If there's unbatched inventory, create a new batch
    if (unbatchedQuantity.compareTo(BigDecimal.ZERO) > 0) {
        ProductBatch newBatch = new ProductBatch();
        newBatch.setProduct(product);
        newBatch.setBatchNumber(ProductBatch.generateBatchNumber(product, LocalDateTime.now()) + "-AUTO");
        newBatch.setPurchaseDate(LocalDateTime.now());
        newBatch.setBuyingPrice(product.getBuyingPrice());
        newBatch.setSellingPrice(product.getSellingPrice());
        newBatch.setWholesalePrice(product.getWholesalePrice());
        newBatch.setInitialQuantity(unbatchedQuantity);
        newBatch.setRemainingQuantity(unbatchedQuantity);
        
        productBatchRepository.save(newBatch);
        totalAvailable = totalAvailable.add(unbatchedQuantity);
        
        // Log this automatic batch creation
        String activityDescription = "Auto-created batch #" + newBatch.getBatchNumber() + 
                " for " + product.getName() + " - Qty: " + unbatchedQuantity;
        activityLogService.logActivity(activityDescription, "INVENTORY", null, product.getId(), "Product");
    }
    
    // Check if we have enough after potential auto-batch creation
    if (totalAvailable.compareTo(quantityToReduce) < 0) {
        throw new IllegalStateException("Not enough quantity available. Requested: " + 
                quantityToReduce + ", Available: " + totalAvailable);
    }
    
    // Get batches ordered by purchase date DESC (newest first) for LIFO
    List<ProductBatch> batches = productBatchRepository.findByProductIdOrderByPurchaseDateDesc(productId);
    
    // Filter only available batches
    batches = batches.stream()
            .filter(batch -> batch.getRemainingQuantity().compareTo(BigDecimal.ZERO) > 0)
            .collect(java.util.stream.Collectors.toList());
    
    List<BatchUsage> usages = new ArrayList<>();
    BigDecimal remainingToReduce = quantityToReduce;
    
    for (ProductBatch batch : batches) {
        if (remainingToReduce.compareTo(BigDecimal.ZERO) <= 0) {
            break;
        }
        
        BigDecimal batchAvailable = batch.getRemainingQuantity();
        BigDecimal quantityFromBatch;
        
        if (batchAvailable.compareTo(remainingToReduce) >= 0) {
            quantityFromBatch = remainingToReduce;
            remainingToReduce = BigDecimal.ZERO;
        } else {
            quantityFromBatch = batchAvailable;
            remainingToReduce = remainingToReduce.subtract(batchAvailable);
        }
        
        // Reduce from the batch
        batch.setRemainingQuantity(batchAvailable.subtract(quantityFromBatch));
        productBatchRepository.save(batch);
        
        // Record usage
        BatchUsage usage = new BatchUsage(batch, quantityFromBatch);
        usages.add(usage);
        
        // Log activity
        String activityDescription = "Reduced " + quantityFromBatch + " units from batch #" + 
                batch.getBatchNumber() + " - Remaining: " + batch.getRemainingQuantity();
        activityLogService.logActivity(activityDescription, "INVENTORY", null, batch.getId(), "ProductBatch");
    }
    
    return usages;
}

/**
 * Reduces quantity from batches using Average Cost method
 * For simplicity, this uses FIFO for actual reduction but could be enhanced
 * to distribute the reduction proportionally across all batches
 */
@Transactional
public List<BatchUsage> reduceBatchQuantitiesAverage(Long productId, BigDecimal quantityToReduce) {
    // For average cost, we still use FIFO for physical inventory reduction
    // but the pricing is calculated as weighted average
    return reduceBatchQuantitiesFIFO(productId, quantityToReduce);
}

/**
 * Reduces batch quantities based on the configured pricing strategy
 */
@Transactional
public List<BatchUsage> reduceBatchQuantitiesStrategy(Long productId, BigDecimal quantityToReduce) {
    String pricingStrategy = configService.getPricingStrategy();
    
    switch (pricingStrategy) {
        case SystemConfigService.FIFO_STRATEGY:
            return reduceBatchQuantitiesFIFO(productId, quantityToReduce);
        case SystemConfigService.LIFO_STRATEGY:
            return reduceBatchQuantitiesLIFO(productId, quantityToReduce);
        case SystemConfigService.AVERAGE_STRATEGY:
            return reduceBatchQuantitiesAverage(productId, quantityToReduce);
        default:
            return reduceBatchQuantitiesFIFO(productId, quantityToReduce);
    }
}



    
}