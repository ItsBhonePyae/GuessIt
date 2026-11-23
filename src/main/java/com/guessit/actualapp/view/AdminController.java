package com.guessit.actualapp.view;

import com.guessit.actualapp.model.GameHistory;
import com.guessit.actualapp.model.Movie;
import com.guessit.actualapp.util.MovieCatalog;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Window;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.atomic.AtomicInteger;

public final class AdminController {
    private static final DateTimeFormatter LOG_TIME =
        DateTimeFormatter.ofPattern("MMM d, HH:mm");

    @FXML private StackPane pageHost;
    @FXML private VBox dashboardPage;
    @FXML private VBox usersPage;
    @FXML private VBox assetsPage;
    @FXML private VBox logsPage;
    @FXML private Label statusLabel;
    @FXML private Label totalUsersLabel;
    @FXML private Label totalMoviesLabel;
    @FXML private Label totalSongsLabel;
    @FXML private Label totalPlaysLabel;
    @FXML private TextField userSearchField;
    @FXML private ComboBox<String> assetTypeBox;
    @FXML private TableView<UserRow> usersTable;
    @FXML private TableColumn<UserRow, String> userNameColumn;
    @FXML private TableColumn<UserRow, String> userEmailColumn;
    @FXML private TableColumn<UserRow, String> userRoleColumn;
    @FXML private TableColumn<UserRow, String> userStatusColumn;
    @FXML private TableView<AssetRow> assetsTable;
    @FXML private TableColumn<AssetRow, String> assetNameColumn;
    @FXML private TableColumn<AssetRow, String> assetTypeColumn;
    @FXML private TableColumn<AssetRow, String> assetDetailsColumn;
    @FXML private TableColumn<AssetRow, String> assetStatusColumn;
    @FXML private TableView<LogRow> logsTable;
    @FXML private TableColumn<LogRow, String> logTimeColumn;
    @FXML private TableColumn<LogRow, String> logActionColumn;
    @FXML private TableColumn<LogRow, String> logDetailsColumn;
    @FXML private Button dashboardNav;
    @FXML private Button usersNav;
    @FXML private Button assetsNav;
    @FXML private Button logsNav;

    private final ObservableList<UserRow> users = FXCollections.observableArrayList();
    private final ObservableList<AssetRow> assets = FXCollections.observableArrayList();
    private final ObservableList<LogRow> logs = FXCollections.observableArrayList();
    private final AtomicInteger nextUserId = new AtomicInteger(4);
    private final AtomicInteger nextAssetId = new AtomicInteger(10);

    @FXML
    private void initialize() {
        configureTables();
        loadPreviewData();

        FilteredList<UserRow> filteredUsers = new FilteredList<>(users, row -> true);
        usersTable.setItems(filteredUsers);
        userSearchField.textProperty().addListener((observable, oldValue, newValue) -> {
            String query = newValue == null ? "" : newValue.trim().toLowerCase();
            filteredUsers.setPredicate(row -> query.isEmpty()
                || row.username().toLowerCase().contains(query)
                || row.email().toLowerCase().contains(query)
                || row.role().toLowerCase().contains(query)
                || row.status().toLowerCase().contains(query));
        });

        FilteredList<AssetRow> filteredAssets = new FilteredList<>(assets, row -> true);
        assetsTable.setItems(filteredAssets);
        assetTypeBox.setItems(FXCollections.observableArrayList("All assets", "Movie", "Song"));
        assetTypeBox.setValue("All assets");
        assetTypeBox.valueProperty().addListener((observable, oldValue, newValue) -> {
            String selected = newValue == null ? "All assets" : newValue;
            filteredAssets.setPredicate(row -> selected.equals("All assets")
                || row.type().equals(selected));
        });
        updateStats();
        showDashboard(null);
    }

