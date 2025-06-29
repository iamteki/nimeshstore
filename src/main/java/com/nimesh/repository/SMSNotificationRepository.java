package com.nimesh.repository;

import com.nimesh.model.SMSNotification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface SMSNotificationRepository extends JpaRepository<SMSNotification, Long> {
    
    List<SMSNotification> findByCustomerId(Long customerId);
    
    List<SMSNotification> findBySentDateBetween(LocalDateTime start, LocalDateTime end);
    
    List<SMSNotification> findByStatus(String status);
    
    List<SMSNotification> findTop20ByOrderBySentDateDesc();
}