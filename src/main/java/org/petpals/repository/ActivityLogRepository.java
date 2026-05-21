package org.petpals.repository;

import org.petpals.model.ActivityLog;
import org.petpals.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ActivityLogRepository extends JpaRepository<ActivityLog, Long> {
    
    Page<ActivityLog> findByUserId(Long userId, Pageable pageable);
    
    Page<ActivityLog> findAll(Pageable pageable);
    
    List<ActivityLog> findByAction(String action);
    
    @Query("SELECT al FROM ActivityLog al WHERE al.timestamp BETWEEN :start AND :end ORDER BY al.timestamp DESC")
    List<ActivityLog> findByDateRange(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);
    
    @Query("SELECT al FROM ActivityLog al WHERE al.user.id = :userId AND al.timestamp BETWEEN :start AND :end")
    List<ActivityLog> findUserActivityInRange(@Param("userId") Long userId, @Param("start") LocalDateTime start, @Param("end") LocalDateTime end);
    
    long countByAction(String action);
    
    @Query("SELECT COUNT(DISTINCT al.user.id) FROM ActivityLog al WHERE al.timestamp BETWEEN :start AND :end")
    long countDistinctActiveUsers(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);
}