    private void configureTables() {
        userNameColumn.setCellValueFactory(cell ->
            new javafx.beans.property.ReadOnlyStringWrapper(cell.getValue().username()));
        userEmailColumn.setCellValueFactory(cell ->
            new javafx.beans.property.ReadOnlyStringWrapper(cell.getValue().email()));
        userRoleColumn.setCellValueFactory(cell ->
            new javafx.beans.property.ReadOnlyStringWrapper(cell.getValue().role()));
        userStatusColumn.setCellValueFactory(cell ->
            new javafx.beans.property.ReadOnlyStringWrapper(cell.getValue().status()));

        assetNameColumn.setCellValueFactory(cell ->
            new javafx.beans.property.ReadOnlyStringWrapper(cell.getValue().title()));
        assetTypeColumn.setCellValueFactory(cell ->
            new javafx.beans.property.ReadOnlyStringWrapper(cell.getValue().type()));
        assetDetailsColumn.setCellValueFactory(cell ->
            new javafx.beans.property.ReadOnlyStringWrapper(cell.getValue().details()));
        assetStatusColumn.setCellValueFactory(cell ->
            new javafx.beans.property.ReadOnlyStringWrapper(cell.getValue().status()));

        logTimeColumn.setCellValueFactory(cell ->
            new javafx.beans.property.ReadOnlyStringWrapper(cell.getValue().time()));
        logActionColumn.setCellValueFactory(cell ->
            new javafx.beans.property.ReadOnlyStringWrapper(cell.getValue().action()));
        logDetailsColumn.setCellValueFactory(cell ->
            new javafx.beans.property.ReadOnlyStringWrapper(cell.getValue().details()));
    }

    private void loadPreviewData() {
        users.addAll(
            new UserRow("U-001", "alex.morgan", "alex@example.com", "Player", "Active"),
            new UserRow("U-002", "jamie.chen", "jamie@example.com", "Player", "Active"),
            new UserRow("U-003", "admin", "admin@guessit.local", "Admin", "Active")
        );

        for (Movie movie : MovieCatalog.getAllMovies()) {
            assets.add(new AssetRow("A-" + movie.getContentId(), movie.getTitle(), "Movie",
                movie.getGenre() + "  |  " + movie.getYear(), "Ready"));
        }
        assets.addAll(
            new AssetRow("A-008", "Midnight City", "Song", "M83  |  2011", "Preview"),
            new AssetRow("A-009", "Blinding Lights", "Song", "The Weeknd  |  2019", "Preview")
        );

        logs.addAll(
            new LogRow("Today, 08:15", "Workspace opened", "Admin preview started"),
            new LogRow("Yesterday, 16:42", "Asset reviewed", "The Lord of the Rings"),
            new LogRow("Yesterday, 15:10", "User status checked", "2 active players")
        );
    }

    @FXML
    private void showDashboard(ActionEvent event) {
        showPage(dashboardPage, dashboardNav, "Dashboard");
    }

    @FXML
    private void showUsers(ActionEvent event) {
        showPage(usersPage, usersNav, "User management");
    }

    @FXML
    private void showAssets(ActionEvent event) {
        showPage(assetsPage, assetsNav, "Movies & songs");
    }

    @FXML
    private void showLogs(ActionEvent event) {
        showPage(logsPage, logsNav, "Activity log");
    }

    private void showPage(VBox selected, Button selectedNav, String title) {
        VBox[] pages = {dashboardPage, usersPage, assetsPage, logsPage};
        for (VBox page : pages) {
            boolean visible = page == selected;
            page.setVisible(visible);
            page.setManaged(visible);
        }
        Button[] navButtons = {dashboardNav, usersNav, assetsNav, logsNav};
        for (Button button : navButtons) {
            button.getStyleClass().remove("active");
        }
        selectedNav.getStyleClass().add("active");
        statusLabel.setText(title);
    }

