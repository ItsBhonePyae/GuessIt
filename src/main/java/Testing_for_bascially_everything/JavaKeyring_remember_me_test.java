package Testing_for_bascially_everything;

import atlantafx.base.theme.PrimerDark;
import atlantafx.base.theme.Styles;
import com.github.javakeyring.BackendNotSupportedException;
import com.github.javakeyring.Keyring;
import com.github.javakeyring.KeyringStorageType;
import com.github.javakeyring.PasswordAccessException;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.Separator;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;
import org.kordamp.ikonli.feather.Feather;
import org.kordamp.ikonli.javafx.FontIcon;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * Java Keyring "remember me" demo.
 *
 * <p>Shows how to persist a login token in the operating system credential store
 * instead of a plain file. On Windows the token is written through
 * {@code com.github.javakeyring} into Windows Credential Manager, which means it
 * can be seen in Control Panel -&gt; Credential Manager -&gt;
 * Windows Credentials under the target name
 * {@code GuessIt.JavaGroupProject|remember-me}.</p>
 *
 * <p>The demo login is hard-coded: <b>user / 123</b>. No database is used.</p>
 *
 * <p>Flow:</p>
 * <ol>
 *   <li>First launch: username/password form is shown.</li>
 *   <li>Sign in with "Remember me" ticked: a signed token is stored in the
 *       credential store and the sample page opens.</li>
 *   <li>Next launch: the token is read, validated and the sample page opens
 *       directly (no form).</li>
 *   <li>Log out with "Keep me signed in on this device" unticked: the token is
 *       deleted, so the next launch shows the form again.</li>
 *   <li>Log out with it ticked: the token survives and the next launch
 *       auto-logs-in.</li>
 * </ol>
 *
 * <p>Control ids for UI automation: {@code username}, {@code password},
 * {@code rememberMe}, {@code signIn}, {@code loginError}, {@code samplePage},
 * {@code rememberAtLogout}, {@code logout}, {@code deleteToken},
 * {@code authState}, {@code tokenStatus}, {@code refreshToken}.</p>
 */
public class JavaKeyring_remember_me_test extends Application {

    /** Credential store service name. Override with -Dkeyring.demo.service=... for tests. */
    private static final String SERVICE =
        System.getProperty("keyring.demo.service", "GuessIt.JavaGroupProject");
    /** Credential store account name. */
    private static final String ACCOUNT = "remember-me";

    private static final String VALID_USER = "user";
    private static final String VALID_PASSWORD = "123";

    private static final String TOKEN_VERSION = "v1";
    private static final long TOKEN_LIFETIME_DAYS = 30;
    private static final String TOKEN_SEPARATOR = "|";

    private static final DateTimeFormatter DATE_FORMAT =
        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm").withZone(ZoneId.systemDefault());

    // ---------------------------------------------------------------- keyring

    private Keyring keyring;
    private String keyringProblem;
    private String backendLabel = "unavailable";

    // ------------------------------------------------------------------- ui

    private final StackPane content = new StackPane();

    private final Label authState = new Label();
    private final Label tokenStatus = new Label();

    private TextField usernameField;
    private PasswordField passwordField;
    private CheckBox rememberMe;
    private Label loginError;
    private Button signInButton;

    private CheckBox rememberAtLogout;

    private String authStateValue = "signed-out";
    private String lastTokenMessage = "";

    private Stage stage;

    // =============================================================== lifecycle

    @Override
    public void start(Stage stage) {
        this.stage = stage;
        Application.setUserAgentStylesheet(new PrimerDark().getUserAgentStylesheet());

        openKeyring();

        BorderPane root = new BorderPane();
        root.setCenter(content);
        root.setBottom(statusBar());

        stage.setTitle("GuessIt - Java Keyring remember-me demo");
        stage.setScene(new Scene(root, 1060, 760));
        stage.setMinWidth(900);
        stage.setMinHeight(640);
        stage.setOnCloseRequest(event -> closeKeyring());

        // Decide the first screen by reading the credential store.
        Token stored = readValidToken();
        if (stored != null) {
            authStateValue = "auto-login";
            showSamplePage("Auto-login: a valid remember-me token for '"
                + stored.username + "' was found in Windows Credential Manager. "
                + "It expires " + DATE_FORMAT.format(Instant.ofEpochMilli(stored.expiresAt)) + ".");
        } else {
            authStateValue = "signed-out";
            showLoginPage("Sign in to continue. The demo credentials are user / 123.");
        }
        updateStatus();
        stage.show();
    }

