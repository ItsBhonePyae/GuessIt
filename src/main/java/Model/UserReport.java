package Model;

import java.time.LocalDateTime;

public class UserReport {

    private int reportId;
    private Integer userId;
    private Integer contentId;
    private Integer sceneId;
    private String reportType;
    private String message;
    private String reportStatus;
    private LocalDateTime createdAt;
    private LocalDateTime resolvedAt;
    private Integer resolvedBy;

    public UserReport() {
    }

    public UserReport(int reportId, Integer userId, Integer contentId, Integer sceneId,
                      String reportType, String message, String reportStatus,
                      LocalDateTime createdAt, LocalDateTime resolvedAt, Integer resolvedBy) {
        this.reportId = reportId;
        this.userId = userId;
        this.contentId = contentId;
        this.sceneId = sceneId;
        this.reportType = reportType;
        this.message = message;
        this.reportStatus = reportStatus;
        this.createdAt = createdAt;
        this.resolvedAt = resolvedAt;
        this.resolvedBy = resolvedBy;
    }

    public int getReportId() {
        return reportId;
    }

    public void setReportId(int reportId) {
        this.reportId = reportId;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public Integer getContentId() {
        return contentId;
    }

    public void setContentId(Integer contentId) {
        this.contentId = contentId;
    }

    public Integer getSceneId() {
        return sceneId;
    }

    public void setSceneId(Integer sceneId) {
        this.sceneId = sceneId;
    }

    public String getReportType() {
        return reportType;
    }

    public void setReportType(String reportType) {
        this.reportType = reportType;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getReportStatus() {
        return reportStatus;
    }

    public void setReportStatus(String reportStatus) {
        this.reportStatus = reportStatus;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getResolvedAt() {
        return resolvedAt;
    }

    public void setResolvedAt(LocalDateTime resolvedAt) {
        this.resolvedAt = resolvedAt;
    }

    public Integer getResolvedBy() {
        return resolvedBy;
    }

    public void setResolvedBy(Integer resolvedBy) {
        this.resolvedBy = resolvedBy;
    }

    @Override
    public String toString() {
        return "UserReport{" +
                "reportId=" + reportId +
                ", userId=" + userId +
                ", contentId=" + contentId +
                ", sceneId=" + sceneId +
                ", reportType='" + reportType + '\'' +
                ", message='" + message + '\'' +
                ", reportStatus='" + reportStatus + '\'' +
                ", createdAt=" + createdAt +
                ", resolvedAt=" + resolvedAt +
                ", resolvedBy=" + resolvedBy +
                '}';
    }
}
