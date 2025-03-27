package application;

import databasePart1.DatabaseHelper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import java.sql.SQLException;
import java.util.List;
import java.util.ArrayList;


public class StudentHomePage {

    private DatabaseHelper databaseHelper;
    // Shared list to store unresolved questions.
    ObservableList<String> unresolvedQuestionsList = FXCollections.observableArrayList();
 // List to store all asked questions and, if answered, their answer.
    ObservableList<String> allQuestionsList = FXCollections.observableArrayList();
    // List of questions marked as resolved.
    public ObservableList<String> resolvedQuestionsList = FXCollections.observableArrayList();
    // List to store all answers.
    ObservableList<String> answersList = FXCollections.observableArrayList();
    // List to store follow-up questions.
    ObservableList<String> followUpQuestionsList = FXCollections.observableArrayList();

    // Counter for sequentially numbering questions.
    private int questionCounter = 1;

    // Constructor that accepts a DatabaseHelper instance.
    public StudentHomePage(DatabaseHelper databaseHelper) {
        this.databaseHelper = databaseHelper;
        // Refresh the Q&A views whenever answersList changes.
        answersList.addListener((javafx.collections.ListChangeListener.Change<? extends String> change) -> {
        });
    }
 // Helper method that returns a combined string of a question and its answers (for search purposes)
    private String combineQA(String question) {
        StringBuilder sb = new StringBuilder();
        sb.append(question).append("\n");
        for (String answer : answersList) {
            String prefix = question + " | Answer: ";
            if (answer.startsWith(prefix)) {
                sb.append("- ").append(answer.substring(prefix.length())).append("\n");
                // Append follow-ups from unresolvedQuestionsList if needed.
            }
        }
        return sb.toString().trim();
    }
    
