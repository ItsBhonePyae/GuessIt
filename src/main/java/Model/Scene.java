package Model;

import java.time.LocalDateTime;

public class Scene {

    private int sceneId;
    private Integer contentId;
    private Integer difficultyLevel;
    private String filePath;
    private Double durationSeconds;
    private Integer isActive;
    private LocalDateTime createdAt;

    public Scene() {
    }

    public Scene(int sceneId, Integer contentId, Integer difficultyLevel, String filePath,
                 Double durationSeconds, Integer isActive, LocalDateTime createdAt) {
        this.sceneId = sceneId;
        this.contentId = contentId;
        this.difficultyLevel = difficultyLevel;
        this.filePath = filePath;
        this.durationSeconds = durationSeconds;
        this.isActive = isActive;
        this.createdAt = createdAt;
    }

    public int getSceneId() {
        return sceneId;
    }

    public void setSceneId(int sceneId) {
        this.sceneId = sceneId;
    }

    public Integer getContentId() {
        return contentId;
    }

    public void setContentId(Integer contentId) {
        this.contentId = contentId;
    }

    public Integer getDifficultyLevel() {
        return difficultyLevel;
    }

    public void setDifficultyLevel(Integer difficultyLevel) {
        this.difficultyLevel = difficultyLevel;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public Double getDurationSeconds() {
        return durationSeconds;
    }

    public void setDurationSeconds(Double durationSeconds) {
        this.durationSeconds = durationSeconds;
    }

    public Integer getIsActive() {
        return isActive;
    }

    public void setIsActive(Integer isActive) {
        this.isActive = isActive;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public String toString() {
        return "Scene{" +
                "sceneId=" + sceneId +
                ", contentId=" + contentId +
                ", difficultyLevel=" + difficultyLevel +
                ", filePath='" + filePath + '\'' +
                ", durationSeconds=" + durationSeconds +
                ", isActive=" + isActive +
                ", createdAt=" + createdAt +
                '}';
    }
}
