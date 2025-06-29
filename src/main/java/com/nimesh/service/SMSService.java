package com.nimesh.service;

import com.nimesh.model.CreditAccount;
import com.nimesh.model.Customer;
import com.nimesh.model.SMSNotification;
import com.nimesh.repository.SMSNotificationRepository;
import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class SMSService {
    
    @Value("${twilio.account.sid}")
    private String ACCOUNT_SID;
    
    @Value("${twilio.auth.token}")
    private String AUTH_TOKEN;
    
    @Value("${twilio.phone.number}")
    private String FROM_NUMBER;
    
    @Value("${sms.enabled}")
    private boolean smsEnabled;
    
    @Autowired
    private CustomerService customerService;
    
    @Autowired
    private SMSNotificationRepository notificationRepository;
    
    /**
     * Sends an SMS to a customer
     */
    public boolean sendSMS(String phoneNumber, String message) {
        if (!smsEnabled) {
            // Log the SMS instead of sending it (for development/testing)
            System.out.println("SMS to " + phoneNumber + ": " + message);
            
            // Record the notification
            SMSNotification notification = new SMSNotification();
            notification.setPhoneNumber(phoneNumber);
            notification.setMessage(message);
            notification.setSentDate(LocalDateTime.now());
            notification.setStatus("SIMULATED");
            notificationRepository.save(notification);
            
            return true;
        }
        
        try {
            // Initialize Twilio
            Twilio.init(ACCOUNT_SID, AUTH_TOKEN);
            
            // Format phone number for Twilio (needs +country code)
            String formattedNumber = formatPhoneNumber(phoneNumber);
            
            // Send message
            Message twilioMessage = Message.creator(
                    new PhoneNumber(formattedNumber),
                    new PhoneNumber(FROM_NUMBER),
                    message)
                .create();
            
            // Record the notification
            SMSNotification notification = new SMSNotification();
            notification.setPhoneNumber(phoneNumber);
            notification.setMessage(message);
            notification.setSentDate(LocalDateTime.now());
            notification.setStatus(twilioMessage.getStatus().toString());
            notification.setExternalId(twilioMessage.getSid());
            notificationRepository.save(notification);
            
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            
            // Record failed notification
            SMSNotification notification = new SMSNotification();
            notification.setPhoneNumber(phoneNumber);
            notification.setMessage(message);
            notification.setSentDate(LocalDateTime.now());
            notification.setStatus("FAILED");
            notification.setErrorMessage(e.getMessage());
            notificationRepository.save(notification);
            
            return false;
        }
    }
    
    /**
     * Sends a credit reminder to a customer
     */
    public boolean sendCreditReminder(Customer customer) {
        if (customer == null || customer.getContactNo() == null || customer.getContactNo().isEmpty()) {
            return false;
        }
        
        CreditAccount account = customerService.getCreditAccount(customer.getId());
        if (account == null || account.getBalance().compareTo(BigDecimal.ZERO) <= 0) {
            return false;
        }
        
        String message = "Dear " + customer.getName() + 
                ", your outstanding balance at Nimesh Store is LKR " + 
                account.getBalance() + ". Please make a payment at your earliest convenience.";
        
        return sendSMS(customer.getContactNo(), message);
    }
    
    /**
     * Sends reminders to all customers with outstanding balances
     */
    public int sendAllOutstandingReminders() {
        List<CreditAccount> outstandingAccounts = customerService.getCustomersWithOutstandingCredit();
        int successCount = 0;
        
        for (CreditAccount account : outstandingAccounts) {
            Customer customer = account.getCustomer();
            if (customer != null && customer.getContactNo() != null && !customer.getContactNo().isEmpty()) {
                boolean success = sendCreditReminder(customer);
                if (success) {
                    successCount++;
                }
            }
        }
        
        return successCount;
    }
    
    /**
     * Sends reminders to customers approaching their credit limit
     */
    public int sendCreditLimitWarnings(int percentThreshold) {
        List<CreditAccount> approachingLimitAccounts = 
                customerService.getCustomersApproachingCreditLimit(percentThreshold);
        int successCount = 0;
        
        for (CreditAccount account : approachingLimitAccounts) {
            Customer customer = account.getCustomer();
            if (customer != null && customer.getContactNo() != null && !customer.getContactNo().isEmpty()) {
                // Calculate percentage used
                BigDecimal percentUsed = account.getBalance()
                        .multiply(new BigDecimal("100"))
                        .divide(account.getCreditLimit(), 0, BigDecimal.ROUND_HALF_UP);
                
                String message = "Dear " + customer.getName() + 
                        ", you have used " + percentUsed + "% of your credit limit at Nimesh Store. " +
                        "Your current balance is LKR " + account.getBalance() + 
                        " of your LKR " + account.getCreditLimit() + " limit.";
                
                boolean success = sendSMS(customer.getContactNo(), message);
                if (success) {
                    successCount++;
                }
            }
        }
        
        return successCount;
    }
    
    /**
     * Scheduled job to send reminders to customers with outstanding balances
     * Runs every Monday at 9:00 AM
     */
    @Scheduled(cron = "0 0 9 * * MON")
    public void scheduledOutstandingReminders() {
        sendAllOutstandingReminders();
    }
    
    /**
     * Scheduled job to send warnings to customers approaching their credit limit
     * Runs every Friday at 2:00 PM
     */
    @Scheduled(cron = "0 0 14 * * FRI")
    public void scheduledCreditLimitWarnings() {
        sendCreditLimitWarnings(80);
    }
    
    /**
     * Formats a phone number for Twilio (adds country code if needed)
     */
    private String formatPhoneNumber(String phoneNumber) {
        // Remove any non-digit characters
        String digitsOnly = phoneNumber.replaceAll("\\D", "");
        
        // If it starts with 0, replace with country code
        if (digitsOnly.startsWith("0")) {
            return "+94" + digitsOnly.substring(1); // Sri Lanka country code
        }
        
        // If it doesn't start with +, add it
        if (!digitsOnly.startsWith("+")) {
            return "+" + digitsOnly;
        }
        
        return phoneNumber;
    }
}