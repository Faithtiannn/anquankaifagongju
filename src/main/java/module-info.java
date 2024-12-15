module com.example.test5 {
    requires javafx.controls;
    requires javafx.fxml;


    opens com.example.test5 to javafx.fxml;
    exports com.example.test5;
}