package Model;

import java.time.LocalDateTime;

public class GameMatch {

    private long matchId;
    private int userId;
    private String contentType;
    private Integer genreId;
    private String answerMode;
    private String gameStatus;
    private int totalRounds;
    private int correctRounds;
    private int totalScore;
    private int totalExpEarned;
    private int hintsUsed;
    private int bestStreak;
    private Integer durationSeconds;
    private Long sessionId;
    private LocalDateTime startedAt;
    private LocalDateTime endedAt;

    public GameMatch() {
    }

    public GameMatch(long matchId, int userId, String contentType, Integer genreId,
                     String answerMode, String gameStatus, int totalRounds, int correctRounds,
                     int totalScore, int totalExpEarned, int hintsUsed, int bestStreak,
                     Integer durationSeconds, Long sessionId, LocalDateTime startedAt,
                     LocalDateTime endedAt) {
        this.matchId = matchId;
        this.userId = userId;
        this.contentType = contentType;
        this.genreId = genreId;
        this.answerMode = answerMode;
        this.gameStatus = gameStatus;
        this.totalRounds = totalRounds;
        this.correctRounds = correctRounds;
        this.totalScore = totalScore;
        this.totalExpEarned = totalExpEarned;
        this.hintsUsed = hintsUsed;
        this.bestStreak = bestStreak;
        this.durationSeconds = durationSeconds;
        this.sessionId = sessionId;
        this.startedAt = startedAt;
        this.endedAt = endedAt;
    }

    public long getMatchId() {
        return matchId;
    }

    public void setMatchId(long matchId) {
        this.matchId = matchId;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public Integer getGenreId() {
        return genreId;
    }

    public void setGenreId(Integer genreId) {
        this.genreId = genreId;
    }

    public String getAnswerMode() {
        return answerMode;
    }

    public void setAnswerMode(String answerMode) {
        this.answerMode = answerMode;
    }

    public String getGameStatus() {
        return gameStatus;
    }

    public void setGameStatus(String gameStatus) {
        this.gameStatus = gameStatus;
    }

    public int getTotalRounds() {
        return totalRounds;
    }

    public void setTotalRounds(int totalRounds) {
        this.totalRounds = totalRounds;
    }

    public int getCorrectRounds() {
        return correctRounds;
    }

    public void setCorrectRounds(int correctRounds) {
        this.correctRounds = correctRounds;
    }

    public int getTotalScore() {
        return totalScore;
    }

    public void setTotalScore(int totalScore) {
        this.totalScore = totalScore;
    }

    public int getTotalExpEarned() {
        return totalExpEarned;
    }

    public void setTotalExpEarned(int totalExpEarned) {
        this.totalExpEarned = totalExpEarned;
    }

    public int getHintsUsed() {
        return hintsUsed;
    }

    public void setHintsUsed(int hintsUsed) {
        this.hintsUsed = hintsUsed;
    }

    public int getBestStreak() {
        return bestStreak;
    }

    public void setBestStreak(int bestStreak) {
        this.bestStreak = bestStreak;
    }

    public Integer getDurationSeconds() {
        return durationSeconds;
    }

    public void setDurationSeconds(Integer durationSeconds) {
        this.durationSeconds = durationSeconds;
    }

    public Long getSessionId() {
        return sessionId;
    }

    public void setSessionId(Long sessionId) {
        this.sessionId = sessionId;
    }

    public LocalDateTime getStartedAt() {
        return startedAt;
    }

    public void setStartedAt(LocalDateTime startedAt) {
        this.startedAt = startedAt;
    }

    public LocalDateTime getEndedAt() {
        return endedAt;
    }

    public void setEndedAt(LocalDateTime endedAt) {
        this.endedAt = endedAt;
    }

    @Override
    public String toString() {
        return "GameMatch{" +
                "matchId=" + matchId +
                ", userId=" + userId +
                ", contentType='" + contentType + '\'' +
                ", genreId=" + genreId +
                ", answerMode='" + answerMode + '\'' +
                ", gameStatus='" + gameStatus + '\'' +
                ", totalRounds=" + totalRounds +
                ", correctRounds=" + correctRounds +
                ", totalScore=" + totalScore +
                ", totalExpEarned=" + totalExpEarned +
                ", hintsUsed=" + hintsUsed +
                ", bestStreak=" + bestStreak +
                ", durationSeconds=" + durationSeconds +
                ", sessionId=" + sessionId +
                ", startedAt=" + startedAt +
                ", endedAt=" + endedAt +
                '}';
    }
}
