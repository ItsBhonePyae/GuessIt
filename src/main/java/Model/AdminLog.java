package Model;

import java.time.LocalDateTime;

public class AdminLog {

    private int adminLogId;
    private Integer adminId;
    private String action;
    private String entityType;
    private String details;
    private LocalDateTime createdAt;

    public AdminLog() {
    }

    public AdminLog(int adminLogId, Integer adminId, String action, String entityType,
                    String details, LocalDateTime createdAt) {
        this.adminLogId = adminLogId;
        this.adminId = adminId;
        this.action = action;
        this.entityType = entityType;
        this.details = details;
        this.createdAt = createdAt;
    }

    public int getAdminLogId() {
        return adminLogId;
    }

    public void setAdminLogId(int adminLogId) {
        this.adminLogId = adminLogId;
    }

    public Integer getAdminId() {
        return adminId;
    }

    public void setAdminId(Integer adminId) {
        this.adminId = adminId;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getEntityType() {
        return entityType;
    }

    public void setEntityType(String entityType) {
        this.entityType = entityType;
    }

    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public String toString() {
        return "AdminLog{" +
                "adminLogId=" + adminLogId +
                ", adminId=" + adminId +
                ", action='" + action + '\'' +
                ", entityType='" + entityType + '\'' +
                ", details='" + details + '\'' +
                ", createdAt=" + createdAt +
                '}';
    }
}
