package com.nimesh.service;

import com.nimesh.model.Customer;
import com.nimesh.model.CreditAccount;
import com.nimesh.model.Invoice;
import com.nimesh.model.InvoiceItem;
import com.nimesh.model.InvoiceItemBatch;
import com.nimesh.model.Product;
import com.nimesh.model.ProductBatch;
import com.nimesh.repository.CreditAccountRepository;
import com.nimesh.repository.InvoiceItemBatchRepository;
import com.nimesh.repository.InvoiceRepository;
import com.nimesh.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import jakarta.persistence.EntityManager;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

@Service
public class InvoiceService {
    
    @Autowired
    private InvoiceRepository invoiceRepository;
    
    @Autowired
    private ProductRepository productRepository;
    
    @Autowired
    private CreditAccountRepository creditAccountRepository;
    
    @Autowired
    private ActivityLogService activityLogService;
    
    @Autowired
    private EntityManager entityManager; 
    
     @Autowired
    private ProductBatchService productBatchService;
    
    @Autowired
    private InvoiceItemBatchRepository invoiceItemBatchRepository;
    
    // Add this to your InvoiceService class if it's not already there

@Autowired
private SystemConfigService configService;
    

    
/**
 * Saves an invoice and updates related data like inventory and credit account
 * with strategy-based batch tracking for products
 */
@Transactional
public Invoice saveInvoice(Invoice invoice) {
    // Generate invoice number if not provided
    if (invoice.getInvoiceNumber() == null || invoice.getInvoiceNumber().isEmpty()) {
        invoice.setInvoiceNumber(generateInvoiceNumber());
    }
    
    // Get the pricing strategy
    String pricingStrategy = configService.getPricingStrategy();
    boolean isWholesale = "WHOLESALE".equals(invoice.getCustomerType());
    
    // Set buying prices for all items BEFORE saving the invoice
    // NOTE: We DON'T override the selling prices here because they might have been manually edited in POS
    for (InvoiceItem item : invoice.getItems()) {
        Product product = item.getProduct();
        
        // Set the buying price based on the pricing strategy
        BigDecimal strategyBuyingPrice = productBatchService.getStrategyBasedBuyingPrice(product.getId());
        item.setBuyingPrice(strategyBuyingPrice);
        
        // DO NOT override the selling price here - keep whatever was set in the POS
        // The selling price (unitPrice) should already be correctly set from the CartItem
        
        // Recalculate total for this item (in case buying price affects anything)
        item.setTotal(item.getUnitPrice().multiply(item.getQuantity()).subtract(
            item.getDiscountAmount() != null ? item.getDiscountAmount() : BigDecimal.ZERO));
    }
    
    // Calculate invoice totals
    invoice.calculateTotals();
    
    // Save the invoice with the prices as they are (including manually edited ones)
    invoice = invoiceRepository.save(invoice);
    
    // Now handle batch tracking using strategy-based reduction
    for (InvoiceItem item : invoice.getItems()) {
        Product product = item.getProduct();
        BigDecimal quantitySold = item.getQuantity();
        
        // Use strategy-based method to reduce stock from batches
        List<ProductBatchService.BatchUsage> batchUsages = 
            productBatchService.reduceBatchQuantitiesStrategy(product.getId(), quantitySold);
        
        // Save batch usage information for this invoice item
        for (ProductBatchService.BatchUsage usage : batchUsages) {
            ProductBatch batch = usage.getBatch();
            BigDecimal quantityFromBatch = usage.getQuantityUsed();
            
            // Create the invoice item batch record
            // Always use the actual selling price from the invoice item to maintain accuracy
            InvoiceItemBatch invoiceItemBatch = new InvoiceItemBatch(
                item, 
                batch, 
                quantityFromBatch, 
                item.getUnitPrice() // Use the actual selling price from the invoice item
            );
            
            // Verify that unit cost is set correctly
            if (invoiceItemBatch.getUnitCost() == null) {
                invoiceItemBatch.setUnitCost(batch.getBuyingPrice());
            }
            
            invoiceItemBatchRepository.save(invoiceItemBatch);
        }
        
        // Reduce the overall product stock
        product.setCurrentStock(product.getCurrentStock().subtract(quantitySold));
        productRepository.save(product);
    }
    
    // Handle credit payment
    if ("CREDIT".equals(invoice.getPaymentMethod())) {
        invoice.setPaymentStatus("PENDING");
        
        // Update customer credit account
        if (invoice.getCustomer() != null) {
            CreditAccount creditAccount = creditAccountRepository.findByCustomerId(invoice.getCustomer().getId());
            if (creditAccount == null) {
                creditAccount = new CreditAccount(invoice.getCustomer(), null);
                creditAccount.setCustomer(invoice.getCustomer());
            }
            
            creditAccount.addToBalance(invoice.getFinalAmount());
            creditAccountRepository.save(creditAccount);
        }
    }
    
    // Log activity
    String activityDescription = "New invoice #" + invoice.getInvoiceNumber() + 
            " created for " + (invoice.getCustomer() != null ? invoice.getCustomer().getName() : "walk-in customer") + 
            " - ₹" + invoice.getFinalAmount();
    activityLogService.logActivity(activityDescription, "INVOICE", null, invoice.getId(), "Invoice");
    
    // Return the saved invoice
    return invoice;
}
    
