package Testing_for_bascially_everything;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

/**
 * State behind {@code SidebarTest.fxml}.
 *
 * <p>Holds no Node references, so it can be swapped for a real service later or
 * exercised without a live scene. The controller listens to these properties;
 * nothing here touches the UI.</p>
 */
public final class SidebarTestModel {

    /** Pages reachable from the sidebar. Settings is a modal, not a page. */
    public static final String SINGLEPLAYER = "singleplayer";
    public static final String HISTORY = "history";

    /** History filters: purely presentational for the demo rows. */
    public static final String ALL = "all";
    public static final String WINS = "wins";
    public static final String LOSSES = "losses";

    public enum Difficulty { EASY, MEDIUM, HARD }

    public enum Accent {
        LIME("Lime"),
        VIOLET("Violet");

        private final String label;

        Accent(String label) {
            this.label = label;
        }

        public String label() {
            return label;
        }
    }

    private final StringProperty page =
        new SimpleStringProperty(this, "page", SINGLEPLAYER);
    private final BooleanProperty sidebarCollapsed =
        new SimpleBooleanProperty(this, "sidebarCollapsed", false);
    private final BooleanProperty settingsOpen =
        new SimpleBooleanProperty(this, "settingsOpen", false);
    private final ObjectProperty<Difficulty> difficulty =
        new SimpleObjectProperty<>(this, "difficulty", Difficulty.MEDIUM);
    private final ObjectProperty<Accent> accent =
        new SimpleObjectProperty<>(this, "accent", Accent.LIME);
    private final StringProperty status =
        new SimpleStringProperty(this, "status", "Accent preview set to Lime.");
    private final StringProperty playerName =
        new SimpleStringProperty(this, "playerName", "Guest Player");
    private final StringProperty playerLevel =
        new SimpleStringProperty(this, "playerLevel", "Level 1");
    private final IntegerProperty streakDays =
        new SimpleIntegerProperty(this, "streakDays", 6);
    private final StringProperty historyFilter =
        new SimpleStringProperty(this, "historyFilter", ALL);

    // --------------------------------------------------------------- pages

    public StringProperty pageProperty() {
        return page;
    }

    public String getPage() {
        return page.get();
    }

    public void setPage(String value) {
        page.set(value);
    }

    /** Ignores unknown keys instead of leaving the centre blank. */
    public void showPage(String key) {
        page.set(HISTORY.equals(key) ? HISTORY : SINGLEPLAYER);
    }

    public String pageTitle() {
        return HISTORY.equals(getPage()) ? "History" : "Singleplayer";
    }

    // -------------------------------------------------------------- layout

    public BooleanProperty sidebarCollapsedProperty() {
        return sidebarCollapsed;
    }

    public boolean isSidebarCollapsed() {
        return sidebarCollapsed.get();
    }

    public void setSidebarCollapsed(boolean value) {
        sidebarCollapsed.set(value);
    }

    // ------------------------------------------------------------- modals

    public BooleanProperty settingsOpenProperty() {
        return settingsOpen;
    }

    public boolean isSettingsOpen() {
        return settingsOpen.get();
    }

    public void setSettingsOpen(boolean value) {
        settingsOpen.set(value);
    }

    public void openSettings() {
        setSettingsOpen(true);
    }

    public void closeSettings() {
        setSettingsOpen(false);
    }

    public void toggleSettings() {
        setSettingsOpen(!isSettingsOpen());
    }

    // ----------------------------------------------------------- settings

    public ObjectProperty<Difficulty> difficultyProperty() {
        return difficulty;
    }

    public Difficulty getDifficulty() {
        return difficulty.get();
    }

    public void setDifficulty(Difficulty value) {
        difficulty.set(value == null ? Difficulty.MEDIUM : value);
    }

    public ObjectProperty<Accent> accentProperty() {
        return accent;
    }

    public Accent getAccent() {
        return accent.get();
    }

    public void setAccent(Accent value) {
        accent.set(value == null ? Accent.LIME : value);
    }

    public StringProperty statusProperty() {
        return status;
    }

    public String getStatus() {
        return status.get();
    }

    public void setStatus(String value) {
        status.set(value == null ? "" : value);
    }

    // ------------------------------------------------------------- player

    public StringProperty playerNameProperty() {
        return playerName;
    }

    public String getPlayerName() {
        return playerName.get();
    }

    public void setPlayerName(String value) {
        playerName.set(value == null ? "" : value);
    }

    public StringProperty playerLevelProperty() {
        return playerLevel;
    }

    public String getPlayerLevel() {
        return playerLevel.get();
    }

    public void setPlayerLevel(String value) {
        playerLevel.set(value == null ? "" : value);
    }

    public IntegerProperty streakDaysProperty() {
        return streakDays;
    }

    public int getStreakDays() {
        return streakDays.get();
    }

    public void setStreakDays(int value) {
        streakDays.set(Math.max(0, value));
    }

    public String streakText() {
        int days = getStreakDays();
        return days + (days == 1 ? " day strong" : " days strong");
    }

    // ------------------------------------------------------------ history

    public StringProperty historyFilterProperty() {
        return historyFilter;
    }

    public String getHistoryFilter() {
        return historyFilter.get();
    }

    /** Ignores unknown filters instead of blanking the list. */
    public void setHistoryFilter(String value) {
        historyFilter.set(WINS.equals(value) || LOSSES.equals(value) ? value : ALL);
    }
}