    private void openKeyring() {
        try {
            keyring = Keyring.create(KeyringStorageType.WINDOWS_CREDENTIAL_STORE);
            backendLabel = KeyringStorageType.WINDOWS_CREDENTIAL_STORE.name();
        } catch (BackendNotSupportedException windowsFailure) {
            // Fallback so the demo still explains itself on a non-Windows machine.
            try {
                keyring = Keyring.create();
                backendLabel = keyring.getKeyringStorageType() == null
                    ? "default" : keyring.getKeyringStorageType().name();
            } catch (BackendNotSupportedException | RuntimeException fallbackFailure) {
                keyringProblem = windowsFailure.getMessage();
                backendLabel = "unavailable (" + windowsFailure.getMessage() + ")";
            }
        } catch (RuntimeException | LinkageError failure) {
            keyringProblem = failure.getClass().getSimpleName() + ": " + failure.getMessage();
            backendLabel = "unavailable (" + keyringProblem + ")";
        }
    }

    private void closeKeyring() {
        if (keyring != null) {
            try {
                keyring.close();
            } catch (Exception ignored) {
                // Closing the Windows backend is a no-op; ignore anyway.
            }
            keyring = null;
        }
    }

    // ================================================================ login UI

    private void showLoginPage(String message) {
        usernameField = new TextField();
        usernameField.setId("username");
        usernameField.setPromptText("user");
        usernameField.setPrefHeight(38);

        passwordField = new PasswordField();
        passwordField.setId("password");
        passwordField.setPromptText("123");
        passwordField.setPrefHeight(38);

        rememberMe = new CheckBox("Remember me on this device");
        rememberMe.setId("rememberMe");

        loginError = new Label();
        loginError.setId("loginError");
        loginError.getStyleClass().addAll(Styles.TEXT_BOLD);
        loginError.setTextFill(Color.web("#f85149"));
        loginError.setWrapText(true);
        loginError.setManaged(false);
        loginError.setVisible(false);

        signInButton = new Button("Sign in", new FontIcon(Feather.LOG_IN));
        signInButton.setId("signIn");
        signInButton.setDefaultButton(true);
        signInButton.setMaxWidth(Double.MAX_VALUE);
        signInButton.setPrefHeight(40);
        signInButton.getStyleClass().add(Styles.ACCENT);
        signInButton.setOnAction(event -> signIn());
        passwordField.setOnAction(event -> signIn());
        usernameField.setOnAction(event -> passwordField.requestFocus());

        Button forget = new Button("Forget saved login");
        forget.setId("forgetOnLogin");
        forget.getStyleClass().addAll(Styles.SMALL, Styles.BUTTON_OUTLINED);
        forget.setOnAction(event -> {
            boolean removed = deleteStoredToken();
            lastTokenMessage = removed
                ? "Saved login removed from Windows Credential Manager."
                : "No saved login to remove (" + lastTokenMessage + ").";
            showLoginError(lastTokenMessage, removed ? "#3fb950" : null);
            updateStatus();
        });

        Label banner = new Label(message + "  " + lastTokenMessage);
        banner.setWrapText(true);
        banner.setMaxWidth(Double.MAX_VALUE);
        banner.setPadding(new Insets(10, 12, 10, 12));
        banner.getStyleClass().addAll(Styles.BG_SUBTLE, Styles.ROUNDED, Styles.TEXT_SMALL);

        VBox card = new VBox(14,
            brandRow("GuessIt", "Group project demo"),
            new Separator(),
            title("Sign in"),
            muted("This screen is the only way in when no token is stored."),
            banner,
            fieldLabel("Username", Feather.USER, usernameField),
            fieldLabel("Password", Feather.LOCK, passwordField),
            rememberMe,
            loginError,
            signInButton,
            rememberHint(),
            new Separator(),
            keyringInfoPane(),
            new HBox(8, forget)
        );
        card.setPadding(new Insets(28));
        card.setMaxWidth(460);
        card.setMinWidth(420);
        card.getStyleClass().add(Styles.BG_DEFAULT);
        card.setStyle("-fx-background-radius: 14; -fx-border-radius: 14;"
            + " -fx-border-color: -color-border-default; -fx-border-width: 1;");

        StackPane page = new StackPane(card);
        page.setId("loginPage");
        page.setPadding(new Insets(32));
        page.setStyle("-fx-background-color: -color-bg-subtle;");
        content.getChildren().setAll(page);

        usernameField.requestFocus();
    }

