package com.example.todo;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Rectangle2D;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.util.Pair;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class HelloApplication extends Application {
    public static class Task {

        private String Name;
        private String Desc;

        public String getDesc() {
            return Desc;
        }

        public String getName() {
            return Name;
        }

        Task(String Name, String Desc)
        {
            this.Name = Name;
            this.Desc = Desc;
        }
    }


    @Override
    public void start(Stage stage) throws IOException {



        //Windows size and stage settings
        Rectangle2D ScreenSize = Screen.getPrimary().getBounds();
        double width = ScreenSize.getWidth();
        double height = ScreenSize.getHeight();
        stage.setTitle("ToDo List");
        stage.setWidth(width*0.27);
        stage.setHeight(height*0.70);

        //Table
        TableView<Task> ToDoTable = new TableView<>();
        ToDoTable.setPlaceholder(new Label("No Tasks"));
        ToDoTable.setMinWidth(width*0.25);
        ToDoTable.setMinHeight(height*0.60);
        ToDoTable.setMaxWidth(width*0.25);
        ToDoTable.setMaxHeight(height*0.60);
        ToDoTable.autosize();

        //Table columns settings
        TableColumn<Task, String> TaskName = new TableColumn<>("Task Name");
        TableColumn<Task, String> Description = new TableColumn<>("Description");
        TaskName.setCellValueFactory(new PropertyValueFactory<>("Name"));
        TaskName.prefWidthProperty().bind(ToDoTable.widthProperty().multiply(0.3));
        Description.setCellValueFactory(new PropertyValueFactory<>("Desc"));
        Description.prefWidthProperty().bind(ToDoTable.widthProperty().multiply(0.7));

        //Data
        ObservableList<Task> data = FXCollections.observableArrayList();
        List<String[]> list = loadStrings("Task.bin");
        for (String [] strings:list
             ) {
            data.add(new Task(strings[0], strings[1]));
        }

        ToDoTable.setItems(data);
        ToDoTable.getColumns().addAll(TaskName,Description);

        //Add task Function
        Dialog AddTDialog = new Dialog();
        ButtonType AddOk = new ButtonType("Add", ButtonBar.ButtonData.OK_DONE);
        ButtonType AddNo = new ButtonType("Return", ButtonBar.ButtonData.CANCEL_CLOSE);
        AddTDialog.getDialogPane().getButtonTypes().add(AddOk);
        AddTDialog.getDialogPane().getButtonTypes().add(AddNo);
        AddTDialog.setTitle("Add Task");
        TextField AddN = new TextField("Task Name");
        TextField AddDesc = new TextField("Description");

        AddTDialog.getDialogPane().setContent(new VBox(8,AddN,AddDesc));

        AddTDialog.setResultConverter(dialogButton -> {
            if (dialogButton == AddOk && !(AddN.getText().trim().isEmpty()) && !(AddDesc.getText().trim().isEmpty())) {
                return new Pair<>(AddN.getText(), AddDesc.getText());
            }
            return null;
        });
        Button AddT = new Button("Add Task");

        AddT.setOnMouseClicked(e ->
        {
            Optional<Pair<String, String>> result = AddTDialog.showAndWait();
            result.ifPresent(pair -> {
                data.add(new Task(pair.getKey(), pair.getValue()));
                ToDoTable.setItems(data);

            });
        });

        //Delete task function
        Button DeleteT = new Button("Delete Task");
        DeleteT.setDisable(true);
        DeleteT.setOnMouseClicked(e ->{
            Task DelTask = ToDoTable.getSelectionModel().getSelectedItem();
            ToDoTable.getItems().remove(DelTask);
        });
        ToDoTable.getSelectionModel().selectedItemProperty().addListener((obs,oldSelection, newSelection)->
                DeleteT.setDisable(newSelection == null));

        //Edit task function

        Dialog EditTDialog = new Dialog();
        ButtonType EditOk = new ButtonType("Edit", ButtonBar.ButtonData.OK_DONE);
        ButtonType EditNo = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
        EditTDialog.getDialogPane().getButtonTypes().add(EditOk);
        EditTDialog.getDialogPane().getButtonTypes().add(EditNo);
        EditTDialog.setTitle("Edit Task");

        TextField EditN = new TextField();
        TextField EditDesc = new TextField();
        EditTDialog.setResultConverter(dialogButton -> {
            if (dialogButton == EditOk && !(EditN.getText().trim().isEmpty()) && !(EditDesc.getText().trim().isEmpty())) {
                return new Pair<>(EditN.getText(), EditDesc.getText());
            }
            return null;
        });

        Button EditT = new Button("Edit");
        EditT.setDisable(true);
        EditT.setOnMouseClicked(e ->
        {
            Task EditTask = ToDoTable.getSelectionModel().getSelectedItem();
            EditN.setText(EditTask.getName());
            EditDesc.setText(EditTask.getDesc());
            EditTDialog.getDialogPane().setContent(new VBox(8,EditN,EditDesc));
            Optional<Pair<String, String>> result = EditTDialog.showAndWait();
            result.ifPresent(pair -> {
                data.set(ToDoTable.getSelectionModel().getSelectedIndex(), new Task(pair.getKey(), pair.getValue()));
                ToDoTable.setItems(data);
            });

        });

        ToDoTable.getSelectionModel().selectedItemProperty().addListener((obs,oldSelection, newSelection)->
                EditT.setDisable(newSelection == null));

        //Delete all task function
        Button DelAllT = new Button("Delete All");

        DelAllT.setOnMouseClicked(e -> {
            data.clear();
            ToDoTable.setItems(data);
        });
        //Layout Set

        Scene MainScene = new Scene(new Group());
        GridPane gridButton = new GridPane();
        gridButton.add(AddT,0,0);
        gridButton.add(DeleteT,1,0);
        gridButton.add(EditT,2,0);
        gridButton.add(DelAllT,3,0);
        VBox vbox = new VBox();
        vbox.setSpacing(5);
        vbox.setPadding(new Insets(10, 0, 0, 10));
        vbox.getChildren().addAll( ToDoTable, gridButton);
        ((Group) MainScene.getRoot()).getChildren().addAll(vbox);

        stage.setScene(MainScene);
        stage.show();

        stage.setOnCloseRequest(windowEvent -> {
            try {
                clearFile("Task.bin");
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            for (Task datum : data) {
                try {
                    saveStrings(datum.getName(), datum.getDesc(), "Task.bin");
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
            Platform.exit();
            System.exit(0);
        });


    }
    public static void saveStrings(String string1, String string2, String filename) throws IOException {
        try (DataOutputStream dos = new DataOutputStream(new FileOutputStream(filename, true))) {
            dos.writeUTF(string1);
            dos.writeUTF(string2);
        }
    }

    public static void clearFile(String filename) throws IOException {
        File file = new File(filename);
        FileWriter writer = new FileWriter(file);
        writer.write("");
        writer.close();
    }
    public static List<String[]> loadStrings(String filename) throws IOException {
        List<String[]> pairs = new ArrayList<>();
        try (DataInputStream dis = new DataInputStream(new FileInputStream(filename))) {
            while (dis.available() > 0) {
                String[] pair = {dis.readUTF(), dis.readUTF()};
                pairs.add(pair);
            }
        }
        return pairs;
    }
    public static void main(String[] args) {
        launch();
    }
}