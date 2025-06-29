package com.nimesh.service;

import com.nimesh.model.Customer;
import com.nimesh.model.CreditAccount;
import com.nimesh.repository.CustomerRepository;
import com.nimesh.repository.CreditAccountRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class CustomerService {
    
    @Autowired
    private CustomerRepository customerRepository;
    
    @Autowired
    private CreditAccountRepository creditAccountRepository;
    
    @Autowired
    private ActivityLogService activityLogService;
    
    /**
     * Creates a new customer with optional credit account
     */
    @Transactional
    public Customer createCustomer(Customer customer, BigDecimal creditLimit) {
        Customer savedCustomer = customerRepository.save(customer);
        
        // Create credit account if this is a credit customer
        if (creditLimit != null) {
            CreditAccount creditAccount = new CreditAccount(savedCustomer, creditLimit);
            creditAccountRepository.save(creditAccount);
        }
        
        // Log activity
    String creditInfo = creditLimit != null ? " with LKR" + creditLimit + " credit limit" : "";
    String activityDescription = "New " + customer.getCustomerType().toLowerCase() + 
            " customer registered: " + customer.getName() + creditInfo;
    activityLogService.logActivity(activityDescription, "CUSTOMER", null, savedCustomer.getId(), "Customer");
        
        
        
        return savedCustomer;
    }
    
    /**
     * Updates an existing customer
     */
    public Customer updateCustomer(Customer customer) {
        return customerRepository.save(customer);
    }
    
    /**
     * Retrieves a customer by ID
     */
    public Optional<Customer> getCustomerById(Long id) {
        return customerRepository.findById(id);
    }
    
    /**
     * Retrieves all customers
     */
    public List<Customer> getAllCustomers() {
        return customerRepository.findAll();
    }
    
    /**
     * Retrieves customers by type (retail or wholesale)
     */
    public List<Customer> getCustomersByType(String customerType) {
        return customerRepository.findByCustomerType(customerType);
    }
    
    /**
     * Searches customers by name
     */
    public List<Customer> searchCustomersByName(String name) {
        return customerRepository.findByNameContainingIgnoreCase(name);
    }
    
    /**
     * Checks if a customer has a credit account
     */
    public boolean hasCreditAccount(Long customerId) {
        CreditAccount creditAccount = creditAccountRepository.findByCustomerId(customerId);
        return creditAccount != null;
    }
    
    /**
     * Gets a customer's credit account
     */
    public CreditAccount getCreditAccount(Long customerId) {
        return creditAccountRepository.findByCustomerId(customerId);
    }
    
    /**
     * Updates a customer's credit limit
     */
    @Transactional
    public CreditAccount updateCreditLimit(Long customerId, BigDecimal newCreditLimit) {
        CreditAccount creditAccount = creditAccountRepository.findByCustomerId(customerId);
        
        if (creditAccount == null) {
            Optional<Customer> customer = customerRepository.findById(customerId);
            if (customer.isPresent()) {
                creditAccount = new CreditAccount(customer.get(), newCreditLimit);
            } else {
                return null; // Customer not found
            }
        } else {
            creditAccount.setCreditLimit(newCreditLimit);
        }
        
        return creditAccountRepository.save(creditAccount);
    }
    
    /**
     * Applies a payment to a customer's credit account
     */
    @Transactional
    public CreditAccount applyCreditPayment(Long customerId, BigDecimal paymentAmount) {
        CreditAccount creditAccount = creditAccountRepository.findByCustomerId(customerId);
        
        if (creditAccount != null && paymentAmount.compareTo(BigDecimal.ZERO) > 0) {
            creditAccount.subtractFromBalance(paymentAmount);
            return creditAccountRepository.save(creditAccount);
        }
        
        return null; // Credit account not found or invalid payment amount
    }
    
    /**
     * Gets all customers with outstanding credit balances
     */
    public List<CreditAccount> getCustomersWithOutstandingCredit() {
        return creditAccountRepository.findAllWithOutstandingBalance();
    }
    
    /**
     * Gets total outstanding credit across all customers
     */
    public BigDecimal getTotalOutstandingCredit() {
        BigDecimal total = creditAccountRepository.getTotalOutstandingCredit();
        return total != null ? total : BigDecimal.ZERO;
    }
    
    /**
     * Initializes the database with some default customers
     */
    @Transactional
    public void initializeDefaultCustomers() {
        if (customerRepository.count() == 0) {
            // Create a default retail customer
            Customer walkInRetail = new Customer("Walk-in Customer", "", "", "RETAIL");
            customerRepository.save(walkInRetail);
            
            // Create some sample customers
            Customer retailCustomer = new Customer("John Smith", "9876543210", "123 Main St", "RETAIL");
            Customer wholesaleCustomer = new Customer("ABC Trading Co.", "8765432109", "456 Business Ave", "WHOLESALE");
            
            customerRepository.save(retailCustomer);
            customerRepository.save(wholesaleCustomer);
            
            // Create a credit account for the wholesale customer
            CreditAccount creditAccount = new CreditAccount(wholesaleCustomer, new BigDecimal("50000"));
            creditAccountRepository.save(creditAccount);
        }
    }
    
    /**
     * Deletes a customer by ID
     */
    @Transactional
    public boolean deleteCustomer(Long customerId) {
        if (customerRepository.existsById(customerId)) {
            // Delete the customer's credit account first
            CreditAccount creditAccount = creditAccountRepository.findByCustomerId(customerId);
            if (creditAccount != null) {
                creditAccountRepository.delete(creditAccount);
            }
            
            // Then delete the customer
            customerRepository.deleteById(customerId);
            return true;
        }
        return false;
    }
    
    
    /**
 * Gets all credit accounts regardless of balance
 */
public List<CreditAccount> getAllCreditAccounts() {
    return creditAccountRepository.findAll();
}

/**
 * Gets customers approaching their credit limit
 * @param percentThreshold Percentage threshold (e.g., 70 means customers using 70% or more of their limit)
 */
public List<CreditAccount> getCustomersApproachingCreditLimit(int percentThreshold) {
    List<CreditAccount> allAccounts = creditAccountRepository.findAll();
    List<CreditAccount> approachingLimit = new ArrayList<>();
    
    for (CreditAccount account : allAccounts) {
        // Skip accounts with no limit or zero balance
        if (account.getCreditLimit() == null || 
                account.getCreditLimit().compareTo(BigDecimal.ZERO) <= 0 ||
                account.getBalance().compareTo(BigDecimal.ZERO) <= 0) {
            continue;
        }
        
        // Calculate percentage of limit used
        BigDecimal limitUsedPercent = account.getBalance()
                .multiply(new BigDecimal("100"))
                .divide(account.getCreditLimit(), 0, BigDecimal.ROUND_HALF_UP);
        
        if (limitUsedPercent.intValue() >= percentThreshold) {
            approachingLimit.add(account);
        }
    }
    
    return approachingLimit;
}

/**
 * Gets customers who have exceeded their credit limit
 */
public List<CreditAccount> getCustomersExceedingCreditLimit() {
    List<CreditAccount> allAccounts = creditAccountRepository.findAll();
    List<CreditAccount> exceedingLimit = new ArrayList<>();
    
    for (CreditAccount account : allAccounts) {
        // Skip accounts with no limit
        if (account.getCreditLimit() == null) {
            continue;
        }
        
        // Check if balance exceeds limit
        if (account.getBalance().compareTo(account.getCreditLimit()) > 0) {
            exceedingLimit.add(account);
        }
    }
    
    return exceedingLimit;
}

/**
 * Finds a customer by contact number
 * 
 * @param contactNo the contact number to search for
 * @return the Customer if found, null otherwise
 */
public Customer findCustomerByContactNo(String contactNo) {
    if (contactNo == null || contactNo.trim().isEmpty()) {
        return null;
    }
    
    // Your repository already has this method returning a single Customer
    return customerRepository.findByContactNo(contactNo);
}  
    
    
}