    private void signIn() {
        String user = usernameField.getText() == null ? "" : usernameField.getText().trim();
        String password = passwordField.getText() == null ? "" : passwordField.getText();

        if (!VALID_USER.equals(user) || !VALID_PASSWORD.equals(password)) {
            showLoginError("Invalid credentials. The demo account is user / 123.", null);
            return;
        }

        boolean remember = rememberMe.isSelected();
        if (remember) {
            Token token = writeToken();
            authStateValue = token == null ? "signed-in (token save failed)" : "signed-in (remembered)";
        } else {
            deleteStoredToken();
            authStateValue = "signed-in (session only)";
        }

        showSamplePage(remember
            ? "Signed in with 'Remember me' - the token was written to Windows Credential Manager. "
                + "Close the window and launch again to test auto-login."
            : "Signed in without 'Remember me' - any previous token was removed. "
                + "The next launch will ask for username and password.");
        updateStatus();
    }

    private void showLoginError(String message, String color) {
        if (loginError == null) {
            return;
        }
        loginError.setText(message);
        loginError.setTextFill(color == null ? Color.web("#f85149") : Color.web(color));
        loginError.setManaged(true);
        loginError.setVisible(true);
    }

    // =============================================================== sample UI

    private void showSamplePage(String message) {
        Token stored = readValidToken();
        boolean remembered = stored != null;

        HBox brand = brandRow("GuessIt", "Sample page");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Circle avatar = new Circle(17, Color.web("#4493f8"));
        Label initial = new Label("U");
        initial.setTextFill(Color.WHITE);
        initial.getStyleClass().add(Styles.TEXT_BOLD);
        StackPane avatarPane = new StackPane(avatar, initial);

        Label who = new Label("user");
        who.getStyleClass().add(Styles.TEXT_BOLD);

        Button headerLogout = new Button("Log out", new FontIcon(Feather.LOG_OUT));
        headerLogout.getStyleClass().addAll(Styles.SMALL, Styles.BUTTON_OUTLINED);
        headerLogout.setOnAction(event -> logout());

        HBox header = new HBox(10, brand, spacer, avatarPane, who, headerLogout);
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(18, 24, 18, 24));
        header.getStyleClass().add(Styles.BG_DEFAULT);

        Label welcome = title("Welcome back, user");
        Label subtitle = muted("You are seeing the protected sample page. "
            + "It is reachable with the demo credentials or with a valid remember-me token.");

        Label banner = new Label(message);
        banner.setWrapText(true);
        banner.setMaxWidth(Double.MAX_VALUE);
        banner.setPadding(new Insets(10, 12, 10, 12));
        banner.getStyleClass().addAll(Styles.BG_ACCENT_SUBTLE, Styles.ROUNDED, Styles.TEXT_SMALL);

        HBox stats = new HBox(14,
            statCard(Feather.BOOKMARK, "Watchlist", "12 titles", "#4493f8"),
            statCard(Feather.STAR, "Favourites", "5 titles", "#d29922"),
            statCard(Feather.CLOCK, "Watch time", "18h 40m", "#3fb950"));

        VBox continueWatching = cardBox(
            title4("Continue watching"),
            watchRow("Blade Runner 2049", "1h 12m left", 0.62, "#4493f8"),
            watchRow("Dune: Part Two", "42m left", 0.81, "#3fb950"),
            watchRow("Arrival", "Not started", 0.0, "#d29922"));

        rememberAtLogout = new CheckBox("Keep me signed in on this device after logout");
        rememberAtLogout.setId("rememberAtLogout");
        rememberAtLogout.setSelected(remembered);

        Label tokenLabel = new Label();
        tokenLabel.setId("tokenStatus");
        tokenLabel.setWrapText(true);
        tokenLabel.getStyleClass().add(Styles.TEXT_SMALL);
        tokenLabel.setText(describeToken());

