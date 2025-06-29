// ActivityLogRepository.java
package com.nimesh.repository;

import com.nimesh.model.ActivityLog;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ActivityLogRepository extends JpaRepository<ActivityLog, Long> {
    
    List<ActivityLog> findByTimestampBetween(LocalDateTime start, LocalDateTime end);
    
    List<ActivityLog> findByActivityType(String activityType);
    
    List<ActivityLog> findByOrderByTimestampDesc(Pageable pageable);
}