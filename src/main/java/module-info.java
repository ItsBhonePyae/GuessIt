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
    requires jbcrypt;

    opens com.example.javagroupproject to javafx.fxml;
    exports com.example.javagroupproject;

    exports Testing_for_bascially_everything.Login;
    opens Testing_for_bascially_everything.Login to javafx.fxml;

    opens com.guessit to javafx.fxml;
    opens com.guessit.login to javafx.fxml;
    opens com.guessit.controller to javafx.fxml;
    opens com.guessit.guessitauth to javafx.fxml;
    opens application to javafx.fxml;
    exports com.guessit to javafx.graphics;
    exports com.guessit.guessitauth to javafx.graphics;
    exports application to javafx.graphics;
}

