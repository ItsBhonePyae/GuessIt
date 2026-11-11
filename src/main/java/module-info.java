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
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.bootstrapfx.core;
    requires eu.hansolo.tilesfx;
    requires com.almasb.fxgl.all;
    requires javafx.media;
    requires com.sun.jna;
    requires java.sql;
    requires atlantafx.base;

    opens com.example.javagroupproject to javafx.fxml;
    opens Testing_for_bascially_everything to javafx.graphics;
    exports com.example.javagroupproject;
}