package com.nimesh.service;

import com.nimesh.model.Category;
import com.nimesh.model.Customer;
import com.nimesh.model.Invoice;
import com.nimesh.model.InvoiceItem;
import com.nimesh.model.InvoiceItemBatch;
import com.nimesh.model.Product;
import com.nimesh.model.ProductBatch;
import com.nimesh.repository.InvoiceRepository;
import com.nimesh.repository.InvoiceItemBatchRepository;
import com.nimesh.repository.ProductRepository;
import com.nimesh.repository.CategoryRepository;
import com.nimesh.repository.CustomerRepository;
import com.nimesh.repository.ProductBatchRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ReportingService {
    
    private static final int DECIMAL_SCALE = 2;
    private static final RoundingMode ROUNDING_MODE = RoundingMode.HALF_UP;

    @Autowired
    private InvoiceRepository invoiceRepository;
    
    @Autowired
    private InvoiceItemBatchRepository invoiceItemBatchRepository;
    
    @Autowired
    private ProductRepository productRepository;
    
    @Autowired
    private CategoryRepository categoryRepository;
    
    @Autowired
    private CustomerRepository customerRepository;
    
    @Autowired
    private ProductBatchRepository productBatchRepository;
    
    @Autowired
    private SystemConfigService systemConfigService;
    
    /**
     * Get sales data grouped by date
     */
    public Map<LocalDate, BigDecimal> getSalesByDate(LocalDateTime startDate, LocalDateTime endDate) {
        List<Invoice> invoices = invoiceRepository.findByDateBetween(startDate, endDate);
        Map<LocalDate, BigDecimal> salesByDate = new TreeMap<>();
        
        for (Invoice invoice : invoices) {
            LocalDate date = invoice.getDate().toLocalDate();
            BigDecimal amount = invoice.getFinalAmount();
            
            salesByDate.merge(date, amount, BigDecimal::add);
        }
        
        return salesByDate;
    }
    
    /**
     * Get dates sorted chronologically
     */
    public List<Map.Entry<LocalDate, BigDecimal>> getDatesChronologically(Map<LocalDate, BigDecimal> salesByDate) {
        return salesByDate.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .collect(Collectors.toList());
    }
    
    /**
     * Get sales data grouped by product with actual batch costs
     */
    public Map<Product, Map<String, Object>> getProductSales(LocalDateTime startDate, LocalDateTime endDate) {
        List<Invoice> invoices = invoiceRepository.findByDateBetween(startDate, endDate);
        Map<Product, Map<String, Object>> productSales = new HashMap<>();
        
        for (Invoice invoice : invoices) {
            for (InvoiceItem item : invoice.getItems()) {
                Product product = item.getProduct();
                if (product == null) {
                    continue;
                }
                
                BigDecimal quantity = item.getQuantity();
                if (quantity == null || quantity.compareTo(BigDecimal.ZERO) <= 0) {
                    continue;
                }
                
                BigDecimal salesValue = item.getTotal();
                
                // Get actual cost from batches
                BigDecimal totalCost = getActualCostForInvoiceItem(item);
                
                Map<String, Object> productData = productSales.computeIfAbsent(product, k -> new HashMap<>());
                
                BigDecimal totalQuantity = (BigDecimal) productData.getOrDefault("quantity", BigDecimal.ZERO);
                BigDecimal totalSalesValue = (BigDecimal) productData.getOrDefault("salesValue", BigDecimal.ZERO);
                BigDecimal totalCostValue = (BigDecimal) productData.getOrDefault("totalCost", BigDecimal.ZERO);
                
                productData.put("quantity", totalQuantity.add(quantity));
                productData.put("salesValue", totalSalesValue.add(salesValue));
                productData.put("totalCost", totalCostValue.add(totalCost));
                
                productSales.put(product, productData);
            }
        }
        
        return productSales;
    }
    
   /**
 * Get actual cost for an invoice item from batch data (PUBLIC method)
 */
public BigDecimal getActualCostForInvoiceItem(InvoiceItem item) {
    List<InvoiceItemBatch> batches = invoiceItemBatchRepository.findByInvoiceItemId(item.getId());
    
    if (batches.isEmpty()) {
        // Fallback to buying price from invoice item or product
        if (item.getBuyingPrice() != null) {
            return item.getBuyingPrice().multiply(item.getQuantity());
        } else {
            return item.getProduct().getBuyingPrice().multiply(item.getQuantity());
        }
    }
    
    BigDecimal totalCost = BigDecimal.ZERO;
    for (InvoiceItemBatch batch : batches) {
        BigDecimal batchCost = batch.getUnitCost().multiply(batch.getQuantity());
        totalCost = totalCost.add(batchCost);
    }
    
    return totalCost;
}
    /**
     * Get top products by sales value
     */
    public List<Map.Entry<Product, Map<String, Object>>> getTopProductsBySalesValue(
            Map<Product, Map<String, Object>> productSales, int limit) {
        
        return productSales.entrySet().stream()
                .sorted((e1, e2) -> {
                    BigDecimal value1 = (BigDecimal) e1.getValue().get("salesValue");
                    BigDecimal value2 = (BigDecimal) e2.getValue().get("salesValue");
                    return value2.compareTo(value1);
                })
                .limit(limit)
                .collect(Collectors.toList());
    }
    
    /**
     * Get all products sorted by sales value
     */
    public List<Map.Entry<Product, Map<String, Object>>> getAllProductsSortedBySalesValue(
            Map<Product, Map<String, Object>> productSales) {
        
        return productSales.entrySet().stream()
                .sorted((e1, e2) -> {
                    BigDecimal value1 = (BigDecimal) e1.getValue().get("salesValue");
                    BigDecimal value2 = (BigDecimal) e2.getValue().get("salesValue");
                    return value2.compareTo(value1);
                })
                .collect(Collectors.toList());
    }
    
    /**
     * Get sales data grouped by category
     */
    public Map<Category, BigDecimal> getCategorySales(LocalDateTime startDate, LocalDateTime endDate) {
        List<Invoice> invoices = invoiceRepository.findByDateBetween(startDate, endDate);
        Map<Category, BigDecimal> categorySales = new HashMap<>();
        
        for (Invoice invoice : invoices) {
            for (InvoiceItem item : invoice.getItems()) {
                Product product = item.getProduct();
                if (product == null) continue;
                
                Category category = product.getCategory();
                BigDecimal salesValue = item.getTotal();
                
                categorySales.merge(category, salesValue, BigDecimal::add);
            }
        }
        
        return categorySales;
    }
    
    /**
     * Get categories sorted by sales value
     */
    public List<Map.Entry<Category, BigDecimal>> getCategoriesSortedBySalesValue(
            Map<Category, BigDecimal> categorySales) {
        
        return categorySales.entrySet().stream()
                .sorted((e1, e2) -> e2.getValue().compareTo(e1.getValue()))
                .collect(Collectors.toList());
    }
    
    /**
     * Get sales data grouped by payment method
     */
    public Map<String, BigDecimal> getSalesByPaymentMethod(LocalDateTime startDate, LocalDateTime endDate) {
        List<Invoice> invoices = invoiceRepository.findByDateBetween(startDate, endDate);
        Map<String, BigDecimal> paymentMethodSales = new HashMap<>();
        
        for (Invoice invoice : invoices) {
            String paymentMethod = invoice.getPaymentMethod();
            BigDecimal amount = invoice.getFinalAmount();
            
            paymentMethodSales.merge(paymentMethod, amount, BigDecimal::add);
        }
        
        return paymentMethodSales;
    }
    
    /**
     * Get sales data grouped by customer type
     */
    public Map<String, BigDecimal> getSalesByCustomerType(LocalDateTime startDate, LocalDateTime endDate) {
        List<Invoice> invoices = invoiceRepository.findByDateBetween(startDate, endDate);
        Map<String, BigDecimal> customerTypeSales = new HashMap<>();
        
        for (Invoice invoice : invoices) {
            String customerType = invoice.getCustomerType();
            BigDecimal amount = invoice.getFinalAmount();
            
            customerTypeSales.merge(customerType, amount, BigDecimal::add);
        }
        
        return customerTypeSales;
    }
    
    /**
     * Get top customers by purchase value
     */
    public List<Map.Entry<Customer, BigDecimal>> getTopCustomersByPurchaseValue(
            LocalDateTime startDate, LocalDateTime endDate, int limit) {
        
        List<Invoice> invoices = invoiceRepository.findByDateBetween(startDate, endDate);
        Map<Customer, BigDecimal> customerPurchases = new HashMap<>();
        
        for (Invoice invoice : invoices) {
            Customer customer = invoice.getCustomer();
            if (customer != null) {
                BigDecimal amount = invoice.getFinalAmount();
                customerPurchases.merge(customer, amount, BigDecimal::add);
            }
        }
        
        return customerPurchases.entrySet().stream()
                .sorted((e1, e2) -> e2.getValue().compareTo(e1.getValue()))
                .limit(limit)
                .collect(Collectors.toList());
    }
    
    /**
     * Calculate total inventory value using current batch buying prices
     */
    public BigDecimal getTotalInventoryValueByBatch() {
        List<Product> products = productRepository.findAll();
        BigDecimal totalValue = BigDecimal.ZERO;
        
        for (Product product : products) {
            List<Object[]> batchValuations = productBatchRepository.getBatchValuationByProductId(product.getId());
            
            for (Object[] batchValuation : batchValuations) {
                BigDecimal batchBuyingPrice = (BigDecimal) batchValuation[0];
                BigDecimal batchQuantity = (BigDecimal) batchValuation[1];
                totalValue = totalValue.add(batchBuyingPrice.multiply(batchQuantity));
            }
        }
        
        return totalValue.setScale(DECIMAL_SCALE, ROUNDING_MODE);
    }
    
    /**
     * Get inventory valuation by category using batch buying prices
     */
    public Map<Category, BigDecimal> getInventoryValueByCategoryUsingBatch() {
        List<Product> products = productRepository.findAll();
        Map<Category, BigDecimal> inventoryByCategory = new HashMap<>();
        
        for (Product product : products) {
            Category category = product.getCategory();
            
            List<Object[]> batchValuations = productBatchRepository.getBatchValuationByProductId(product.getId());
            
            BigDecimal productValue = BigDecimal.ZERO;
            for (Object[] batchValuation : batchValuations) {
                BigDecimal batchBuyingPrice = (BigDecimal) batchValuation[0];
                BigDecimal batchQuantity = (BigDecimal) batchValuation[1];
                productValue = productValue.add(batchBuyingPrice.multiply(batchQuantity));
            }
            
            if (productValue.compareTo(BigDecimal.ZERO) > 0) {
                inventoryByCategory.merge(category, productValue, BigDecimal::add);
            }
        }
        
        return inventoryByCategory;
    }
    
    /**
     * Get low stock products
     */
    public List<Product> getLowStockProducts() {
        List<Product> allProducts = productRepository.findAll();
        
        return allProducts.stream()
                .filter(product -> {
                    BigDecimal currentStock = product.getCurrentStock();
                    BigDecimal reorderLevel = product.getReorderLevel();
                    
                    if (reorderLevel == null || reorderLevel.compareTo(BigDecimal.ZERO) == 0) {
                        return currentStock.compareTo(BigDecimal.ZERO) == 0;
                    }
                    
                    return currentStock.compareTo(reorderLevel) <= 0;
                })
                .sorted((p1, p2) -> {
                    BigDecimal stock1 = p1.getCurrentStock();
                    BigDecimal reorder1 = p1.getReorderLevel();
                    BigDecimal stock2 = p2.getCurrentStock();
                    BigDecimal reorder2 = p2.getReorderLevel();
                    
                    // Handle zero reorder levels
                    if (reorder1 == null || reorder1.compareTo(BigDecimal.ZERO) == 0) {
                        reorder1 = BigDecimal.ONE;
                    }
                    if (reorder2 == null || reorder2.compareTo(BigDecimal.ZERO) == 0) {
                        reorder2 = BigDecimal.ONE;
                    }
                    
                    BigDecimal ratio1 = stock1.divide(reorder1, 4, ROUNDING_MODE);
                    BigDecimal ratio2 = stock2.divide(reorder2, 4, ROUNDING_MODE);
                    
                    return ratio1.compareTo(ratio2);
                })
                .collect(Collectors.toList());
    }
    
    /**
     * Get inventory turnover rate
     */
    public Map<Product, Double> getInventoryTurnoverRate(LocalDateTime startDate, LocalDateTime endDate) {
        Map<Product, Map<String, Object>> productSales = getProductSales(startDate, endDate);
        Map<Product, Double> turnoverRates = new HashMap<>();
        
        for (Product product : productRepository.findAll()) {
            Map<String, Object> salesData = findProductSalesData(productSales, product.getId());
            
            if (salesData != null) {
                BigDecimal quantitySold = (BigDecimal) salesData.get("quantity");
                BigDecimal currentStock = product.getCurrentStock();
                BigDecimal reorderLevel = product.getReorderLevel();
                
                // Calculate average inventory (current + reorder level) / 2
                BigDecimal averageStock = currentStock.add(reorderLevel != null ? reorderLevel : BigDecimal.ZERO)
                        .divide(BigDecimal.valueOf(2), 4, ROUNDING_MODE);
                
                if (averageStock.compareTo(BigDecimal.ZERO) > 0) {
                    double turnoverRate = quantitySold.divide(averageStock, 4, ROUNDING_MODE).doubleValue();
                    turnoverRates.put(product, turnoverRate);
                } else {
                    turnoverRates.put(product, 0.0);
                }
            } else {
                turnoverRates.put(product, 0.0);
            }
        }
        
        return turnoverRates;
    }
    
    /**
     * Helper method to find sales data for a specific product by ID
     */
    private Map<String, Object> findProductSalesData(Map<Product, Map<String, Object>> productSales, Long productId) {
        for (Map.Entry<Product, Map<String, Object>> entry : productSales.entrySet()) {
            if (entry.getKey().getId().equals(productId)) {
                return entry.getValue();
            }
        }
        return null;
    }
    
    /**
     * Calculate gross profit using actual batch costs
     */
    public BigDecimal calculateGrossProfit(LocalDateTime startDate, LocalDateTime endDate) {
        List<Invoice> invoices = invoiceRepository.findByDateBetween(startDate, endDate);
        
        BigDecimal totalSales = BigDecimal.ZERO;
        BigDecimal totalCOGS = BigDecimal.ZERO;
        
        for (Invoice invoice : invoices) {
            totalSales = totalSales.add(invoice.getFinalAmount());
            
            for (InvoiceItem item : invoice.getItems()) {
                BigDecimal itemCost = getActualCostForInvoiceItem(item);
                totalCOGS = totalCOGS.add(itemCost);
            }
        }
        
        return totalSales.subtract(totalCOGS).setScale(DECIMAL_SCALE, ROUNDING_MODE);
    }
    
    /**
     * Calculate profit margin using actual batch costs
     */
    public double calculateProfitMargin(LocalDateTime startDate, LocalDateTime endDate) {
        List<Invoice> invoices = invoiceRepository.findByDateBetween(startDate, endDate);
        
        BigDecimal totalSales = BigDecimal.ZERO;
        BigDecimal totalCOGS = BigDecimal.ZERO;
        
        for (Invoice invoice : invoices) {
            totalSales = totalSales.add(invoice.getFinalAmount());
            
            for (InvoiceItem item : invoice.getItems()) {
                BigDecimal itemCost = getActualCostForInvoiceItem(item);
                totalCOGS = totalCOGS.add(itemCost);
            }
        }
        
        if (totalSales.compareTo(BigDecimal.ZERO) == 0) {
            return 0.0;
        }
        
        BigDecimal grossProfit = totalSales.subtract(totalCOGS);
        return grossProfit.divide(totalSales, 4, ROUNDING_MODE)
                .multiply(BigDecimal.valueOf(100))
                .doubleValue();
    }
    
    /**
     * Calculate product profit margins using actual batch costs
     */
    public Map<Product, Double> getProductProfitMargins(LocalDateTime startDate, LocalDateTime endDate) {
        Map<Product, Map<String, Object>> productSales = getProductSales(startDate, endDate);
        Map<Product, Double> profitMargins = new HashMap<>();
        
        for (Map.Entry<Product, Map<String, Object>> entry : productSales.entrySet()) {
            Product product = entry.getKey();
            Map<String, Object> salesData = entry.getValue();
            
            BigDecimal salesValue = (BigDecimal) salesData.get("salesValue");
            BigDecimal totalCost = (BigDecimal) salesData.getOrDefault("totalCost", BigDecimal.ZERO);
            
            if (salesValue.compareTo(BigDecimal.ZERO) > 0) {
                BigDecimal profit = salesValue.subtract(totalCost);
                double marginPercentage = profit.divide(salesValue, 4, ROUNDING_MODE)
                        .multiply(BigDecimal.valueOf(100))
                        .doubleValue();
                
                profitMargins.put(product, marginPercentage);
            } else {
                profitMargins.put(product, 0.0);
            }
        }
        
        return profitMargins;
    }
    
    /**
     * Get detailed product cost analysis with batch information
     */
    public Map<Product, Map<String, Object>> getProductCostAnalysis(LocalDateTime startDate, LocalDateTime endDate) {
        List<Invoice> invoices = invoiceRepository.findByDateBetween(startDate, endDate);
        Map<Product, Map<String, Object>> productAnalysis = new HashMap<>();
        
        for (Invoice invoice : invoices) {
            for (InvoiceItem item : invoice.getItems()) {
                Product product = item.getProduct();
                if (product == null) continue;
                
                Map<String, Object> analysis = productAnalysis.computeIfAbsent(product, k -> new HashMap<>());
                
                // Get batch details for this item
                List<InvoiceItemBatch> batches = invoiceItemBatchRepository.findByInvoiceItemId(item.getId());
                List<Map<String, Object>> batchDetails = new ArrayList<>();
                
                BigDecimal totalCost = BigDecimal.ZERO;
                BigDecimal totalQuantity = BigDecimal.ZERO;
                
                for (InvoiceItemBatch batch : batches) {
                    Map<String, Object> batchInfo = new HashMap<>();
                    batchInfo.put("quantity", batch.getQuantity());
                    batchInfo.put("unitCost", batch.getUnitCost());
                    batchInfo.put("unitPrice", batch.getUnitPrice());
                    batchInfo.put("totalCost", batch.getUnitCost().multiply(batch.getQuantity()));
                    batchInfo.put("totalRevenue", batch.getUnitPrice().multiply(batch.getQuantity()));
                    
                    batchDetails.add(batchInfo);
                    totalCost = totalCost.add(batch.getUnitCost().multiply(batch.getQuantity()));
                    totalQuantity = totalQuantity.add(batch.getQuantity());
                }
                
                // Update cumulative analysis
                BigDecimal existingCost = (BigDecimal) analysis.getOrDefault("totalCost", BigDecimal.ZERO);
                BigDecimal existingQuantity = (BigDecimal) analysis.getOrDefault("totalQuantity", BigDecimal.ZERO);
                BigDecimal existingRevenue = (BigDecimal) analysis.getOrDefault("totalRevenue", BigDecimal.ZERO);
                
                analysis.put("totalCost", existingCost.add(totalCost));
                analysis.put("totalQuantity", existingQuantity.add(totalQuantity));
                analysis.put("totalRevenue", existingRevenue.add(item.getTotal()));
                
                // Add batch details
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> existingBatches = (List<Map<String, Object>>) analysis.getOrDefault("batches", new ArrayList<>());
                existingBatches.addAll(batchDetails);
                analysis.put("batches", existingBatches);
            }
        }
        
        // Calculate margins for each product
        for (Map.Entry<Product, Map<String, Object>> entry : productAnalysis.entrySet()) {
            Map<String, Object> analysis = entry.getValue();
            BigDecimal totalCost = (BigDecimal) analysis.get("totalCost");
            BigDecimal totalRevenue = (BigDecimal) analysis.get("totalRevenue");
            
            if (totalRevenue.compareTo(BigDecimal.ZERO) > 0) {
                BigDecimal profit = totalRevenue.subtract(totalCost);
                double margin = profit.divide(totalRevenue, 4, ROUNDING_MODE).doubleValue() * 100;
                analysis.put("profitMargin", margin);
                analysis.put("totalProfit", profit);
            } else {
                analysis.put("profitMargin", 0.0);
                analysis.put("totalProfit", BigDecimal.ZERO);
            }
        }
        
        return productAnalysis;
    }
    
    
    /**
 * Get inventory value for a specific product using current batch costs
 */
public BigDecimal getProductInventoryValue(Long productId) {
    List<Object[]> batchValuations = productBatchRepository.getBatchValuationByProductId(productId);
    
    BigDecimal totalValue = BigDecimal.ZERO;
    for (Object[] batchValuation : batchValuations) {
        BigDecimal batchBuyingPrice = (BigDecimal) batchValuation[0];
        BigDecimal batchQuantity = (BigDecimal) batchValuation[1];
        totalValue = totalValue.add(batchBuyingPrice.multiply(batchQuantity));
    }
    
    return totalValue.setScale(DECIMAL_SCALE, ROUNDING_MODE);
}






/**
     * Get batch data with filtering options
     */
    public List<ProductBatch> getFilteredBatches(Product product, String status, String dateRange) {
        List<ProductBatch> batches;
        
        // Filter by product first
        if (product == null) {
            batches = productBatchRepository.findAll();
        } else {
            batches = productBatchRepository.findByProductId(product.getId());
        }
        
        // Apply date range filter
        LocalDateTime dateThreshold = getDateThresholdForRange(dateRange);
        if (dateThreshold != null) {
            batches = batches.stream()
                    .filter(batch -> batch.getPurchaseDate().isAfter(dateThreshold))
                    .collect(Collectors.toList());
        }
        
        // Apply status filter
        LocalDateTime now = LocalDateTime.now();
        switch (status) {
            case "Active Batches":
                batches = batches.stream()
                        .filter(batch -> batch.getRemainingQuantity().compareTo(BigDecimal.ZERO) > 0)
                        .filter(batch -> batch.getExpiryDate() == null || batch.getExpiryDate().isAfter(now))
                        .collect(Collectors.toList());
                break;
                
            case "Expired Batches":
                batches = batches.stream()
                        .filter(batch -> batch.getExpiryDate() != null && batch.getExpiryDate().isBefore(now))
                        .collect(Collectors.toList());
                break;
                
            case "Low Stock Batches":
                batches = batches.stream()
                        .filter(batch -> {
                            BigDecimal remaining = batch.getRemainingQuantity();
                            BigDecimal initial = batch.getInitialQuantity();
                            BigDecimal threshold = initial.multiply(new BigDecimal("0.2")); // 20% threshold
                            return remaining.compareTo(BigDecimal.ZERO) > 0 && remaining.compareTo(threshold) <= 0;
                        })
                        .collect(Collectors.toList());
                break;
                
            case "Empty Batches":
                batches = batches.stream()
                        .filter(batch -> batch.getRemainingQuantity().compareTo(BigDecimal.ZERO) == 0)
                        .collect(Collectors.toList());
                break;
                
            case "Near Expiry Batches":
                LocalDateTime nearExpiryThreshold = now.plusDays(30); // Next 30 days
                batches = batches.stream()
                        .filter(batch -> batch.getExpiryDate() != null)
                        .filter(batch -> batch.getExpiryDate().isAfter(now) && batch.getExpiryDate().isBefore(nearExpiryThreshold))
                        .filter(batch -> batch.getRemainingQuantity().compareTo(BigDecimal.ZERO) > 0)
                        .collect(Collectors.toList());
                break;
                
            case "All Batches":
            default:
                // No additional filtering
                break;
        }
        
        return batches;
    }
    
    /**
     * Get batch performance metrics
     */
    public Map<ProductBatch, Map<String, Object>> getBatchPerformanceMetrics(List<ProductBatch> batches) {
        Map<ProductBatch, Map<String, Object>> batchMetrics = new HashMap<>();
        
        for (ProductBatch batch : batches) {
            Map<String, Object> metrics = new HashMap<>();
            
            // Calculate utilization percentage
            BigDecimal utilized = batch.getInitialQuantity().subtract(batch.getRemainingQuantity());
            double utilizationPct = 0.0;
            if (batch.getInitialQuantity().compareTo(BigDecimal.ZERO) > 0) {
                utilizationPct = utilized.divide(batch.getInitialQuantity(), 4, ROUNDING_MODE)
                        .multiply(new BigDecimal("100")).doubleValue();
            }
            
            // Calculate days since purchase
            long daysSincePurchase = ChronoUnit.DAYS.between(
                    batch.getPurchaseDate().toLocalDate(), 
                    LocalDate.now()
            );
            
            // Calculate sales velocity (units sold per day)
            double salesVelocity = 0.0;
            if (daysSincePurchase > 0) {
                salesVelocity = utilized.divide(new BigDecimal(daysSincePurchase), 4, ROUNDING_MODE).doubleValue();
            }
            
            // Calculate total investment and current value
            BigDecimal totalInvestment = batch.getBuyingPrice().multiply(batch.getInitialQuantity());
            BigDecimal currentValue = batch.getBuyingPrice().multiply(batch.getRemainingQuantity());
            BigDecimal realizedValue = batch.getBuyingPrice().multiply(utilized);
            
            // Estimate days to complete (if sales velocity > 0)
            double daysToComplete = 0.0;
            if (salesVelocity > 0 && batch.getRemainingQuantity().compareTo(BigDecimal.ZERO) > 0) {
                daysToComplete = batch.getRemainingQuantity().divide(new BigDecimal(salesVelocity), 4, ROUNDING_MODE).doubleValue();
            }
            
            // Check expiry status
            String expiryStatus = "No Expiry";
            if (batch.getExpiryDate() != null) {
                LocalDateTime now = LocalDateTime.now();
                if (batch.getExpiryDate().isBefore(now)) {
                    expiryStatus = "Expired";
                } else {
                    long daysToExpiry = ChronoUnit.DAYS.between(now.toLocalDate(), batch.getExpiryDate().toLocalDate());
                    if (daysToExpiry <= 0) {
                        expiryStatus = "Expired";
                    } else if (daysToExpiry <= 7) {
                        expiryStatus = "Critical";
                    } else if (daysToExpiry <= 30) {
                        expiryStatus = "Warning";
                    } else {
                        expiryStatus = "Good";
                    }
                }
            }
            
            metrics.put("utilized", utilized);
            metrics.put("utilizationPct", utilizationPct);
            metrics.put("daysSincePurchase", daysSincePurchase);
            metrics.put("salesVelocity", salesVelocity);
            metrics.put("totalInvestment", totalInvestment);
            metrics.put("currentValue", currentValue);
            metrics.put("realizedValue", realizedValue);
            metrics.put("daysToComplete", daysToComplete);
            metrics.put("expiryStatus", expiryStatus);
            
            batchMetrics.put(batch, metrics);
        }
        
        return batchMetrics;
    }
    
    /**
     * Get batch sales data (quantities sold from each batch)
     */
    public Map<ProductBatch, BigDecimal> getBatchSalesData(List<ProductBatch> batches, LocalDateTime startDate, LocalDateTime endDate) {
        Map<ProductBatch, BigDecimal> batchSales = new HashMap<>();
        
        for (ProductBatch batch : batches) {
            List<InvoiceItemBatch> salesFromBatch = invoiceItemBatchRepository
                    .findByProductBatchIdAndDateRange(batch.getId(), startDate, endDate);
            
            BigDecimal totalSold = BigDecimal.ZERO;
            for (InvoiceItemBatch sale : salesFromBatch) {
                totalSold = totalSold.add(sale.getQuantity());
            }
            
            batchSales.put(batch, totalSold);
        }
        
        return batchSales;
    }
    
    /**
     * Calculate batch profitability
     */
    public Map<ProductBatch, Map<String, BigDecimal>> getBatchProfitability(List<ProductBatch> batches, LocalDateTime startDate, LocalDateTime endDate) {
        Map<ProductBatch, Map<String, BigDecimal>> batchProfitability = new HashMap<>();
        
        for (ProductBatch batch : batches) {
            Map<String, BigDecimal> profitData = new HashMap<>();
            
            // Get sales from this batch
            List<InvoiceItemBatch> salesFromBatch = invoiceItemBatchRepository
                    .findByProductBatchIdAndDateRange(batch.getId(), startDate, endDate);
            
            BigDecimal totalRevenue = BigDecimal.ZERO;
            BigDecimal totalCost = BigDecimal.ZERO;
            BigDecimal quantitySold = BigDecimal.ZERO;
            
            for (InvoiceItemBatch sale : salesFromBatch) {
                BigDecimal saleRevenue = sale.getUnitPrice().multiply(sale.getQuantity());
                BigDecimal saleCost = sale.getUnitCost().multiply(sale.getQuantity());
                
                totalRevenue = totalRevenue.add(saleRevenue);
                totalCost = totalCost.add(saleCost);
                quantitySold = quantitySold.add(sale.getQuantity());
            }
            
            BigDecimal profit = totalRevenue.subtract(totalCost);
            
            // Calculate margins
            BigDecimal profitMargin = BigDecimal.ZERO;
            if (totalRevenue.compareTo(BigDecimal.ZERO) > 0) {
                profitMargin = profit.divide(totalRevenue, 4, ROUNDING_MODE).multiply(new BigDecimal("100"));
            }
            
            profitData.put("revenue", totalRevenue);
            profitData.put("cost", totalCost);
            profitData.put("profit", profit);
            profitData.put("profitMargin", profitMargin);
            profitData.put("quantitySold", quantitySold);
            
            batchProfitability.put(batch, profitData);
        }
        
        return batchProfitability;
    }
    
    /**
     * Get batch turnover analysis
     */
    public Map<ProductBatch, Double> getBatchTurnoverRates(List<ProductBatch> batches) {
        Map<ProductBatch, Double> turnoverRates = new HashMap<>();
        
        for (ProductBatch batch : batches) {
            // Calculate turnover rate based on days since purchase and quantity sold
            BigDecimal quantitySold = batch.getInitialQuantity().subtract(batch.getRemainingQuantity());
            long daysSincePurchase = ChronoUnit.DAYS.between(
                    batch.getPurchaseDate().toLocalDate(), 
                    LocalDate.now()
            );
            
            double turnoverRate = 0.0;
            if (daysSincePurchase > 0 && batch.getInitialQuantity().compareTo(BigDecimal.ZERO) > 0) {
                // Calculate how many times the batch would turn over in a year
                double dailyTurnover = quantitySold.doubleValue() / daysSincePurchase;
                double yearlyProjection = dailyTurnover * 365;
                turnoverRate = yearlyProjection / batch.getInitialQuantity().doubleValue();
            }
            
            turnoverRates.put(batch, turnoverRate);
        }
        
        return turnoverRates;
    }
    
    /**
     * Get expiry analysis for batches
     */
    public Map<String, Object> getBatchExpiryAnalysis(List<ProductBatch> batches) {
        Map<String, Object> analysis = new HashMap<>();
        
        LocalDateTime now = LocalDateTime.now();
        int expired = 0;
        int critical = 0; // Expires within 7 days
        int warning = 0;  // Expires within 30 days
        int good = 0;
        int noExpiry = 0;
        
        BigDecimal expiredValue = BigDecimal.ZERO;
        BigDecimal criticalValue = BigDecimal.ZERO;
        BigDecimal warningValue = BigDecimal.ZERO;
        
        List<ProductBatch> expiringBatches = new ArrayList<>();
        
        for (ProductBatch batch : batches) {
            BigDecimal batchValue = batch.getBuyingPrice().multiply(batch.getRemainingQuantity());
            
            if (batch.getExpiryDate() == null) {
                noExpiry++;
            } else {
                long daysToExpiry = ChronoUnit.DAYS.between(now.toLocalDate(), batch.getExpiryDate().toLocalDate());
                
                if (daysToExpiry <= 0) {
                    expired++;
                    expiredValue = expiredValue.add(batchValue);
                } else if (daysToExpiry <= 7) {
                    critical++;
                    criticalValue = criticalValue.add(batchValue);
                    expiringBatches.add(batch);
                } else if (daysToExpiry <= 30) {
                    warning++;
                    warningValue = warningValue.add(batchValue);
                    expiringBatches.add(batch);
                } else {
                    good++;
                }
            }
        }
        
        analysis.put("expired", expired);
        analysis.put("critical", critical);
        analysis.put("warning", warning);
        analysis.put("good", good);
        analysis.put("noExpiry", noExpiry);
        analysis.put("expiredValue", expiredValue);
        analysis.put("criticalValue", criticalValue);
        analysis.put("warningValue", warningValue);
        analysis.put("expiringBatches", expiringBatches);
        
        return analysis;
    }
    
    /**
     * Helper method to get date threshold for filtering
     */
    private LocalDateTime getDateThresholdForRange(String dateRange) {
        LocalDateTime now = LocalDateTime.now();
        
        switch (dateRange) {
            case "Last 30 Days":
                return now.minusDays(30);
            case "Last 3 Months":
                return now.minusMonths(3);
            case "Last 6 Months":
                return now.minusMonths(6);
            case "This Year":
                return now.withDayOfYear(1).withHour(0).withMinute(0).withSecond(0);
            case "Last Year":
                return now.minusYears(1).withDayOfYear(1).withHour(0).withMinute(0).withSecond(0);
            case "All Time":
            default:
                return null;
        }
    }





    
    
}