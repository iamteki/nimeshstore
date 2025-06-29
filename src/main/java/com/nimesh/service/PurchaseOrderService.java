package com.nimesh.service;

import com.nimesh.model.PurchaseOrder;
import com.nimesh.model.OrderItem;
import com.nimesh.model.Product;
import com.nimesh.repository.PurchaseOrderRepository;
import com.nimesh.repository.OrderItemRepository;
import com.nimesh.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

@Service
public class PurchaseOrderService {
    
    @Autowired
    private PurchaseOrderRepository purchaseOrderRepository;
    
    @Autowired
    private OrderItemRepository orderItemRepository;
    
    @Autowired
    private ProductRepository productRepository;
    
    @Autowired
    private ProductBatchService productBatchService;
    
     @Autowired
    private ActivityLogService activityLogService;
    
    /**
     * Creates a new purchase order
     */
    @Transactional
    public PurchaseOrder createPurchaseOrder(PurchaseOrder order) {
        // Generate order number if not provided
        if (order.getOrderNumber() == null || order.getOrderNumber().isEmpty()) {
            order.setOrderNumber(generateOrderNumber());
        }
        
        // Set order date if not set
        if (order.getOrderDate() == null) {
            order.setOrderDate(LocalDateTime.now());
        }
        
        // Set initial status if not set
        if (order.getStatus() == null || order.getStatus().isEmpty()) {
            order.setStatus("PENDING");
        }
        
        // Calculate total amount
        order.calculateTotal();
        
        // Save the order
        return purchaseOrderRepository.save(order);
    }
    
    /**
     * Retrieves a purchase order by ID
     */
    public Optional<PurchaseOrder> getPurchaseOrderById(Long id) {
        return purchaseOrderRepository.findById(id);
    }
    
    /**
     * Retrieves a purchase order by order number
     */
    public PurchaseOrder getPurchaseOrderByNumber(String orderNumber) {
        return purchaseOrderRepository.findByOrderNumber(orderNumber);
    }
    
    /**
     * Retrieves all purchase orders
     */
    public List<PurchaseOrder> getAllPurchaseOrders() {
        return purchaseOrderRepository.findAll();
    }
    
    /**
     * Retrieves purchase orders by supplier
     */
    public List<PurchaseOrder> getPurchaseOrdersBySupplier(Long supplierId) {
        return purchaseOrderRepository.findBySupplierId(supplierId);
    }
    
    /**
     * Retrieves purchase orders by status
     */
    public List<PurchaseOrder> getPurchaseOrdersByStatus(String status) {
        return purchaseOrderRepository.findByStatus(status);
    }
    
   /**
     * Updates a purchase order status and creates product batches when items are received
     */
    @Transactional
    public PurchaseOrder updateOrderStatus(Long orderId, String newStatus) {
        PurchaseOrder order = purchaseOrderRepository.findById(orderId)
            .orElseThrow(() -> new IllegalArgumentException("Order not found with ID: " + orderId));
        
        String oldStatus = order.getStatus();
        order.setStatus(newStatus);
        
        // If the status is changing to RECEIVED, create product batches
        if ("RECEIVED".equals(newStatus) && !"RECEIVED".equals(oldStatus)) {
            for (OrderItem item : order.getItems()) {
                Product product = item.getProduct();
                
                // Create a new batch for this received product
                productBatchService.createBatch(
                    product.getId(),
                    item.getQuantity(),
                    item.getUnitPrice(),
                    product.getSellingPrice(), // Use current selling price as default
                    product.getWholesalePrice(), // Use current wholesale price as default
                    order, 
                    null // No expiry date by default
                );
                
                // Note: The stock is already updated in the createBatch method
            }
            
            // Set delivery date when items are received
            order.setDeliveryDate(LocalDateTime.now());
        }
        
        // Log activity
        String activityDescription = "Purchase order #" + order.getOrderNumber() + 
                " status updated from " + oldStatus + " to " + newStatus;
        activityLogService.logActivity(activityDescription, "PURCHASE", null, order.getId(), "PurchaseOrder");
        
        return purchaseOrderRepository.save(order);
    }
    
