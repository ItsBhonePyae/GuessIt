package Model;

import java.time.LocalDateTime;

public class UserSession {

    private long sessionId;
    private Integer userId;
    private String tokenHash;
    private LocalDateTime createdAt;
    private String deviceName;
    private LocalDateTime lastUsedAt;
    private LocalDateTime expiredAt;
    private LocalDateTime revokedAt;

    public UserSession() {
    }

    public UserSession(long sessionId, Integer userId, String tokenHash, LocalDateTime createdAt,
                       String deviceName, LocalDateTime lastUsedAt, LocalDateTime expiredAt,
                       LocalDateTime revokedAt) {
        this.sessionId = sessionId;
        this.userId = userId;
        this.tokenHash = tokenHash;
        this.createdAt = createdAt;
        this.deviceName = deviceName;
        this.lastUsedAt = lastUsedAt;
        this.expiredAt = expiredAt;
        this.revokedAt = revokedAt;
    }

    public long getSessionId() {
        return sessionId;
    }

    public void setSessionId(long sessionId) {
        this.sessionId = sessionId;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public String getTokenHash() {
        return tokenHash;
    }

    public void setTokenHash(String tokenHash) {
        this.tokenHash = tokenHash;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public String getDeviceName() {
        return deviceName;
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }

    public LocalDateTime getLastUsedAt() {
        return lastUsedAt;
    }

    public void setLastUsedAt(LocalDateTime lastUsedAt) {
        this.lastUsedAt = lastUsedAt;
    }

    public LocalDateTime getExpiredAt() {
        return expiredAt;
    }

    public void setExpiredAt(LocalDateTime expiredAt) {
        this.expiredAt = expiredAt;
    }

    public LocalDateTime getRevokedAt() {
        return revokedAt;
    }

    public void setRevokedAt(LocalDateTime revokedAt) {
        this.revokedAt = revokedAt;
    }

    @Override
    public String toString() {
        return "UserSession{" +
                "sessionId=" + sessionId +
                ", userId=" + userId +
                ", tokenHash='" + tokenHash + '\'' +
                ", createdAt=" + createdAt +
                ", deviceName='" + deviceName + '\'' +
                ", lastUsedAt=" + lastUsedAt +
                ", expiredAt=" + expiredAt +
                ", revokedAt=" + revokedAt +
                '}';
    }
}
