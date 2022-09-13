module com.app.wordleclone {
    requires javafx.controls;
    requires javafx.fxml;

    opens com.app.wordleclone to javafx.fxml;
    exports com.app.wordleclone;
}