    /**
     * Process order reception - update inventory when order is received
     */
  @Transactional
public void receiveOrder(PurchaseOrder order) {
    // Update stock levels for all items in the order
    for (OrderItem item : order.getItems()) {
        Product product = item.getProduct();
        BigDecimal currentStock = product.getCurrentStock();
        BigDecimal newQuantity = item.getQuantity();
        
        product.setCurrentStock(currentStock.add(newQuantity));
        product.setLastUpdated(LocalDateTime.now());
        
        productRepository.save(product);
    }
}
    
    /**
     * Cancels a purchase order
     */
    @Transactional
    public boolean cancelOrder(Long orderId) {
        Optional<PurchaseOrder> optionalOrder = purchaseOrderRepository.findById(orderId);
        
        if (optionalOrder.isPresent()) {
            PurchaseOrder order = optionalOrder.get();
            
            // Only allow cancellation of PENDING or PROCESSING orders
            if ("PENDING".equals(order.getStatus()) || "PROCESSING".equals(order.getStatus())) {
                order.setStatus("CANCELLED");
                purchaseOrderRepository.save(order);
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * Adds an item to a purchase order
     */
    @Transactional
    public OrderItem addOrderItem(Long orderId, OrderItem item) {
        Optional<PurchaseOrder> optionalOrder = purchaseOrderRepository.findById(orderId);
        
        if (optionalOrder.isPresent()) {
            PurchaseOrder order = optionalOrder.get();
            
            // Only allow adding items to PENDING orders
            if (!"PENDING".equals(order.getStatus())) {
                return null;
            }
            
            order.addItem(item);
            purchaseOrderRepository.save(order);
            return item;
        }
        
        return null;
    }
    
    /**
     * Removes an item from a purchase order
     */
    @Transactional
    public boolean removeOrderItem(Long orderId, Long itemId) {
        Optional<PurchaseOrder> optionalOrder = purchaseOrderRepository.findById(orderId);
        Optional<OrderItem> optionalItem = orderItemRepository.findById(itemId);
        
        if (optionalOrder.isPresent() && optionalItem.isPresent()) {
            PurchaseOrder order = optionalOrder.get();
            OrderItem item = optionalItem.get();
            
            // Only allow removing items from PENDING orders
            if (!"PENDING".equals(order.getStatus())) {
                return false;
            }
            
            order.removeItem(item);
            orderItemRepository.delete(item);
            purchaseOrderRepository.save(order);
            return true;
        }
        
        return false;
    }
    
    /**
     * Generates a new order number
     */
    private String generateOrderNumber() {
        // Format: PO-YYYYMMDD-XXXX where XXXX is a sequence number
        LocalDate today = LocalDate.now();
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyyMMdd");
        String dateStr = today.format(dateFormatter);
        
        // Get the max ID and add 1, or start at 1 if no orders exist
        Long maxId = purchaseOrderRepository.findMaxId();
        long nextNumber = (maxId == null) ? 1 : maxId + 1;
        String sequenceStr = String.format("%04d", nextNumber);
        
        return "PO-" + dateStr + "-" + sequenceStr;
    }
    
    /**
     * Gets orders for a specific date range
     */
    public List<PurchaseOrder> getOrdersForDateRange(LocalDateTime start, LocalDateTime end) {
        return purchaseOrderRepository.findByOrderDateBetween(start, end);
    }
    
    /**
     * Gets total purchase amount for a specific period
     */
    public BigDecimal getTotalPurchasesForPeriod(LocalDateTime start, LocalDateTime end) {
        List<PurchaseOrder> orders = getOrdersForDateRange(start, end);
        
        BigDecimal total = BigDecimal.ZERO;
        for (PurchaseOrder order : orders) {
            // Only include orders that are not cancelled
            if (!"CANCELLED".equals(order.getStatus())) {
                total = total.add(order.getTotalAmount());
            }
        }
        
        return total;
    }
    
    /**
     * Gets today's total purchases
     */
    public BigDecimal getTodaysPurchases() {
        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        LocalDateTime endOfDay = LocalDate.now().atTime(LocalTime.MAX);
        
        return getTotalPurchasesForPeriod(startOfDay, endOfDay);
    }
}