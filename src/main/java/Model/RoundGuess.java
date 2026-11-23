package Model;

import java.time.LocalDateTime;

public class RoundGuess {

    private long guessId;
    private long roundId;
    private String guessText;
    private Integer guessContentId;
    private int isCorrect;
    private LocalDateTime createdAt;

    public RoundGuess() {
    }

    public RoundGuess(long guessId, long roundId, String guessText, Integer guessContentId,
                      int isCorrect, LocalDateTime createdAt) {
        this.guessId = guessId;
        this.roundId = roundId;
        this.guessText = guessText;
        this.guessContentId = guessContentId;
        this.isCorrect = isCorrect;
        this.createdAt = createdAt;
    }

    public long getGuessId() {
        return guessId;
    }

    public void setGuessId(long guessId) {
        this.guessId = guessId;
    }

    public long getRoundId() {
        return roundId;
    }

    public void setRoundId(long roundId) {
        this.roundId = roundId;
    }

    public String getGuessText() {
        return guessText;
    }

    public void setGuessText(String guessText) {
        this.guessText = guessText;
    }

    public Integer getGuessContentId() {
        return guessContentId;
    }

    public void setGuessContentId(Integer guessContentId) {
        this.guessContentId = guessContentId;
    }

    public int getIsCorrect() {
        return isCorrect;
    }

    public void setIsCorrect(int isCorrect) {
        this.isCorrect = isCorrect;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public String toString() {
        return "RoundGuess{" +
                "guessId=" + guessId +
                ", roundId=" + roundId +
                ", guessText='" + guessText + '\'' +
                ", guessContentId=" + guessContentId +
                ", isCorrect=" + isCorrect +
                ", createdAt=" + createdAt +
                '}';
    }
}
