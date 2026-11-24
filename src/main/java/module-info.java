module com.example.javagroupproject {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.web;
    requires uk.co.caprica.vlcj.javafx;
    requires javafx.graphics;
    requires uk.co.caprica.vlcj;

    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires net.synedra.validatorfx;
    requires org.kordamp.ikonli.core;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.ikonli.feather;
    requires org.kordamp.bootstrapfx.core;
    requires eu.hansolo.tilesfx;
    requires com.almasb.fxgl.all;
    requires javafx.media;
    requires com.sun.jna;
    requires java.sql;
    requires atlantafx.base;
    requires animatefx;
    requires java.keyring;
    requires com.sun.jna.platform;
    requires java.management;

    opens com.example.javagroupproject to javafx.fxml;
    exports com.example.javagroupproject;

    // FIX: removed `opens com.guessit to javafx.fxml;`. No such package exists, and the
    // JVM refuses to start the app when the module descriptor names a missing package:
    //   java.lang.module.InvalidModuleDescriptorException: Package com.guessit not found in module
    // javac only warns about it, which is why the build "succeeded" but launch failed.

    // FIX: the new screen controllers (Controller.MainMenuController, ...) live in `Controller`.
    // Without opening the package, FXMLLoader cannot inject @FXML fields / call onAction methods:
    //   javafx.fxml.LoadException: module com.example.javagroupproject does not "opens Controller" to javafx.fxml
    opens Controller to javafx.fxml;

    // FIX: Controller.Main extends javafx.application.Application. javafx.graphics constructs
    // the Application class by reflection, so its package must be exported, otherwise launch
    // fails with: IllegalAccessException ... does not export Controller to module javafx.graphics
    exports Controller;

    // FIX: same reason as above for the older hand-written views under com.guessit.actualapp.view
    // (MainMenuGuest.fxml declares fx:controller in that package).
    opens com.guessit.actualapp.view to javafx.fxml;
}

