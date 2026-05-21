package org.petpals.dto;

import java.time.LocalDateTime;

public class ActivityLogDTO {
    private Long id;
    private String userName;
    private String action;
    private String description;
    private String ipAddress;
    private LocalDateTime timestamp;
    private String entityType;
    private Long entityId;
    private String status;

    public ActivityLogDTO() {}

    public ActivityLogDTO(Long id, String userName, String action, String description, LocalDateTime timestamp) {
        this.id = id;
        this.userName = userName;
        this.action = action;
        this.description = description;
        this.timestamp = timestamp;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }

    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getIpAddress() { return ipAddress; }
    public void setIpAddress(String ipAddress) { this.ipAddress = ipAddress; }

    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }

    public String getEntityType() { return entityType; }
    public void setEntityType(String entityType) { this.entityType = entityType; }

    public Long getEntityId() { return entityId; }
    public void setEntityId(Long entityId) { this.entityId = entityId; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