        Button logout = new Button("Log out", new FontIcon(Feather.LOG_OUT));
        logout.setId("logout");
        logout.getStyleClass().add(Styles.ACCENT);
        logout.setOnAction(event -> logout());

        Button deleteToken = new Button("Delete saved token", new FontIcon(Feather.TRASH_2));
        deleteToken.setId("deleteToken");
        deleteToken.getStyleClass().addAll(Styles.BUTTON_OUTLINED);
        deleteToken.setOnAction(event -> {
            boolean removed = deleteStoredToken();
            rememberAtLogout.setSelected(false);
            tokenLabel.setText(describeToken());
            lastTokenMessage = removed
                ? "Saved token deleted from Windows Credential Manager."
                : "No saved token to delete.";
            updateStatus();
        });

        Button refresh = new Button("Refresh", new FontIcon(Feather.REFRESH_CW));
        refresh.setId("refreshToken");
        refresh.getStyleClass().addAll(Styles.SMALL, Styles.BUTTON_OUTLINED);
        refresh.setOnAction(event -> {
            tokenLabel.setText(describeToken());
            updateStatus();
        });

        VBox rememberCard = cardBox(
            title4("Remembered login"),
            muted("The token is stored in Windows Credential Manager, "
                + "not in a file or in the database."),
            new Separator(),
            rememberAtLogout,
            tokenLabel,
            new HBox(8, logout, deleteToken, refresh));

        VBox pageContent = new VBox(16, welcome, subtitle, banner, stats,
            continueWatching, rememberCard);
        pageContent.setPadding(new Insets(24));
        pageContent.setFillWidth(true);

