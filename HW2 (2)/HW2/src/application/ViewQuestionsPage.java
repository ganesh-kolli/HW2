package application;

import javafx.scene.Scene;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import databasePart1.DatabaseHelper;
import java.util.List;

/**
 * Displays a list of all questions in the database.
 */
public class ViewQuestionsPage {

    private final DatabaseHelper databaseHelper;

    public ViewQuestionsPage(DatabaseHelper databaseHelper) {
        this.databaseHelper = databaseHelper;
    }

    public void show(Stage primaryStage) {
        VBox layout = new VBox();
        layout.setStyle("-fx-alignment: center; -fx-padding: 20;");

        ListView<String> questionList = new ListView<>();

        // Ganesh: Fetch and display questions with real usernames
        List<String> questions = databaseHelper.getAllQuestions();
        questionList.getItems().addAll(questions);

        // Input field for editing
        TextField editField = new TextField();
        editField.setPromptText("Enter new text for selected question...");

        // Edit button
        Button editButton = new Button("Edit Question");
        editButton.setOnAction(e -> {
            String selected = questionList.getSelectionModel().getSelectedItem();
            if (selected != null && !editField.getText().trim().isEmpty()) {
                try {
                    int id = Integer.parseInt(selected.split(":")[0].trim()); // Extract ID safely
                    databaseHelper.updateQuestion(id, editField.getText().trim());
                    show(primaryStage); // Refresh page
                } catch (NumberFormatException ex) {
                    System.out.println("Error: Invalid question ID format.");
                }
            }
        });

        // Delete button
        Button deleteButton = new Button("Delete Question");
        deleteButton.setOnAction(e -> {
            String selected = questionList.getSelectionModel().getSelectedItem();
            if (selected != null) {
                try {
                    int id = Integer.parseInt(selected.split(":")[0].trim()); // Extract ID safely
                    databaseHelper.deleteQuestion(id);
                    show(primaryStage); // Refresh page
                } catch (NumberFormatException ex) {
                    System.out.println("Error: Invalid question ID format.");
                }
            }
        });

        // Back button
        Button backButton = new Button("Back");
        backButton.setOnAction(e -> new UserHomePage(databaseHelper).show(primaryStage));

        layout.getChildren().addAll(questionList, editField, editButton, deleteButton, backButton);
        Scene scene = new Scene(layout, 800, 400);
        primaryStage.setScene(scene);
        primaryStage.setTitle("View Questions");
    }
}

