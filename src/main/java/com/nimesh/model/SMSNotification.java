package com.nimesh.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "sms_notifications")
public class SMSNotification {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "phone_number", nullable = false)
    private String phoneNumber;
    
    @Column(nullable = false, length = 500)
    private String message;
    
    @Column(name = "sent_date", nullable = false)
    private LocalDateTime sentDate;
    
    @Column(nullable = false)
    private String status;
    
    @Column(name = "external_id")
    private String externalId;
    
    @Column(name = "error_message", length = 500)
    private String errorMessage;
    
    @ManyToOne
    @JoinColumn(name = "customer_id")
    private Customer customer;
    
    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public LocalDateTime getSentDate() {
        return sentDate;
    }

    public void setSentDate(LocalDateTime sentDate) {
        this.sentDate = sentDate;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getExternalId() {
        return externalId;
    }

    public void setExternalId(String externalId) {
        this.externalId = externalId;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public Customer getCustomer() {
        return customer;
    }

    public void setCustomer(Customer customer) {
        this.customer = customer;
    }
}