        BorderPane page = new BorderPane();
        page.setId("samplePage");
        page.setTop(header);
        page.setCenter(pageContent);
        page.setStyle("-fx-background-color: -color-bg-subtle;");
        content.getChildren().setAll(page);
    }

    private void logout() {
        boolean keep = rememberAtLogout != null && rememberAtLogout.isSelected();

        if (keep) {
            Token existing = readValidToken();
            if (existing == null) {
                writeToken();
            } else {
                lastTokenMessage = "Kept the existing token; it expires "
                    + DATE_FORMAT.format(Instant.ofEpochMilli(existing.expiresAt)) + ".";
            }
            authStateValue = "signed-out (token kept)";
        } else {
            deleteStoredToken();
            authStateValue = "signed-out (token deleted)";
        }

        showLoginPage(keep
            ? "You logged out but kept the remember-me token, so the next launch will go straight to the sample page."
            : "You logged out and did not keep the token, so the next launch will ask for username and password.");
        updateStatus();
    }

    // ============================================================ token storage

    /** Writes a fresh token to the credential store. Returns null when it fails. */
    private Token writeToken() {
        if (keyring == null) {
            lastTokenMessage = "Keyring unavailable: " + keyringProblem;
            return null;
        }
        long expiresAt = System.currentTimeMillis() + TimeUnit.DAYS.toMillis(TOKEN_LIFETIME_DAYS);
        Token token = new Token(VALID_USER, UUID.randomUUID().toString(), expiresAt);
        try {
            keyring.setPassword(SERVICE, ACCOUNT, token.serialize());
            lastTokenMessage = "Token saved (service='" + SERVICE + "', account='" + ACCOUNT
                + "'), expires " + DATE_FORMAT.format(Instant.ofEpochMilli(expiresAt)) + ".";
            return token;
        } catch (PasswordAccessException | RuntimeException failure) {
            lastTokenMessage = "Could not save token: " + failure.getMessage();
            return null;
        }
    }

    /** Reads and validates the stored token. Returns null if missing, malformed or expired. */
    private Token readValidToken() {
        String raw = readRawToken();
        if (raw == null) {
            return null;
        }
        Token token = Token.parse(raw);
        if (token == null) {
            lastTokenMessage = "A credential exists but is not a GuessIt token; ignoring it.";
            return null;
        }
        if (token.isExpired()) {
            lastTokenMessage = "Stored token expired on "
                + DATE_FORMAT.format(Instant.ofEpochMilli(token.expiresAt)) + "; deleting it.";
            deleteStoredToken();
            return null;
        }
        lastTokenMessage = "Valid token read from Windows Credential Manager; expires "
            + DATE_FORMAT.format(Instant.ofEpochMilli(token.expiresAt)) + ".";
        return token;
    }

    /** Reads the raw credential. Returns null when nothing is stored. */
    private String readRawToken() {
        if (keyring == null) {
            lastTokenMessage = "Keyring unavailable: " + keyringProblem;
            return null;
        }
        try {
            return keyring.getPassword(SERVICE, ACCOUNT);
        } catch (PasswordAccessException | RuntimeException missing) {
            lastTokenMessage = "No saved token (" + missing.getMessage() + ").";
            return null;
        }
    }

    /** Deletes the credential. Returns true when something was removed. */
    private boolean deleteStoredToken() {
        if (keyring == null) {
            lastTokenMessage = "Keyring unavailable: " + keyringProblem;
            return false;
        }
        try {
            keyring.deletePassword(SERVICE, ACCOUNT);
            lastTokenMessage = "Token deleted from Windows Credential Manager.";
            return true;
        } catch (PasswordAccessException | RuntimeException missing) {
            lastTokenMessage = "Token was not stored (" + missing.getMessage() + ").";
            return false;
        }
    }

    private String describeToken() {
        Token token = readValidToken();
        if (token == null) {
            return "Credential store: no usable token. This launch will not auto-login again.";
        }
        return "Credential store: token present for '" + token.username + "', expires "
            + DATE_FORMAT.format(Instant.ofEpochMilli(token.expiresAt)) + ".";
    }

    // ================================================================= status

    private Node statusBar() {
        authState.setId("authState");
        tokenStatus.setId("tokenStatusBar");
        tokenStatus.setWrapText(true);
        tokenStatus.setMaxWidth(360);

        authState.getStyleClass().addAll(Styles.TEXT_SMALL, Styles.TEXT_BOLD);
        tokenStatus.getStyleClass().add(Styles.TEXT_SMALL);

        Button refresh = new Button("Refresh", new FontIcon(Feather.REFRESH_CW));
        refresh.setId("refreshStatus");
        refresh.getStyleClass().addAll(Styles.SMALL, Styles.BUTTON_OUTLINED);
        refresh.setOnAction(event -> updateStatus());

        Label backend = new Label("backend: " + backendLabel);
        backend.getStyleClass().addAll(Styles.TEXT_SMALL, Styles.TEXT_MUTED);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox row = new HBox(12, authState, tokenStatus, spacer, backend, refresh);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(8, 16, 8, 16));
        row.getStyleClass().add(Styles.BG_DEFAULT);

        return row;
    }

    private void updateStatus() {
        authState.setText("authState: " + authStateValue);
        String raw = readRawToken();
        Token parsed = raw == null ? null : Token.parse(raw);
        if (parsed == null) {
            tokenStatus.setText("token: none");
        } else if (parsed.isExpired()) {
            tokenStatus.setText("token: expired");
        } else {
            tokenStatus.setText("token: present (expires "
                + DATE_FORMAT.format(Instant.ofEpochMilli(parsed.expiresAt)) + ")");
        }
    }

    // ================================================================ helpers

    private static HBox brandRow(String name, String subtitle) {
        Label icon = new Label();
        icon.setGraphic(new FontIcon(Feather.FILM));
        icon.setTextFill(Color.web("#4493f8"));
        icon.setStyle("-fx-font-size: 22px;");
        Label title = new Label(name);
        title.getStyleClass().add(Styles.TITLE_3);
        Label sub = new Label(subtitle);
        sub.getStyleClass().addAll(Styles.TEXT_SMALL, Styles.TEXT_MUTED);
        VBox text = new VBox(1, title, sub);
        HBox box = new HBox(10, icon, text);
        box.setAlignment(Pos.CENTER_LEFT);
        return box;
    }

    private static Label title(String text) {
        Label label = new Label(text);
        label.getStyleClass().add(Styles.TITLE_2);
        return label;
    }

    private static Label title4(String text) {
        Label label = new Label(text);
        label.getStyleClass().add(Styles.TITLE_4);
        return label;
    }

    private static Label muted(String text) {
        Label label = new Label(text);
        label.setWrapText(true);
        label.getStyleClass().addAll(Styles.TEXT_MUTED, Styles.TEXT_SMALL);
        return label;
    }

    private static VBox fieldLabel(String text, Feather icon, Region field) {
        Label label = new Label(text, new FontIcon(icon));
        label.getStyleClass().addAll(Styles.TEXT_SMALL, Styles.TEXT_BOLD);
        VBox box = new VBox(5, label, field);
        box.setFillWidth(true);
        return box;
    }

    private static Node rememberHint() {
        Label hint = new Label("Remember me stores a 30-day token in the OS credential store. "
            + "The password itself is never written.");
        hint.setWrapText(true);
        hint.getStyleClass().addAll(Styles.TEXT_SMALL, Styles.TEXT_MUTED);
        return hint;
    }

    private Node keyringInfoPane() {
        Feather icon = keyring == null ? Feather.ALERT_TRIANGLE : Feather.SHIELD;
        Label label = new Label("", new FontIcon(icon));
        label.setWrapText(true);
        label.getStyleClass().addAll(Styles.TEXT_SMALL, Styles.TEXT_MUTED);
        label.setText(keyring == null
            ? "Keyring backend unavailable: " + keyringProblem
            : "Credential store: " + backendLabel
                + " | service '" + SERVICE + "' / account '" + ACCOUNT + "'");
        return label;
    }

    private static VBox cardBox(Node... children) {
        VBox box = new VBox(10, children);
        box.setPadding(new Insets(16, 18, 16, 18));
        box.getStyleClass().addAll(Styles.BG_DEFAULT, Styles.ROUNDED);
        box.setStyle("-fx-border-color: -color-border-default; -fx-border-width: 1;"
            + " -fx-border-radius: 10; -fx-background-radius: 10;");
        box.setMaxWidth(Double.MAX_VALUE);
        return box;
    }

    private static VBox statCard(Feather icon, String label, String value, String color) {
        Label iconLabel = new Label();
        iconLabel.setGraphic(new FontIcon(icon));
        iconLabel.setTextFill(Color.web(color));
        iconLabel.setStyle("-fx-font-size: 18px;");
        Label name = new Label(label);
        name.getStyleClass().addAll(Styles.TEXT_SMALL, Styles.TEXT_MUTED);
        Label amount = new Label(value);
        amount.getStyleClass().add(Styles.TITLE_4);
        VBox box = new VBox(4, iconLabel, name, amount);
        box.setPadding(new Insets(14, 16, 14, 16));
        box.getStyleClass().addAll(Styles.BG_DEFAULT, Styles.ROUNDED);
        box.setStyle("-fx-border-color: -color-border-default; -fx-border-width: 1;"
            + " -fx-border-radius: 10; -fx-background-radius: 10;");
        HBox.setHgrow(box, Priority.ALWAYS);
        box.setMaxWidth(Double.MAX_VALUE);
        return box;
    }

    private static HBox watchRow(String title, String subtitle, double progress, String color) {
        Label name = new Label(title);
        name.getStyleClass().add(Styles.TEXT_BOLD);
        Label meta = new Label(subtitle);
        meta.getStyleClass().addAll(Styles.TEXT_SMALL, Styles.TEXT_MUTED);
        VBox text = new VBox(1, name, meta);

        ProgressBar bar = new ProgressBar(progress);
        bar.setPrefWidth(160);
        bar.setStyle("-fx-accent: " + color + ";");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox row = new HBox(12, new FontIcon(Feather.FILM), text, spacer, bar);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(8, 4, 8, 4));
        return row;
    }

    // ================================================================== token

    /** Serialized as {@code v1|user|<uuid>|<expiresAtEpochMillis>}. */
    private static final class Token {

        final String username;
        final String id;
        final long expiresAt;

        Token(String username, String id, long expiresAt) {
            this.username = username;
            this.id = id;
            this.expiresAt = expiresAt;
        }

        String serialize() {
            return TOKEN_VERSION + TOKEN_SEPARATOR + username + TOKEN_SEPARATOR
                + id + TOKEN_SEPARATOR + expiresAt;
        }

        boolean isExpired() {
            return System.currentTimeMillis() >= expiresAt;
        }

        static Token parse(String raw) {
            if (raw == null) {
                return null;
            }
            String[] parts = raw.split("\\|", -1);
            if (parts.length != 4 || !TOKEN_VERSION.equals(parts[0]) || parts[1].isEmpty()) {
                return null;
            }
            try {
                return new Token(parts[1], parts[2], Long.parseLong(parts[3]));
            } catch (NumberFormatException invalidExpiry) {
                return null;
            }
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}