package application;

import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import databasePart1.DatabaseHelper;
import javafx.scene.control.Button;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import java.util.Random;

/**
 * This page displays a simple welcome message for the user.
 */
public class UserHomePage {

    private final DatabaseHelper databaseHelper; // Store DatabaseHelper instance

    public UserHomePage(DatabaseHelper databaseHelper) {
        this.databaseHelper = databaseHelper;
    }

    public void show(Stage primaryStage) {
        VBox layout = new VBox();
        layout.setStyle("-fx-alignment: center; -fx-padding: 20;");

        // Label to display Hello user
        Label userLabel = new Label("Hello, User!");
        userLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

        // Ganesh: Added input field for asking questions
        Label questionLabel = new Label("Enter your question:");
        TextField questionTextField = new TextField();
        questionTextField.setPromptText("Type your question here...");

        // Ganesh: Added submit button for posting questions
        Button submitQuestionButton = new Button("Submit Question");

        submitQuestionButton.setOnAction(event -> {
            String questionText = questionTextField.getText().trim();
            String currentUser = "Student1"; // Ganesh: Hardcoded username for now

            // Ganesh: Validate question before submission
            if (questionText.isEmpty()) {
                Alert alert = new Alert(AlertType.ERROR);
                alert.setTitle("Validation Error");
                alert.setHeaderText(null);
                alert.setContentText("Error: Question cannot be empty!");
                alert.showAndWait();
            } else {
                int questionId = generateId(); // Generate a unique ID
                
                // Ganesh: Insert question into database
                databaseHelper.insertQuestion(questionId, questionText, currentUser);

                Alert successAlert = new Alert(AlertType.INFORMATION);
                successAlert.setTitle("Success");
                successAlert.setHeaderText(null);
                successAlert.setContentText("Question submitted successfully!");
                successAlert.showAndWait();
                questionTextField.clear(); // Clear input field
            }
        });

        // Ganesh: Added view questions button
        Button viewQuestionsButton = new Button("View Questions");
        viewQuestionsButton.setOnAction(a -> new ViewQuestionsPage(databaseHelper).show(primaryStage));

        // Ganesh: Added logout button to allow users to log out and return to the login page
        Button logoutButton = new Button("Logout");
        logoutButton.setOnAction(a -> {
            new UserLoginPage(databaseHelper).show(primaryStage); // Pass DatabaseHelper instance
        });

        layout.getChildren().addAll(userLabel, questionLabel, questionTextField, submitQuestionButton, viewQuestionsButton, logoutButton);
        Scene userScene = new Scene(layout, 800, 400);

        // Set the scene to primary stage
        primaryStage.setScene(userScene);
        primaryStage.setTitle("User Page");
    }

    // Ganesh: Generates a random ID for questions (replace this with DB auto-increment if needed)
    private int generateId() {
        return new Random().nextInt(10000);
    }
}
