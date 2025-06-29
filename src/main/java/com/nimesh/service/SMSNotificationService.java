package com.nimesh.service;

import com.nimesh.model.SMSNotification;
import com.nimesh.repository.SMSNotificationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class SMSNotificationService {
    
    @Autowired
    private SMSNotificationRepository notificationRepository;
    
    public List<SMSNotification> getRecentNotifications() {
        return notificationRepository.findTop20ByOrderBySentDateDesc();
    }
    
    public List<SMSNotification> getNotificationsForCustomer(Long customerId) {
        return notificationRepository.findByCustomerId(customerId);
    }
    
    public List<SMSNotification> getNotificationsBetween(LocalDateTime start, LocalDateTime end) {
        return notificationRepository.findBySentDateBetween(start, end);
    }
    
    public List<SMSNotification> getNotificationsByStatus(String status) {
        return notificationRepository.findByStatus(status);
    }
    
    public SMSNotification saveNotification(SMSNotification notification) {
        return notificationRepository.save(notification);
    }
}