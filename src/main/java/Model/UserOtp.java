package Model;

import java.time.LocalDateTime;

public class UserOtp {

    private int otpId;
    private int userId;
    private String purpose;
    private String codeHash;
    private String sentTo;
    private Integer attempts;
    private Integer maxAttempts;
    private LocalDateTime createdAt;
    private LocalDateTime expiresAt;
    private LocalDateTime usedAt;

    public UserOtp() {
    }

    public UserOtp(int otpId, int userId, String purpose, String codeHash, String sentTo,
                   Integer attempts, Integer maxAttempts, LocalDateTime createdAt,
                   LocalDateTime expiresAt, LocalDateTime usedAt) {
        this.otpId = otpId;
        this.userId = userId;
        this.purpose = purpose;
        this.codeHash = codeHash;
        this.sentTo = sentTo;
        this.attempts = attempts;
        this.maxAttempts = maxAttempts;
        this.createdAt = createdAt;
        this.expiresAt = expiresAt;
        this.usedAt = usedAt;
    }

    public int getOtpId() {
        return otpId;
    }

    public void setOtpId(int otpId) {
        this.otpId = otpId;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getPurpose() {
        return purpose;
    }

    public void setPurpose(String purpose) {
        this.purpose = purpose;
    }

    public String getCodeHash() {
        return codeHash;
    }

    public void setCodeHash(String codeHash) {
        this.codeHash = codeHash;
    }

    public String getSentTo() {
        return sentTo;
    }

    public void setSentTo(String sentTo) {
        this.sentTo = sentTo;
    }

    public Integer getAttempts() {
        return attempts;
    }

    public void setAttempts(Integer attempts) {
        this.attempts = attempts;
    }

    public Integer getMaxAttempts() {
        return maxAttempts;
    }

    public void setMaxAttempts(Integer maxAttempts) {
        this.maxAttempts = maxAttempts;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(LocalDateTime expiresAt) {
        this.expiresAt = expiresAt;
    }

    public LocalDateTime getUsedAt() {
        return usedAt;
    }

    public void setUsedAt(LocalDateTime usedAt) {
        this.usedAt = usedAt;
    }

    @Override
    public String toString() {
        return "UserOtp{" +
                "otpId=" + otpId +
                ", userId=" + userId +
                ", purpose='" + purpose + '\'' +
                ", codeHash='" + codeHash + '\'' +
                ", sentTo='" + sentTo + '\'' +
                ", attempts=" + attempts +
                ", maxAttempts=" + maxAttempts +
                ", createdAt=" + createdAt +
                ", expiresAt=" + expiresAt +
                ", usedAt=" + usedAt +
                '}';
    }
}
