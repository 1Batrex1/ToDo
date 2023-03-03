module com.example.todo {
    requires javafx.controls;
    requires javafx.fxml;


    opens com.example.todo to javafx.fxml;
    exports com.example.todo;
}