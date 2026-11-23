package Model;

import java.time.LocalDateTime;

public class UserPreference {

    private int userId;
    private String preferredAnswerMode;
    private String preferredCategory;
    private Integer volume;
    private LocalDateTime updatedAt;

    public UserPreference() {
    }

    public UserPreference(int userId, String preferredAnswerMode, String preferredCategory,
                          Integer volume, LocalDateTime updatedAt) {
        this.userId = userId;
        this.preferredAnswerMode = preferredAnswerMode;
        this.preferredCategory = preferredCategory;
        this.volume = volume;
        this.updatedAt = updatedAt;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getPreferredAnswerMode() {
        return preferredAnswerMode;
    }

    public void setPreferredAnswerMode(String preferredAnswerMode) {
        this.preferredAnswerMode = preferredAnswerMode;
    }

    public String getPreferredCategory() {
        return preferredCategory;
    }

    public void setPreferredCategory(String preferredCategory) {
        this.preferredCategory = preferredCategory;
    }

    public Integer getVolume() {
        return volume;
    }

    public void setVolume(Integer volume) {
        this.volume = volume;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    @Override
    public String toString() {
        return "UserPreference{" +
                "userId=" + userId +
                ", preferredAnswerMode='" + preferredAnswerMode + '\'' +
                ", preferredCategory='" + preferredCategory + '\'' +
                ", volume=" + volume +
                ", updatedAt=" + updatedAt +
                '}';
    }
}
