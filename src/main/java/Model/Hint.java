package Model;

import java.time.LocalDateTime;

public class Hint {

    private int hintId;
    private Integer contentId;
    private Integer hintOrder;
    private String hintText;
    private LocalDateTime createdAt;

    public Hint() {
    }

    public Hint(int hintId, Integer contentId, Integer hintOrder, String hintText,
                LocalDateTime createdAt) {
        this.hintId = hintId;
        this.contentId = contentId;
        this.hintOrder = hintOrder;
        this.hintText = hintText;
        this.createdAt = createdAt;
    }

    public int getHintId() {
        return hintId;
    }

    public void setHintId(int hintId) {
        this.hintId = hintId;
    }

    public Integer getContentId() {
        return contentId;
    }

    public void setContentId(Integer contentId) {
        this.contentId = contentId;
    }

    public Integer getHintOrder() {
        return hintOrder;
    }

    public void setHintOrder(Integer hintOrder) {
        this.hintOrder = hintOrder;
    }

    public String getHintText() {
        return hintText;
    }

    public void setHintText(String hintText) {
        this.hintText = hintText;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public String toString() {
        return "Hint{" +
                "hintId=" + hintId +
                ", contentId=" + contentId +
                ", hintOrder=" + hintOrder +
                ", hintText='" + hintText + '\'' +
                ", createdAt=" + createdAt +
                '}';
    }
}
