package Model;

import java.time.LocalDateTime;

public class GameRound {

    private long roundId;
    private long matchId;
    private int roundNumber;
    private int contentId;
    private Integer sceneId;
    private int scenesViewed;
    private int hintsUsed;
    private int isCorrect;
    private int skipped;
    private int pointsEarned;
    private int expEarned;
    private Integer timeTakenMs;
    private LocalDateTime startedAt;
    private LocalDateTime endedAt;

    public GameRound() {
    }

    public GameRound(long roundId, long matchId, int roundNumber, int contentId, Integer sceneId,
                     int scenesViewed, int hintsUsed, int isCorrect, int skipped, int pointsEarned,
                     int expEarned, Integer timeTakenMs, LocalDateTime startedAt,
                     LocalDateTime endedAt) {
        this.roundId = roundId;
        this.matchId = matchId;
        this.roundNumber = roundNumber;
        this.contentId = contentId;
        this.sceneId = sceneId;
        this.scenesViewed = scenesViewed;
        this.hintsUsed = hintsUsed;
        this.isCorrect = isCorrect;
        this.skipped = skipped;
        this.pointsEarned = pointsEarned;
        this.expEarned = expEarned;
        this.timeTakenMs = timeTakenMs;
        this.startedAt = startedAt;
        this.endedAt = endedAt;
    }

    public long getRoundId() {
        return roundId;
    }

    public void setRoundId(long roundId) {
        this.roundId = roundId;
    }

    public long getMatchId() {
        return matchId;
    }

    public void setMatchId(long matchId) {
        this.matchId = matchId;
    }

    public int getRoundNumber() {
        return roundNumber;
    }

    public void setRoundNumber(int roundNumber) {
        this.roundNumber = roundNumber;
    }

    public int getContentId() {
        return contentId;
    }

    public void setContentId(int contentId) {
        this.contentId = contentId;
    }

    public Integer getSceneId() {
        return sceneId;
    }

    public void setSceneId(Integer sceneId) {
        this.sceneId = sceneId;
    }

    public int getScenesViewed() {
        return scenesViewed;
    }

    public void setScenesViewed(int scenesViewed) {
        this.scenesViewed = scenesViewed;
    }

    public int getHintsUsed() {
        return hintsUsed;
    }

    public void setHintsUsed(int hintsUsed) {
        this.hintsUsed = hintsUsed;
    }

    public int getIsCorrect() {
        return isCorrect;
    }

    public void setIsCorrect(int isCorrect) {
        this.isCorrect = isCorrect;
    }

    public int getSkipped() {
        return skipped;
    }

    public void setSkipped(int skipped) {
        this.skipped = skipped;
    }

    public int getPointsEarned() {
        return pointsEarned;
    }

    public void setPointsEarned(int pointsEarned) {
        this.pointsEarned = pointsEarned;
    }

    public int getExpEarned() {
        return expEarned;
    }

    public void setExpEarned(int expEarned) {
        this.expEarned = expEarned;
    }

    public Integer getTimeTakenMs() {
        return timeTakenMs;
    }

    public void setTimeTakenMs(Integer timeTakenMs) {
        this.timeTakenMs = timeTakenMs;
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
        return "GameRound{" +
                "roundId=" + roundId +
                ", matchId=" + matchId +
                ", roundNumber=" + roundNumber +
                ", contentId=" + contentId +
                ", sceneId=" + sceneId +
                ", scenesViewed=" + scenesViewed +
                ", hintsUsed=" + hintsUsed +
                ", isCorrect=" + isCorrect +
                ", skipped=" + skipped +
                ", pointsEarned=" + pointsEarned +
                ", expEarned=" + expEarned +
                ", timeTakenMs=" + timeTakenMs +
                ", startedAt=" + startedAt +
                ", endedAt=" + endedAt +
                '}';
    }
}
