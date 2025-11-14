module com.teris.tetris {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.media;
    requires java.desktop;
    requires transitive javafx.graphics;
    requires javafx.base;
    requires java.sql;
    requires jdk.httpserver;

    opens com.tetris to javafx.fxml;
    exports com.tetris;
}