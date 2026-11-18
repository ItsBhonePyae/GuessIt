module com.guessit.actualapp {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.graphics;
    requires javafx.media;
    requires javafx.web;
    requires java.sql;

    requires mysql.connector.j;
    requires jbcrypt;
    requires atlantafx.base;
    requires org.kordamp.ikonli.core;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.ikonli.feather;

    opens com.guessit.actualapp to javafx.graphics, javafx.fxml;
    opens com.guessit.actualapp.controller to javafx.fxml;
    opens com.guessit.actualapp.model to javafx.base;

    exports com.guessit.actualapp;
    exports com.guessit.actualapp.model;
}
