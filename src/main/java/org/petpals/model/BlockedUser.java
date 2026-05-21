package org.petpals.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "blocked_users", indexes = @Index(name = "idx_user_id", columnList = "user_id"))
public class BlockedUser {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "blocked_by_admin_id", nullable = false)
    private User blockedByAdmin;

    @Column(columnDefinition = "TEXT")
    private String reason;

    @Column(name = "blocked_at", nullable = false)
    private LocalDateTime blockedAt;

    @Column(name = "unblocked_at")
    private LocalDateTime unblockedAt;

    @Column(nullable = false)
    private boolean active = true;

    public BlockedUser() {}

    public BlockedUser(User user, User blockedByAdmin, String reason) {
        this.user = user;
        this.blockedByAdmin = blockedByAdmin;
        this.reason = reason;
        this.blockedAt = LocalDateTime.now();
        this.active = true;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public User getBlockedByAdmin() { return blockedByAdmin; }
    public void setBlockedByAdmin(User blockedByAdmin) { this.blockedByAdmin = blockedByAdmin; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }

    public LocalDateTime getBlockedAt() { return blockedAt; }
    public void setBlockedAt(LocalDateTime blockedAt) { this.blockedAt = blockedAt; }

    public LocalDateTime getUnblockedAt() { return unblockedAt; }
    public void setUnblockedAt(LocalDateTime unblockedAt) { this.unblockedAt = unblockedAt; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
}