    @FXML
    private void addUser(ActionEvent event) {
        TextField username = new TextField();
        username.setPromptText("e.g. taylor.lee");
        TextField email = new TextField();
        email.setPromptText("name@example.com");
        ComboBox<String> role = new ComboBox<>(
            FXCollections.observableArrayList("Player", "Admin"));
        role.setValue("Player");
        role.setMaxWidth(Double.MAX_VALUE);

        GridPane form = formGrid();
        addFormRow(form, 0, "Username", username);
        addFormRow(form, 1, "Email", email);
        addFormRow(form, 2, "Role", role);
        Label validation = new Label("Preview only; this does not create a login account.");
        validation.getStyleClass().add("dialog-hint");

        Dialog<ButtonType> dialog = createDialog("Add user", "Add a user to this session preview");
        dialog.getDialogPane().setContent(new VBox(12, form, validation));
        ButtonType save = new ButtonType("Add user", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().setAll(save, ButtonType.CANCEL);
        dialog.setResultConverter(button -> button);
        Button saveButton = (Button) dialog.getDialogPane().lookupButton(save);
        saveButton.addEventFilter(ActionEvent.ACTION, action -> {
            if (username.getText().isBlank() || !email.getText().contains("@")) {
                validation.setText("Enter a username and a valid email address.");
                validation.getStyleClass().add("validation-error");
                action.consume();
            }
        });

        if (dialog.showAndWait().filter(save::equals).isPresent()) {
            String id = String.format("U-%03d", nextUserId.getAndIncrement());
            users.add(new UserRow(id, username.getText().trim(), email.getText().trim(),
                role.getValue(), "Active"));
            recordAction("User added", username.getText().trim());
            updateStats();
        }
    }

    @FXML
    private void deleteUser(ActionEvent event) {
        UserRow selected = usersTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showMessage(Alert.AlertType.INFORMATION, "Select a user",
                "Choose a user row before removing it from this preview.");
            return;
        }
        if (confirmRemoval("Remove user", "Remove " + selected.username()
            + " from this session preview?")) {
            users.remove(selected);
            recordAction("User removed", selected.username());
            updateStats();
        }
    }

    @FXML
    private void addAsset(ActionEvent event) {
        ComboBox<String> type = new ComboBox<>(
            FXCollections.observableArrayList("Movie", "Song"));
        type.setValue("Movie");
        type.setMaxWidth(Double.MAX_VALUE);
        TextField title = new TextField();
        title.setPromptText("Title");
        TextField details = new TextField();
        details.setPromptText("Genre and year, or artist and year");

        GridPane form = formGrid();
        addFormRow(form, 0, "Type", type);
        addFormRow(form, 1, "Title", title);
        addFormRow(form, 2, "Details", details);
        Label validation = new Label("Preview only; changes are not saved to the game catalog.");
        validation.getStyleClass().add("dialog-hint");

        Dialog<ButtonType> dialog = createDialog("Add asset", "Add movie or song preview data");
        dialog.getDialogPane().setContent(new VBox(12, form, validation));
        ButtonType save = new ButtonType("Add asset", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().setAll(save, ButtonType.CANCEL);
        dialog.setResultConverter(button -> button);
        Button saveButton = (Button) dialog.getDialogPane().lookupButton(save);
        saveButton.addEventFilter(ActionEvent.ACTION, action -> {
            if (title.getText().isBlank()) {
                validation.setText("Enter an asset title.");
                validation.getStyleClass().add("validation-error");
                action.consume();
            }
        });

        if (dialog.showAndWait().filter(save::equals).isPresent()) {
            String id = String.format("A-%03d", nextAssetId.getAndIncrement());
            assets.add(new AssetRow(id, title.getText().trim(), type.getValue(),
                details.getText().isBlank() ? "Details not set" : details.getText().trim(),
                "Preview"));
            recordAction("Asset added", title.getText().trim());
            updateStats();
        }
    }

    @FXML
    private void deleteAsset(ActionEvent event) {
        AssetRow selected = assetsTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showMessage(Alert.AlertType.INFORMATION, "Select an asset",
                "Choose an asset row before removing it from this preview.");
            return;
        }
        if (confirmRemoval("Remove asset", "Remove " + selected.title()
            + " from this session preview?")) {
            assets.remove(selected);
            recordAction("Asset removed", selected.title());
            updateStats();
        }
    }

