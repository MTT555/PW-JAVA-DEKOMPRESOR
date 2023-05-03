module com.example.dekompresorgui {
    requires javafx.controls;
    requires javafx.fxml;


    opens com.example.dekompresorgui to javafx.fxml;
    exports com.example.dekompresorgui;
}