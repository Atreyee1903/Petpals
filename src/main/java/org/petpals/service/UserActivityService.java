package org.petpals.service;

import org.petpals.dto.ActivityLogDTO;
import org.petpals.model.ActivityLog;
import org.petpals.model.User;
import org.petpals.repository.ActivityLogRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class UserActivityService {

    private final ActivityLogRepository activityLogRepository;

    public UserActivityService(ActivityLogRepository activityLogRepository) {
        this.activityLogRepository = activityLogRepository;
    }

    public void logActivity(User user, String action, String description, String ipAddress, String userAgent) {
        ActivityLog log = new ActivityLog(user, action, description, ipAddress, userAgent);
        activityLogRepository.save(log);
    }

    public void logActivity(User user, String action, String description, String ipAddress, String userAgent, 
                           String entityType, Long entityId) {
        ActivityLog log = new ActivityLog(user, action, description, ipAddress, userAgent);
        log.setEntityType(entityType);
        log.setEntityId(entityId);
        activityLogRepository.save(log);
    }

    public void logActivity(User user, String action, String description) {
        ActivityLog log = new ActivityLog(user, action, description, null, null);
        activityLogRepository.save(log);
    }

    public Page<ActivityLogDTO> getUserActivity(Long userId, Pageable pageable) {
        Page<ActivityLog> logs = activityLogRepository.findByUserId(userId, pageable);
        return new PageImpl<>(convertToDTO(logs.getContent()), pageable, logs.getTotalElements());
    }

    public Page<ActivityLogDTO> getAllActivity(Pageable pageable) {
        Page<ActivityLog> logs = activityLogRepository.findAll(pageable);
        return new PageImpl<>(convertToDTO(logs.getContent()), pageable, logs.getTotalElements());
    }

    public List<ActivityLogDTO> getActivityByDateRange(LocalDateTime start, LocalDateTime end) {
        List<ActivityLog> logs = activityLogRepository.findByDateRange(start, end);
        return convertToDTO(logs);
    }

    public List<ActivityLogDTO> getUserActivityInRange(Long userId, LocalDateTime start, LocalDateTime end) {
        List<ActivityLog> logs = activityLogRepository.findUserActivityInRange(userId, start, end);
        return convertToDTO(logs);
    }

    public long getActivityCount(String action) {
        return activityLogRepository.countByAction(action);
    }

    public long getActiveUsersCount(LocalDateTime start, LocalDateTime end) {
        return activityLogRepository.countDistinctActiveUsers(start, end);
    }

    private ActivityLogDTO convertToDTO(ActivityLog log) {
        ActivityLogDTO dto = new ActivityLogDTO();
        dto.setId(log.getId());
        dto.setUserName(log.getUser() != null ? log.getUser().getUsername() : "SYSTEM");
        dto.setAction(log.getAction());
        dto.setDescription(log.getDescription());
        dto.setIpAddress(log.getIpAddress());
        dto.setTimestamp(log.getTimestamp());
        dto.setEntityType(log.getEntityType());
        dto.setEntityId(log.getEntityId());
        dto.setStatus(log.getStatus());
        return dto;
    }

    private List<ActivityLogDTO> convertToDTO(List<ActivityLog> logs) {
        return logs.stream().map(this::convertToDTO).collect(Collectors.toList());
    }
}
