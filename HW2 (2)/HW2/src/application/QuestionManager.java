package application;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import databasePart1.DatabaseHelper;

public class QuestionManager {
    private final DatabaseHelper databaseHelper;

    public QuestionManager(DatabaseHelper databaseHelper) {
        this.databaseHelper = databaseHelper;
    }

    // Ganesh: Fetch all questions from the database
    public ArrayList<Question> getAllQuestions() {
        ArrayList<Question> questions = new ArrayList<>();
        String query = "SELECT * FROM questions";

        try (Connection conn = databaseHelper.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                int id = rs.getInt("id");
                String text = rs.getString("text");
                String author = rs.getString("author");
                questions.add(new Question(id, text, author));
            }

        } catch (SQLException e) {
            System.out.println("Error fetching questions: " + e.getMessage());
        }
        return questions;
    }

    // Ganesh: Update an existing question
    public void updateQuestion(int id, String newText) {
        String sql = "UPDATE questions SET text = ? WHERE id = ?";
        try (PreparedStatement pstmt = databaseHelper.getConnection().prepareStatement(sql)) {
            pstmt.setString(1, newText);
            pstmt.setInt(2, id);
            pstmt.executeUpdate();
            System.out.println("Question updated successfully!");
        } catch (SQLException e) {
            System.out.println("Error updating question: " + e.getMessage());
        }
    }

    // Ganesh: Delete a question
    public void deleteQuestion(int id) {
        String sql = "DELETE FROM questions WHERE id = ?";
        try (PreparedStatement pstmt = databaseHelper.getConnection().prepareStatement(sql)) {
            pstmt.setInt(1, id);
            pstmt.executeUpdate();
            System.out.println("Question deleted successfully!");
        } catch (SQLException e) {
            System.out.println("Error deleting question: " + e.getMessage());
        }
    }
}
