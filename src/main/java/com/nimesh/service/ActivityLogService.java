// ActivityLogService.java
package com.nimesh.service;

import com.nimesh.model.ActivityLog;
import com.nimesh.repository.ActivityLogRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ActivityLogService {
    
    @Autowired
    private ActivityLogRepository activityLogRepository;
    
   /**
 * Creates a new activity log entry
 */
public ActivityLog logActivity(String description, String activityType) {
    try {
        ActivityLog log = new ActivityLog(description, activityType);
        return activityLogRepository.save(log);
    } catch (Exception e) {
        // Log error but don't block application flow
        System.err.println("Failed to log activity: " + e.getMessage());
        return null;
    }
}
    
  /**
 * Creates a new activity log entry with reference details
 */
public ActivityLog logActivity(String description, String activityType, 
                                Long userId, Long referenceId, String referenceType) {
    try {
        ActivityLog log = new ActivityLog(description, activityType);
        log.setUserId(userId);
        log.setReferenceId(referenceId);
        log.setReferenceType(referenceType);
        return activityLogRepository.save(log);
    } catch (Exception e) {
        // Log error but don't block application flow
        System.err.println("Failed to log activity: " + e.getMessage());
        return null;
    }
}
    
    /**
     * Gets recent activity logs, limited by count
     */
    public List<ActivityLog> getRecentActivities(int count) {
        return activityLogRepository.findByOrderByTimestampDesc(PageRequest.of(0, count));
    }
    
    /**
     * Gets activity logs for a specific date range
     */
    public List<ActivityLog> getActivitiesBetween(LocalDateTime start, LocalDateTime end) {
        return activityLogRepository.findByTimestampBetween(start, end);
    }
    
    /**
     * Gets activity logs for a specific type
     */
    public List<ActivityLog> getActivitiesByType(String activityType) {
        return activityLogRepository.findByActivityType(activityType);
    }
}