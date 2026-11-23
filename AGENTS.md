# AGENTS.md — GuessIt (JavaGroupProject)

This is a JavaFX desktop application built with Maven in IntelliJ IDEA, using
SceneBuilder for FXML UI design and MySQL for the database.

## Tech stack

- **Java 16+** (run with a full JDK, e.g. `C:\Users\Lenovo\.jdks\liberica-full-26.0.2.1`
  — the IntelliJ-bundled JBR is missing `jdk.jsobject` and cannot run `javafx.web`)
- **JavaFX 21** via the `javafx-maven-plugin` — run with `mvnw javafx:run`
- **Maven** (`mvnw` / `mvnw.cmd` wrapper, no local install needed)
- **MySQL** via `com.mysql:mysql-connector-j`
- **jBCrypt** (`org.mindrot:jbcrypt`) for secure password hashing
- SceneBuilder for editing `.fxml` files

## Project structure

```
GuessIt/
├── AGENTS.md                      <- this file
├── pom.xml                        <- Maven config + dependencies
├── mvnw / mvnw.cmd                <- Maven wrapper
└── src/main/
    ├── java/
    │   ├── module-info.java
    │   └── com/guessit/           <- application code
    │       ├── Main.java          <- JavaFX entry point (Login screen)
    │       ├── login/             <- Login & Register section (controllers)
    │       ├── Controller/        <- other screen controllers
    │       ├── model/             <- plain data classes (POJOs)
    │       ├── dao/               <- database access, one DAO per model
    │       └── util/              <- DBUtil, PasswordUtil, helpers
    └── resources/
        ├── fxml/                  <- all .fxml views (SceneBuilder)
        ├── css/                   <- all .css stylesheets
        └── images/                <- images used by the views
```

## Rules

1. **All `.fxml` and `.css` files live in `src/main/resources`** — never in
   `src/main/java`. Controllers load them by absolute path, e.g.
   `getClass().getResource("/fxml/login.fxml")`.
2. **Controllers never run SQL.** A Controller calls a DAO; the DAO uses
   `DBUtil` for its `Connection`.
3. **Passwords are never stored in plain text.** Hash with jBCrypt
   (`BCrypt.hashpw(...)`) on register, check with `BCrypt.checkpw(...)` on login.
4. **FXML Controller packages must be opened** in `module-info.java`, e.g.
   `opens com.guessit.login to javafx.fxml;`
5. Models are plain POJOs — no JavaFX imports, no JDBC imports.

## Key dependencies (pom.xml)

| Dependency | Purpose |
|---|---|
| `com.mysql:mysql-connector-j:9.3.0` | MySQL JDBC driver |
| `org.mindrot:jbcrypt:0.4` | secure password hashing |
| `org.openjfx:javafx-*:21.0.6` | UI toolkit |
| `uk.co.caprica.vlcj` / `vlcj-javafx` | embedded video playback (game clips) |
| AtlantaFX, AnimateFX, Ikonli, ControlsFX | theming / animation / icons |

## Running

```bash
# terminal (JAVA_HOME must point to a full JDK)
.\mvnw.cmd javafx:run
```

or in IntelliJ: Maven panel → `JavaGroupProject` → Plugins → `javafx` → `javafx:run`.