  /**
 * Generates a new invoice number based on the current date and sequence
 * This is now public so it can be accessed from the POSController
 */
public String generateInvoiceNumber() {
    // Format: INV-YYYYMMDD-XXXX where XXXX is a sequence number
    LocalDate today = LocalDate.now();
    DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyyMMdd");
    String dateStr = today.format(dateFormatter);
    
    // Get invoice count for today
    LocalDateTime startOfDay = today.atStartOfDay();
    LocalDateTime endOfDay = today.atTime(LocalTime.MAX);
    Long invoiceCount = invoiceRepository.countInvoicesForDate(startOfDay, endOfDay);
    
    // Add 1 to get the next number and format with leading zeros
    long nextNumber = (invoiceCount == null) ? 1 : invoiceCount + 1;
    String sequenceStr = String.format("%04d", nextNumber);
    
    return "INV-" + dateStr + "-" + sequenceStr;
}
    
    /**
     * Retrieves an invoice by its ID
     */
    public Optional<Invoice> getInvoiceById(Long id) {
        return invoiceRepository.findById(id);
    }
    
    /**
     * Retrieves an invoice by its invoice number
     */
    public Invoice getInvoiceByNumber(String invoiceNumber) {
        return invoiceRepository.findByInvoiceNumber(invoiceNumber);
    }
    
    /**
     * Retrieves all invoices
     */
    public List<Invoice> getAllInvoices() {
        return invoiceRepository.findAll();
    }
    
    /**
     * Retrieves invoices for a specific date
     */
    public List<Invoice> getInvoicesForDate(LocalDateTime date) {
        return invoiceRepository.findByDate(date);
    }
    
    /**
     * Retrieves invoices between two dates
     */
    public List<Invoice> getInvoicesForDateRange(LocalDateTime start, LocalDateTime end) {
        return invoiceRepository.findByDateBetween(start, end);
    }
    
    /**
     * Retrieves invoices for a specific customer
     */
    public List<Invoice> getInvoicesForCustomer(Long customerId) {
    // Use a join fetch to eagerly load items with the invoice
    return entityManager
        .createQuery("SELECT i FROM Invoice i LEFT JOIN FETCH i.items WHERE i.customer.id = :customerId", Invoice.class)
        .setParameter("customerId", customerId)
        .getResultList();
}
    
    /**
     * Retrieves invoices by payment method
     */
    public List<Invoice> getInvoicesByPaymentMethod(String paymentMethod) {
        return invoiceRepository.findByPaymentMethod(paymentMethod);
    }
    
    /**
     * Retrieves invoices by payment status
     */
    public List<Invoice> getInvoicesByPaymentStatus(String paymentStatus) {
        return invoiceRepository.findByPaymentStatus(paymentStatus);
    }
    
    /**
     * Retrieves invoices by customer type (retail or wholesale)
     */
    public List<Invoice> getInvoicesByCustomerType(String customerType) {
        return invoiceRepository.findByCustomerType(customerType);
    }
    
    /**
     * Gets today's total sales amount
     */
    public BigDecimal getTodaysSales() {
        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        LocalDateTime endOfDay = LocalDate.now().atTime(LocalTime.MAX);
        
        List<Invoice> todaysInvoices = invoiceRepository.findByDateBetween(startOfDay, endOfDay);
        
        BigDecimal totalSales = BigDecimal.ZERO;
        for (Invoice invoice : todaysInvoices) {
            totalSales = totalSales.add(invoice.getFinalAmount());
        }
        
        return totalSales;
    }
    
    /**
     * Gets today's invoice count
     */
    public long getTodaysInvoiceCount() {
        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        LocalDateTime endOfDay = LocalDate.now().atTime(LocalTime.MAX);
        
        Long count = invoiceRepository.countInvoicesForDate(startOfDay, endOfDay);
        return count == null ? 0 : count;
    }
    
    
    /**
 * Gets sales total for a specific date
 */
public BigDecimal getSalesForDate(LocalDate date) {
    LocalDateTime startOfDay = date.atStartOfDay();
    LocalDateTime endOfDay = date.atTime(23, 59, 59);
    
    List<Invoice> dateInvoices = invoiceRepository.findByDateBetween(startOfDay, endOfDay);
    
    BigDecimal totalSales = BigDecimal.ZERO;
    for (Invoice invoice : dateInvoices) {
        totalSales = totalSales.add(invoice.getFinalAmount());
    }
    
    return totalSales;
}

/**
 * Gets sales total for a date range
 */
public BigDecimal getSalesForDateRange(LocalDateTime start, LocalDateTime end) {
    List<Invoice> rangeInvoices = invoiceRepository.findByDateBetween(start, end);
    
    BigDecimal totalSales = BigDecimal.ZERO;
    for (Invoice invoice : rangeInvoices) {
        totalSales = totalSales.add(invoice.getFinalAmount());
    }
    
    return totalSales;
}
    

/**
 * Gets a list of recent invoices, limited by count
 */
public List<Invoice> getRecentInvoices(int count) {
    return invoiceRepository.findTop5ByOrderByDateDesc();
}


/**
 * Gets sales for a specific customer in the given date range
 */
public BigDecimal getCustomerSalesForDateRange(Long customerId, LocalDateTime start, LocalDateTime end) {
    List<Invoice> invoices = invoiceRepository.findByCustomerIdAndDateBetween(customerId, start, end);
    
    BigDecimal totalSales = BigDecimal.ZERO;
    for (Invoice invoice : invoices) {
        totalSales = totalSales.add(invoice.getFinalAmount());
    }
    
    return totalSales;
}

/**
 * Retrieves invoices for a specific customer and date range
 */
public List<Invoice> getInvoicesForCustomerAndDateRange(Long customerId, LocalDateTime start, LocalDateTime end) {
    return invoiceRepository.findByCustomerIdAndDateBetween(customerId, start, end);
}




    
}