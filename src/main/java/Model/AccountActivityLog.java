package Model;

import java.time.LocalDateTime;

public class AccountActivityLog {

    private long accountActivityLogId;
    private Integer userId;
    private String emailAttempted;
    private String eventType;
    private String failureReason;
    private Long sessionId;
    private String deviceInfo;
    private LocalDateTime createdAt;

    public AccountActivityLog() {
    }

    public AccountActivityLog(long accountActivityLogId, Integer userId, String emailAttempted,
                              String eventType, String failureReason, Long sessionId,
                              String deviceInfo, LocalDateTime createdAt) {
        this.accountActivityLogId = accountActivityLogId;
        this.userId = userId;
        this.emailAttempted = emailAttempted;
        this.eventType = eventType;
        this.failureReason = failureReason;
        this.sessionId = sessionId;
        this.deviceInfo = deviceInfo;
        this.createdAt = createdAt;
    }

    public long getAccountActivityLogId() {
        return accountActivityLogId;
    }

    public void setAccountActivityLogId(long accountActivityLogId) {
        this.accountActivityLogId = accountActivityLogId;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public String getEmailAttempted() {
        return emailAttempted;
    }

    public void setEmailAttempted(String emailAttempted) {
        this.emailAttempted = emailAttempted;
    }

    public String getEventType() {
        return eventType;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    public String getFailureReason() {
        return failureReason;
    }

    public void setFailureReason(String failureReason) {
        this.failureReason = failureReason;
    }

    public Long getSessionId() {
        return sessionId;
    }

    public void setSessionId(Long sessionId) {
        this.sessionId = sessionId;
    }

    public String getDeviceInfo() {
        return deviceInfo;
    }

    public void setDeviceInfo(String deviceInfo) {
        this.deviceInfo = deviceInfo;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public String toString() {
        return "AccountActivityLog{" +
                "accountActivityLogId=" + accountActivityLogId +
                ", userId=" + userId +
                ", emailAttempted='" + emailAttempted + '\'' +
                ", eventType='" + eventType + '\'' +
                ", failureReason='" + failureReason + '\'' +
                ", sessionId=" + sessionId +
                ", deviceInfo='" + deviceInfo + '\'' +
                ", createdAt=" + createdAt +
                '}';
    }
}