    @FXML
    private void playGame(ActionEvent event) {
        try {
            Navigator.go(MainMenuGuestController.load());
        } catch (java.io.IOException exception) {
            showMessage(Alert.AlertType.ERROR, "Could not open GuessIt",
                exception.getMessage() == null ? "The player page could not be loaded."
                    : exception.getMessage());
        }
    }

    private void updateStats() {
        totalUsersLabel.setText(Integer.toString(users.size()));
        totalMoviesLabel.setText(Long.toString(assets.stream()
            .filter(asset -> asset.type().equals("Movie")).count()));
        totalSongsLabel.setText(Long.toString(assets.stream()
            .filter(asset -> asset.type().equals("Song")).count()));
        totalPlaysLabel.setText(Integer.toString(GameHistory.recent().size()));
    }

    private void recordAction(String action, String details) {
        logs.add(0, new LogRow(LocalDateTime.now().format(LOG_TIME), action, details));
        while (logs.size() > 100) {
            logs.remove(logs.size() - 1);
        }
    }

    private boolean confirmRemoval(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, message,
            ButtonType.CANCEL, new ButtonType("Remove", ButtonBar.ButtonData.OK_DONE));
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.initOwner(ownerWindow());
        return alert.showAndWait().filter(button -> button.getButtonData()
            == ButtonBar.ButtonData.OK_DONE).isPresent();
    }

    private void showMessage(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type, message, ButtonType.OK);
        alert.setTitle(title);
        alert.setHeaderText(null);
        Window owner = ownerWindow();
        if (owner != null) {
            alert.initOwner(owner);
        }
        alert.showAndWait();
    }

    private Dialog<ButtonType> createDialog(String title, String header) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle(title);
        dialog.setHeaderText(header);
        dialog.initOwner(ownerWindow());
        dialog.getDialogPane().getStylesheets().add(
            AdminController.class.getResource("/com/guessit/actualapp/admin/admin.css")
                .toExternalForm());
        return dialog;
    }

    private Window ownerWindow() {
        return pageHost.getScene() == null ? null : pageHost.getScene().getWindow();
    }

    private static GridPane formGrid() {
        GridPane grid = new GridPane();
        grid.setHgap(14);
        grid.setVgap(12);
        grid.setMinWidth(390);
        return grid;
    }

    private static void addFormRow(GridPane grid, int row, String label, javafx.scene.Node input) {
        Label fieldLabel = new Label(label);
        fieldLabel.getStyleClass().add("dialog-label");
        GridPane.setConstraints(fieldLabel, 0, row);
        GridPane.setConstraints(input, 1, row);
        javafx.scene.layout.ColumnConstraints labelColumn =
            grid.getColumnConstraints().isEmpty() ? new javafx.scene.layout.ColumnConstraints()
                : grid.getColumnConstraints().get(0);
        labelColumn.setMinWidth(86);
        if (grid.getColumnConstraints().isEmpty()) {
            grid.getColumnConstraints().add(labelColumn);
            javafx.scene.layout.ColumnConstraints inputColumn =
                new javafx.scene.layout.ColumnConstraints();
            inputColumn.setHgrow(javafx.scene.layout.Priority.ALWAYS);
            grid.getColumnConstraints().add(inputColumn);
        }
        if (input instanceof TextField field) {
            field.setMaxWidth(Double.MAX_VALUE);
        }
        grid.getChildren().addAll(fieldLabel, input);
    }

    private record UserRow(String id, String username, String email, String role, String status) {}
    private record AssetRow(String id, String title, String type, String details, String status) {}
    private record LogRow(String time, String action, String details) {}
}
