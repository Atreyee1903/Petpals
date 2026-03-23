package org.petpals.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "support_queries")
public class SupportQuery {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "query_id")
    private Long queryId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "query_text", nullable = false, columnDefinition = "TEXT")
    private String queryText;

    @Column(name = "query_timestamp")
    private LocalDateTime queryTimestamp;

    @Column(name = "admin_reply", columnDefinition = "TEXT")
    private String adminReply;

    @Column(name = "reply_timestamp")
    private LocalDateTime replyTimestamp;

    @Column(nullable = false, length = 20)
    private String status = "Open";

    public SupportQuery() {}

    @PrePersist
    protected void onCreate() {
        if (queryTimestamp == null) {
            queryTimestamp = LocalDateTime.now();
        }
        if (status == null) {
            status = "Open";
        }
    }

    // Getters and setters
    public Long getQueryId() { return queryId; }
    public void setQueryId(Long queryId) { this.queryId = queryId; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public String getQueryText() { return queryText; }
    public void setQueryText(String queryText) { this.queryText = queryText; }

    public LocalDateTime getQueryTimestamp() { return queryTimestamp; }
    public void setQueryTimestamp(LocalDateTime queryTimestamp) { this.queryTimestamp = queryTimestamp; }

    public String getAdminReply() { return adminReply; }
    public void setAdminReply(String adminReply) { this.adminReply = adminReply; }

    public LocalDateTime getReplyTimestamp() { return replyTimestamp; }
    public void setReplyTimestamp(LocalDateTime replyTimestamp) { this.replyTimestamp = replyTimestamp; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}