    // The show method creates the UI and displays the Student Home Page on the given Stage.
    public void show(Stage stage) {
        // Connect to the database.
        try {
            databaseHelper.connectToDatabase();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        // Create a TabPane for the different student tasks.
        TabPane tabPane = new TabPane();
        tabPane.getTabs().addAll(
            createAskQuestionTab(),
            createUnresolvedQuestionsTab(),
            createAnswerQuestionTab(),
            createAllQATab(),
            createSearchTab(),
            createDeleteTab(), // Delete Q&A tab
            createEditQATab(), // Edit Q&A tab
            createResolvedQuestionsTab(),
            createFollowUpTab()
        );
        
        // Set up the overall layout using a BorderPane.
        BorderPane root = new BorderPane();
        root.setPadding(new Insets(20));
        
        // Top: Header label.
        Label headerLabel = new Label("Student Home Page");
        headerLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold;");
        root.setTop(headerLabel);
        BorderPane.setAlignment(headerLabel, Pos.CENTER);
        
        // Center: The TabPane.
        root.setCenter(tabPane);
        
        // Bottom: A simple exit button.
        Button exitButton = new Button("Exit");
        exitButton.setOnAction(e -> stage.close());
        HBox bottomBox = new HBox(exitButton);
        bottomBox.setAlignment(Pos.CENTER);
        bottomBox.setPadding(new Insets(10, 0, 0, 0));
        root.setBottom(bottomBox);
        
        Scene scene = new Scene(root, 1000, 600);
        stage.setScene(scene);
        stage.setTitle("Student Home Page");
        stage.show();
    }
    
    // Tab 1: Ask a Question and Receive Answers.
    private Tab createAskQuestionTab() {
        Tab tab = new Tab("Ask Question");
        VBox layout = new VBox(10);
        layout.setPadding(new Insets(10));
        layout.setAlignment(Pos.CENTER_LEFT);

        Label instruction = new Label("Enter your question:");
        TextField questionField = new TextField();
        questionField.setPromptText("Type your question here...");
        Button submitButton = new Button("Submit Question");
        Label feedbackLabel = new Label();

        submitButton.setOnAction(e -> {
            String questionText = questionField.getText().trim();
            if (questionText.isEmpty()) {
                feedbackLabel.setText("Question cannot be empty.");
            } else {
                int id;
                try {
                    // Insert the question into the database and get the generated id.
                    id = databaseHelper.insertQuestion(questionText, "student");
                } catch (SQLException ex) {
                    ex.printStackTrace();
                    feedbackLabel.setText("Error submitting question to the database.");
                    return;
                }
                
                String baseQuestion = "Q" + id + ": " + questionText;
                feedbackLabel.setText("Question submitted: " + baseQuestion);
                questionField.clear();
                unresolvedQuestionsList.add(baseQuestion);
                allQuestionsList.add(baseQuestion);
            }
        });

        layout.getChildren().addAll(instruction, questionField, submitButton, feedbackLabel);
        tab.setContent(layout);
        tab.setClosable(false);
        return tab;
    }

    
 // Tab 2: View Unresolved Questions and Their Answers.
    private Tab createUnresolvedQuestionsTab() {
        Tab tab = new Tab("Unresolved Questions");
        VBox layout = new VBox(10);
        layout.setPadding(new Insets(10));

        Label header = new Label("List of Unresolved Questions:");
        ListView<String> questionListView = new ListView<>(unresolvedQuestionsList);
        // Double-click to open the thread dialog.
        questionListView.setOnMouseClicked(e -> {
            if (e.getClickCount() == 2) {
                String selectedQuestion = questionListView.getSelectionModel().getSelectedItem();
                if (selectedQuestion != null) {
                    openQuestionThreadDialog(selectedQuestion, true);
                }
            }
        });

        layout.getChildren().addAll(header, questionListView);
        tab.setContent(layout);
        tab.setClosable(false);
        return tab;
    }
    // Opens a dialog with a nested Accordion (TreeView-like) for the selected question thread.
    private void openQuestionThreadDialog(String selectedQuestion, boolean canResolve) {
        Stage dialog = new Stage();
        dialog.setTitle("Question Thread: " + selectedQuestion);

        // Parse question ID and text.
        String[] parts = selectedQuestion.split(": ", 2);
        String questionID = (parts.length > 0) ? parts[0].trim() : "Q?";
        String questionText = (parts.length > 1) ? parts[1].trim() : "";

        VBox root = new VBox(10);
        root.setPadding(new Insets(15));

        Label headingLabel = new Label("Question " + questionID + ": " + questionText);
        headingLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
        root.getChildren().add(headingLabel);

        // Top-level Accordion for main answers.
        Accordion answersAccordion = new Accordion();

        // Retrieve raw entries (merged answer lines) for this question.
        List<String> rawEntries = getRawEntriesForQuestion(selectedQuestion);
        for (String raw : rawEntries) {
            // Split the raw entry into segments.
            String[] segments = raw.split("\\|");
            if (segments.length < 2) continue;
            // The second segment is the main answer.
            String mainAnswerText = segments[1].trim().replace("Answer:", "").trim();
            TitledPane mainAnswerPane = new TitledPane(mainAnswerText, null);

            VBox mainAnswerContent = new VBox(10);
            mainAnswerContent.setPadding(new Insets(10));

            // Nested Accordion for follow-up questions.
            Accordion followUpAccordion = new Accordion();

            // Parse remaining segments in pairs: "Follow-Up: ..." then "Answer: ..." (if present)
            int idx = 2;
            while (idx < segments.length) {
                String seg = segments[idx].trim();
                if (seg.startsWith("Follow-Up:")) {
                    String followUpQ = seg.replace("Follow-Up:", "").trim();
                    // Collect follow-up answers for this follow-up question.
                    List<String> followUpAnswers = new ArrayList<>();
                    idx++;
                    while (idx < segments.length) {
                        String nextSeg = segments[idx].trim();
                        if (nextSeg.startsWith("Follow-Up:")) {
                            idx--;
                            break;
                        } else if (nextSeg.startsWith("Answer:")) {
                            String ans = nextSeg.replace("Answer:", "").trim();
                            followUpAnswers.add(ans);
                        } else {
                            idx--;
                            break;
                        }
                        idx++;
                    }
                    TitledPane followUpPane = new TitledPane(followUpQ, null);
                    VBox followUpContent = new VBox(5);
                    followUpContent.setPadding(new Insets(10));
                    for (String fAns : followUpAnswers) {
                        Label lbl = new Label(fAns);
                        followUpContent.getChildren().add(lbl);
                    }
                    followUpPane.setContent(followUpContent);
                    followUpAccordion.getPanes().add(followUpPane);
                }
                idx++;
            }

            mainAnswerContent.getChildren().add(followUpAccordion);
            mainAnswerPane.setContent(mainAnswerContent);
            answersAccordion.getPanes().add(mainAnswerPane);
        }

        // Add "Mark as Resolved" button only if allowed.
        if (canResolve) {
            Button resolveButton = new Button("Mark as Resolved");
            resolveButton.setOnAction(e -> {
                resolvedQuestionsList.add(selectedQuestion);
                unresolvedQuestionsList.remove(selectedQuestion);
                dialog.close();
            });
            root.getChildren().add(resolveButton);
        }

        root.getChildren().add(answersAccordion);

        Scene scene = new Scene(root, 600, 600);
        dialog.setScene(scene);
        dialog.show();
    }
    // Retrieves raw merged entries for the given question from answersList.
    private List<String> getRawEntriesForQuestion(String question) {
        List<String> results = new ArrayList<>();
        int colonIndex = question.indexOf(":");
        if (colonIndex < 0) return results;
        String qidKey = question.substring(0, colonIndex).trim();  // e.g., "Q1"
        for (String ans : answersList) {
            if (ans.startsWith(qidKey)) {
                results.add(ans);
            }
        }
        for (String followUpEntry : followUpQuestionsList) {
            if (followUpEntry.startsWith(qidKey)) {
                results.add(followUpEntry);
            }
        }
        return results;
    }



    // Tab 3: Answer a Selected Question.
    private Tab createAnswerQuestionTab() {
        Tab tab = new Tab("Answer a Question");
        VBox layout = new VBox(10);
        layout.setPadding(new Insets(10));

        Label instruction = new Label("Select a question to answer:");
        ComboBox<String> questionCombo = new ComboBox<>(unresolvedQuestionsList);
        
        Label orLabel = new Label("OR");
        orLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");
        
        Label followUpInstruction = new Label("Select a follow-up question to answer:");
        ComboBox<String> followUpCombo = new ComboBox<>();
        
        Label linkedAnswerLabel = new Label("Related Answer:");
        linkedAnswerLabel.setStyle("-fx-font-size: 12px; -fx-font-weight: bold;");
        TextArea linkedAnswerArea = new TextArea();
        linkedAnswerArea.setWrapText(true);
        linkedAnswerArea.setEditable(false);
        linkedAnswerArea.setVisible(false);
        
        TextField answerField = new TextField();
        answerField.setPromptText("Type your answer here...");
        Button submitAnswerButton = new Button("Submit Answer");
        Label feedback = new Label();

        // Update follow-up dropdown when a new question is selected.
        questionCombo.setOnAction(e -> {
            String selectedQuestion = questionCombo.getValue();
            followUpCombo.getItems().clear();
            linkedAnswerArea.clear();
            linkedAnswerArea.setVisible(false);
            if (selectedQuestion != null) {
                for (String followUp : followUpQuestionsList) {
                    if (followUp.contains(selectedQuestion.trim()) && followUp.contains(" | Follow-Up: ")) {
                        followUpCombo.getItems().add(followUp);
                    }
                }
            }
        });

        // Show linked answer when a follow-up question is selected.
        followUpCombo.setOnAction(e -> {
            String selectedFollowUp = followUpCombo.getValue();
            if (selectedFollowUp != null) {
                String[] parts = selectedFollowUp.split(" \\| Follow-Up: ");
                if (parts.length > 1) {
                    String relatedAnswer = parts[0].replace(" | Answer:", "").trim();
                    linkedAnswerArea.setText(relatedAnswer);
                    linkedAnswerArea.setVisible(true);
                }
            }
        });

        submitAnswerButton.setOnAction(e -> {
            String mainQuestion = questionCombo.getValue();
            String followUpQuestion = followUpCombo.getValue();
            String answerText = answerField.getText().trim();
            String username = DatabaseHelper.getCurrentUsername(); // Fetch username dynamically

            if ((mainQuestion == null && followUpQuestion == null) || answerText.isEmpty()) {
                feedback.setText("Please select a question (or follow-up) and provide an answer.");
            } else {
                if (followUpQuestion != null) {
                    // Answering a follow-up: remove old merged entry, add updated merged entry.
                    answersList.remove(followUpQuestion);
                    String mergedEntry = followUpQuestion + " | Answer: " + answerText + " by " + username;
                    answersList.add(mergedEntry);
                    followUpQuestionsList.remove(followUpQuestion);
                } else {
                    // Answering a main question.
                    // Extract the question id from the main question text. (Format: "Q{id}: question text")
                    int colonIndex = mainQuestion.indexOf(":");
                    int questionId = Integer.parseInt(mainQuestion.substring(1, colonIndex).trim());
                    try {
                        // Insert the answer into the database.
                        databaseHelper.insertAnswer(questionId, username, answerText);
                        // Also update your in-memory list.
                        answersList.add(mainQuestion + " | Answer: " + answerText + " by " + username);
                    } catch (SQLException ex) {
                        ex.printStackTrace();
                        feedback.setText("Error inserting answer into the database.");
                        return;
                    }
                }
                feedback.setText("Answer submitted successfully!");
                answerField.clear();
                followUpCombo.getItems().clear();
                linkedAnswerArea.clear();
                linkedAnswerArea.setVisible(false);
            }
        });


        layout.getChildren().addAll(instruction, questionCombo, orLabel, followUpInstruction, followUpCombo, linkedAnswerLabel, linkedAnswerArea, answerField, submitAnswerButton, feedback);
        tab.setContent(layout);
        tab.setClosable(false);
        return tab;
    }

    // Tab 4: All Q&A.
    private Tab createAllQATab() {
        Tab tab = new Tab("All Q&A");
        VBox layout = new VBox(10);
        layout.setPadding(new Insets(10));

        Label header = new Label("All Asked Questions and Their Answers:");
        ListView<String> allQAListView = new ListView<>(allQuestionsList);
        allQAListView.setOnMouseClicked(e -> {
            if (e.getClickCount() == 2) {
                String selectedQuestion = allQAListView.getSelectionModel().getSelectedItem();
                if (selectedQuestion != null) {
                    openQuestionThreadDialog(selectedQuestion, false);
                }
            }
        });

        layout.getChildren().addAll(header, allQAListView);
        tab.setContent(layout);
        tab.setClosable(false);
        return tab;
    }

   
    
    // Tab 5: Search for Questions in All Q&A (Answered and Unanswered)
    private Tab createSearchTab() {
        Tab tab = new Tab("Search");
        VBox layout = new VBox(10);
        layout.setPadding(new Insets(10));
        
        Label instruction = new Label("Enter a keyword to search for questions:");
        TextField keywordField = new TextField();
        keywordField.setPromptText("Enter keyword...");
        Button searchButton = new Button("Search");
        TextArea searchResults = new TextArea();
        searchResults.setEditable(false);
        searchResults.setWrapText(true);
        
        searchButton.setOnAction(e -> {
            String keyword = keywordField.getText().trim().toLowerCase();
            if (keyword.isEmpty()) {
                searchResults.setText("Please enter a keyword.");
            } else {
                StringBuilder results = new StringBuilder();
                results.append("Search Results for '").append(keyword).append("':\n");
                boolean found = false;
                for (String entry : allQuestionsList) {
                    if (entry.toLowerCase().contains(keyword)) {
                        results.append(entry).append("\n");
                        found = true;
                    }
                }
                if (!found) {
                    results.append("No matching questions found.\n");
                }
                searchResults.setText(results.toString());
            }
        });
        
        layout.getChildren().addAll(instruction, keywordField, searchButton, searchResults);
        tab.setContent(layout);
        return tab;
    }
    
    
    // Tab 6: Delete Q&A.
    private Tab createDeleteTab() {
        Tab tab = new Tab("Delete Q&A");
        VBox layout = new VBox(10);
        layout.setPadding(new Insets(10));

        Label header = new Label("Select a question to delete:");
        ListView<String> deleteListView = new ListView<>(allQuestionsList);
        Button deleteButton = new Button("Delete Selected");
        Label feedbackLabel = new Label();

        deleteListView.setOnMouseClicked(e -> {
            if (e.getClickCount() == 2) {
                String selectedQuestion = deleteListView.getSelectionModel().getSelectedItem();
                if (selectedQuestion != null) {
                    openQuestionThreadDialog(selectedQuestion, false);
                }
            }
        });

        deleteButton.setOnAction(e -> {
            String selectedQuestion = deleteListView.getSelectionModel().getSelectedItem();
            if (selectedQuestion != null) {
                allQuestionsList.remove(selectedQuestion);
                unresolvedQuestionsList.remove(selectedQuestion);
                resolvedQuestionsList.remove(selectedQuestion);
                answersList.removeIf(answer -> answer.startsWith(selectedQuestion + " | Answer: "));
                feedbackLabel.setText("Deleted: " + selectedQuestion);
            } else {
                feedbackLabel.setText("Please select a question to delete.");
            }
        });

        layout.getChildren().addAll(header, deleteListView, deleteButton, feedbackLabel);
        tab.setContent(layout);
        tab.setClosable(false);
        return tab;
    }

    // Tab 7: Resolved Questions.
    private Tab createResolvedQuestionsTab() {
        Tab tab = new Tab("Resolved Questions");
        VBox layout = new VBox(10);
        layout.setPadding(new Insets(10));

        Label header = new Label("List of Resolved Questions:");
        ListView<String> resolvedListView = new ListView<>(resolvedQuestionsList);
        resolvedListView.setOnMouseClicked(e -> {
            if (e.getClickCount() == 2) {
                String selectedQuestion = resolvedListView.getSelectionModel().getSelectedItem();
                if (selectedQuestion != null) {
                    openQuestionThreadDialog(selectedQuestion, false);
                }
            }
        });

        layout.getChildren().addAll(header, resolvedListView);
        tab.setContent(layout);
        tab.setClosable(false);
        return tab;
    }

    // Tab 8: Ask a Follow-Up Question.
    private Tab createFollowUpTab() {
        Tab tab = new Tab("Ask a Follow-Up Question");
        VBox layout = new VBox(10);
        layout.setPadding(new Insets(10));

        Label instruction = new Label("Select an answer and ask a follow-up question");
        ComboBox<String> answeredQuestionCombo = new ComboBox<>();
        TextField followUpQuestionField = new TextField();
        followUpQuestionField.setPromptText("Type your follow-up question here");
        Button submitFollowUpButton = new Button("Submit Question");
        Label feedbackLabel = new Label();

        // Populate the ComboBox on mouse click with current answers in answersList.
        answeredQuestionCombo.setOnMouseClicked(e -> {
            answeredQuestionCombo.getItems().clear();
            for (String answer : answersList) {
                answeredQuestionCombo.getItems().add(answer);
            }
        });

        submitFollowUpButton.setOnAction(e -> {
            String selectedAnswer = answeredQuestionCombo.getValue();
            String followUpText = followUpQuestionField.getText().trim();
            if (selectedAnswer == null || followUpText.isEmpty()) {
                feedbackLabel.setText("Please select an answer and enter a follow-up question.");
            } else {
                // Remove the old answer from answersList.
                answersList.remove(selectedAnswer);
                // Create a new merged entry that includes the follow-up question.
                String mergedEntry = selectedAnswer + " | Follow-Up: " + followUpText;
                // Add the merged entry to followUpQuestionsList.
                followUpQuestionsList.add(mergedEntry);
                feedbackLabel.setText("Follow-up question submitted successfully (merged with original answer).");
                followUpQuestionField.clear();
            }
        });

        layout.getChildren().addAll(
            instruction,
            answeredQuestionCombo,
            followUpQuestionField,
            submitFollowUpButton,
            feedbackLabel
        );

        tab.setContent(layout);
        tab.setClosable(false);
        return tab;
    }
    
    // Tab 7: Edit Q&A
    private Tab createEditQATab() {
        Tab tab = new Tab("Edit Q&A");
        VBox layout = new VBox(10);
        layout.setStyle("-fx-padding: 20; -fx-alignment: center;");

        Label titleLabel = new Label("Edit Your Questions & Answers");
        titleLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

        // Separate ListViews for Questions and Answers
        ListView<String> questionList = new ListView<>();
        ListView<String> answerList = new ListView<>();

        // TextArea for editing
        TextArea editField = new TextArea();
        editField.setPromptText("Edit your selected question or answer here...");
        editField.setWrapText(true);

        Button updateButton = new Button("Update");
        Label statusLabel = new Label();

        // Load student's questions and answers from the database
        try {
            List<String[]> questions = databaseHelper.getAllQuestions();
            for (String[] question : questions) {
                questionList.getItems().add("Q" + question[0] + ": " + question[1]);
            }

            List<String[]> answers = databaseHelper.getAllAnswers();
            for (String[] answer : answers) {
                answerList.getItems().add("A" + answer[0] + ": " + answer[1]);
            }
        } catch (SQLException e) {
            statusLabel.setText("Error retrieving data.");
            e.printStackTrace();
        }

        // When selecting an item, display its text in the edit field.
        questionList.setOnMouseClicked(event -> {
            answerList.getSelectionModel().clearSelection();
            String selected = questionList.getSelectionModel().getSelectedItem();
            if (selected != null) {
                editField.setText(selected.substring(selected.indexOf(":") + 2));
            }
        });

        answerList.setOnMouseClicked(event -> {
            questionList.getSelectionModel().clearSelection();
            String selected = answerList.getSelectionModel().getSelectedItem();
            if (selected != null) {
                editField.setText(selected.substring(selected.indexOf(":") + 2));
            }
        });

        // Handle update action for editing question or answer.
        updateButton.setOnAction(e -> {
            String selectedQuestion = questionList.getSelectionModel().getSelectedItem();
            String selectedAnswer = answerList.getSelectionModel().getSelectedItem();
            String newText = editField.getText().trim();

            if (newText.isEmpty()) {
                statusLabel.setText("Text cannot be empty.");
                return;
            }

            try {
                if (selectedQuestion != null) {
                    int id = Integer.parseInt(selectedQuestion.split(":")[0].substring(1));
                    databaseHelper.updateQuestion(id, newText);
                    int selectedIndex = questionList.getSelectionModel().getSelectedIndex();
                    String updatedQuestion = "Q" + id + ": " + newText;
                    questionList.getItems().set(selectedIndex, updatedQuestion);

                    // Update the shared allQuestionsList.
                    for (int i = 0; i < allQuestionsList.size(); i++) {
                        String entry = allQuestionsList.get(i);
                        if (entry.startsWith("Q" + id + ":")) {
                            int answerIndex = entry.indexOf(" | Answer:");
                            if (answerIndex != -1) {
                                allQuestionsList.set(i, updatedQuestion + entry.substring(answerIndex));
                            } else {
                                allQuestionsList.set(i, updatedQuestion);
                            }
                        }
                    }
                    // Also update unresolvedQuestionsList if present.
                    for (int i = 0; i < unresolvedQuestionsList.size(); i++) {
                        String entry = unresolvedQuestionsList.get(i);
                        if (entry.startsWith("Q" + id + ":")) {
                            unresolvedQuestionsList.set(i, updatedQuestion);
                        }
                    }
                    statusLabel.setText("Question updated successfully.");
                } else if (selectedAnswer != null) {
                    int answerId = Integer.parseInt(selectedAnswer.split(":")[0].substring(1));
                    databaseHelper.updateAnswer(answerId, newText);
                    int selectedIndex = answerList.getSelectionModel().getSelectedIndex();
                    String updatedAnswer = "A" + answerId + ": " + newText;
                    answerList.getItems().set(selectedIndex, updatedAnswer);

                    // Retrieve the associated question id for this answer.
                    int questionId = databaseHelper.getQuestionIdByAnswerId(answerId);
                    // Update the answer portion in the shared allQuestionsList.
                    for (int i = 0; i < allQuestionsList.size(); i++) {
                        String entry = allQuestionsList.get(i);
                        if (entry.startsWith("Q" + questionId + ":")) {
                            // Update or append the answer text.
                            int answerIdx = entry.indexOf(" | Answer:");
                            String questionPart = (answerIdx != -1) ? entry.substring(0, answerIdx) : entry;
                            allQuestionsList.set(i, questionPart + " | Answer: " + newText);
                        }
                    }
                    statusLabel.setText("Answer updated successfully.");
                } else {
                    statusLabel.setText("Please select an item to edit.");
                }
            } catch (SQLException | NumberFormatException ex) {
                statusLabel.setText("Error updating.");
                ex.printStackTrace();
            }
        });

        // Refresh button to reload questions and answers from the database (for the edit tab)
        Button refreshButton = new Button("Refresh");
        refreshButton.setOnAction(e -> {
            questionList.getItems().clear();
            answerList.getItems().clear();
            try {
                List<String[]> questions = databaseHelper.getAllQuestions();
                for (String[] question : questions) {
                    questionList.getItems().add("Q" + question[0] + ": " + question[1]);
                }
                List<String[]> answers = databaseHelper.getAllAnswers();
                for (String[] answer : answers) {
                    answerList.getItems().add("A" + answer[0] + ": " + answer[1]);
                }
            } catch (SQLException ex) {
                statusLabel.setText("Error retrieving data.");
                ex.printStackTrace();
            }
        });

        HBox listsContainer = new HBox(20, questionList, answerList);
        listsContainer.setAlignment(Pos.CENTER);

        layout.getChildren().addAll(titleLabel, listsContainer, editField, updateButton, refreshButton, statusLabel);
        tab.setContent(layout);
        return tab;
    }
}