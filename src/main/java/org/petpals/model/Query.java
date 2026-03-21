package org.petpals.model;

import java.sql.Timestamp;

public class Query {
    private int queryId;
    private int userId;
    private String queryText;
    private Timestamp queryTimestamp;
    private String adminReply;
    private Timestamp replyTimestamp;
    private String status;
    private String username; // To display who asked the query

    // Constructors
    public Query() {
    }

    public Query(int queryId, int userId, String queryText, Timestamp queryTimestamp, String adminReply, Timestamp replyTimestamp, String status, String username) {
        this.queryId = queryId;
        this.userId = userId;
        this.queryText = queryText;
        this.queryTimestamp = queryTimestamp;
        this.adminReply = adminReply;
        this.replyTimestamp = replyTimestamp;
        this.status = status;
        this.username = username;
    }


    // Getters and Setters
    public int getQueryId() {
        return queryId;
    }

    public void setQueryId(int queryId) {
        this.queryId = queryId;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getQueryText() {
        return queryText;
    }

    public void setQueryText(String queryText) {
        this.queryText = queryText;
    }

    public Timestamp getQueryTimestamp() {
        return queryTimestamp;
    }

    public void setQueryTimestamp(Timestamp queryTimestamp) {
        this.queryTimestamp = queryTimestamp;
    }

    public String getAdminReply() {
        return adminReply;
    }

    public void setAdminReply(String adminReply) {
        this.adminReply = adminReply;
    }

    public Timestamp getReplyTimestamp() {
        return replyTimestamp;
    }

    public void setReplyTimestamp(Timestamp replyTimestamp) {
        this.replyTimestamp = replyTimestamp;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

     public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    @Override
    public String toString() {
        return "Query{" +
               "queryId=" + queryId +
               ", userId=" + userId +
               ", queryText='" + queryText + '\'' +
               ", queryTimestamp=" + queryTimestamp +
               ", adminReply='" + adminReply + '\'' +
               ", replyTimestamp=" + replyTimestamp +
               ", status='" + status + '\'' +
               ", username='" + username + '\'' +
               '}';
    }
}
