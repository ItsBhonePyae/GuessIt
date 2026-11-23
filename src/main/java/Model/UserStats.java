package Model;

import java.time.LocalDateTime;

public class UserStats {

    private int userId;
    private int userLevel;
    private int totalExp;
    private int matchesPlayed;
    private int roundsPlayed;
    private int roundsCorrect;
    private int bestMatchScore;
    private int currentStreak;
    private int bestStreak;
    private LocalDateTime lastPlayedAt;

    public UserStats() {
    }

    public UserStats(int userId, int userLevel, int totalExp, int matchesPlayed, int roundsPlayed,
                     int roundsCorrect, int bestMatchScore, int currentStreak, int bestStreak,
                     LocalDateTime lastPlayedAt) {
        this.userId = userId;
        this.userLevel = userLevel;
        this.totalExp = totalExp;
        this.matchesPlayed = matchesPlayed;
        this.roundsPlayed = roundsPlayed;
        this.roundsCorrect = roundsCorrect;
        this.bestMatchScore = bestMatchScore;
        this.currentStreak = currentStreak;
        this.bestStreak = bestStreak;
        this.lastPlayedAt = lastPlayedAt;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public int getUserLevel() {
        return userLevel;
    }

    public void setUserLevel(int userLevel) {
        this.userLevel = userLevel;
    }

    public int getTotalExp() {
        return totalExp;
    }

    public void setTotalExp(int totalExp) {
        this.totalExp = totalExp;
    }

    public int getMatchesPlayed() {
        return matchesPlayed;
    }

    public void setMatchesPlayed(int matchesPlayed) {
        this.matchesPlayed = matchesPlayed;
    }

    public int getRoundsPlayed() {
        return roundsPlayed;
    }

    public void setRoundsPlayed(int roundsPlayed) {
        this.roundsPlayed = roundsPlayed;
    }

    public int getRoundsCorrect() {
        return roundsCorrect;
    }

    public void setRoundsCorrect(int roundsCorrect) {
        this.roundsCorrect = roundsCorrect;
    }

    public int getBestMatchScore() {
        return bestMatchScore;
    }

    public void setBestMatchScore(int bestMatchScore) {
        this.bestMatchScore = bestMatchScore;
    }

    public int getCurrentStreak() {
        return currentStreak;
    }

    public void setCurrentStreak(int currentStreak) {
        this.currentStreak = currentStreak;
    }

    public int getBestStreak() {
        return bestStreak;
    }

    public void setBestStreak(int bestStreak) {
        this.bestStreak = bestStreak;
    }

    public LocalDateTime getLastPlayedAt() {
        return lastPlayedAt;
    }

    public void setLastPlayedAt(LocalDateTime lastPlayedAt) {
        this.lastPlayedAt = lastPlayedAt;
    }

    @Override
    public String toString() {
        return "UserStats{" +
                "userId=" + userId +
                ", userLevel=" + userLevel +
                ", totalExp=" + totalExp +
                ", matchesPlayed=" + matchesPlayed +
                ", roundsPlayed=" + roundsPlayed +
                ", roundsCorrect=" + roundsCorrect +
                ", bestMatchScore=" + bestMatchScore +
                ", currentStreak=" + currentStreak +
                ", bestStreak=" + bestStreak +
                ", lastPlayedAt=" + lastPlayedAt +
                '}';
    }
}
