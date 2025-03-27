package databasePart1;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import application.User;

/**
 * The DatabaseHelper class is responsible for managing the connection to the database,
 * performing operations such as user registration, login validation, and handling invitation codes.
 */
public class DatabaseHelper {

    // JDBC driver name and database URL 
    static final String JDBC_DRIVER = "org.h2.Driver";   
    static final String DB_URL = "jdbc:h2:~/FoundationDatabase";  

    // Database credentials 
    static final String USER = "sa"; 
    static final String PASS = ""; 

    private static Connection connection = null;
    private Statement statement = null;

    public void connectToDatabase() throws SQLException {
        try {
            Class.forName(JDBC_DRIVER); // Load the JDBC driver
            System.out.println("Connecting to database...");
            connection = DriverManager.getConnection(DB_URL, USER, PASS);
            statement = connection.createStatement(); 
            // Optionally clear the database using: statement.execute("DROP ALL OBJECTS");
            createTables();  // Create necessary tables if they don't exist
            
            // Clear previous session Q&A data so that only new questions/answers appear during the current session.
            // Delete answers first (due to foreign key constraints), then questions.
            statement.execute("DELETE FROM answers");
            statement.execute("DELETE FROM questions");
            // Reset auto-increment counters so that numbering starts at 1 each session.
            statement.execute("ALTER TABLE questions ALTER COLUMN id RESTART WITH 1");
            statement.execute("ALTER TABLE answers ALTER COLUMN id RESTART WITH 1");
            
        } catch (ClassNotFoundException e) {
            System.err.println("JDBC Driver not found: " + e.getMessage());
        }
    }

    private void createTables() throws SQLException {
        String userTable = "CREATE TABLE IF NOT EXISTS cse360users ("
                + "id INT AUTO_INCREMENT PRIMARY KEY, "
                + "userName VARCHAR(255) UNIQUE, "
                + "password VARCHAR(255), "
                + "role VARCHAR(20))";
        statement.execute(userTable);
        
        String dropTable = "DROP TABLE IF EXISTS InvitationCodes";
        statement.execute(dropTable);
        
        // Create the invitation codes table
        String invitationCodesTable = "CREATE TABLE IF NOT EXISTS InvitationCodes ("
                + "code VARCHAR(10) PRIMARY KEY, "
                + "isUsed BOOLEAN DEFAULT FALSE, "
                + "deadline TIMESTAMP)";
        statement.execute(invitationCodesTable);
        
        String dropOtpTable = "DROP TABLE IF EXISTS user_otp";
        statement.execute(dropOtpTable);

        // Create the user_otp table with the correct schema
        String otpTable = "CREATE TABLE IF NOT EXISTS user_otp ("
                + "userName VARCHAR(255), "
                + "otp VARCHAR(10), "
                + "isUsed BOOLEAN DEFAULT FALSE)";
        statement.execute(otpTable);
        
        // Create tables for Questions and Answers
        String questionsTable = "CREATE TABLE IF NOT EXISTS questions ("
                + "id INT AUTO_INCREMENT PRIMARY KEY, "
                + "text VARCHAR(1000) NOT NULL, "
                + "createdBy VARCHAR(255) NOT NULL, "
                + "createdAt TIMESTAMP DEFAULT CURRENT_TIMESTAMP)";
        statement.execute(questionsTable);

        String answersTable = "CREATE TABLE IF NOT EXISTS answers ("
                + "id INT AUTO_INCREMENT PRIMARY KEY, "
                + "question_id INT, "
                + "answeredBy VARCHAR(255) NOT NULL, "
                + "text VARCHAR(1000) NOT NULL, "
                + "createdAt TIMESTAMP DEFAULT CURRENT_TIMESTAMP, "
                + "FOREIGN KEY (question_id) REFERENCES questions(id) ON DELETE CASCADE)";
        statement.execute(answersTable);
    }

    // Check if the database is empty
    public boolean isDatabaseEmpty() throws SQLException {
        String query = "SELECT COUNT(*) AS count FROM cse360users";
        ResultSet resultSet = statement.executeQuery(query);
        if (resultSet.next()) {
            return resultSet.getInt("count") == 0;
        }
        return true;
    }

    // Registers a new user in the database.
    public void register(User user) throws SQLException {
        String insertUser = "INSERT INTO cse360users (userName, password, role) VALUES (?, ?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(insertUser)) {
            pstmt.setString(1, user.getUserName());
            pstmt.setString(2, user.getPassword());
            pstmt.setString(3, user.getRole());
            pstmt.executeUpdate();
        }
    }
 // Validates a user's login credentials.(sumedha)
 	public boolean login(User user) throws SQLException {
 	    String query = "SELECT * FROM cse360users WHERE userName = ? AND password = ? AND role = ?";
 	    try (PreparedStatement pstmt = connection.prepareStatement(query)) {
 	        pstmt.setString(1, user.getUserName());
 	        pstmt.setString(2, user.getPassword());
 	        pstmt.setString(3, user.getRole());
 	        try (ResultSet rs = pstmt.executeQuery()) {
 	            if (rs.next()) {
 	                currentUsername = user.getUserName();  // Store the logged-in username
 	                return true;
 	            }
 	        }
 	    }
 	    return false;
 	}
 	private static String currentUsername = null; // Stores the logged-in username

    
    // Checks if a user already exists in the database based on their userName.
    public boolean doesUserExist(String userName) {
        String query = "SELECT COUNT(*) FROM cse360users WHERE userName = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, userName);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
    
    // Retrieves the role of a user from the database using their userName.
    public String getUserRole(String userName) {
        String query = "SELECT role FROM cse360users WHERE userName = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, userName);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getString("role");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
    
    // Generates a new invitation code and inserts it into the database.
    public String generateInvitationCode() {
        String code = UUID.randomUUID().toString().substring(0, 4);
        long deadlineMillis = System.currentTimeMillis() + 2 * 60 * 1000; // 2 minute deadline
        Timestamp deadline = new Timestamp(deadlineMillis);
        String query = "INSERT INTO InvitationCodes (code, deadline) VALUES (?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, code);
            pstmt.setTimestamp(2, deadline);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return code;
    }

    // Validates an invitation code to check if it is unused.
    public boolean validateInvitationCode(String code) {
        String query = "SELECT * FROM InvitationCodes WHERE code = ? AND isUsed = FALSE AND deadline > CURRENT_TIMESTAMP";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, code);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                markInvitationCodeAsUsed(code);
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
    
    // Marks the invitation code as used in the database.
    private void markInvitationCodeAsUsed(String code) {
        String query = "UPDATE InvitationCodes SET isUsed = TRUE WHERE code = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, code);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    public String generateOTP(String userName) {
        // Clean up any unused OTPs for the user before generating a new one.
        String cleanupQuery = "DELETE FROM user_otp WHERE userName = ? AND isUsed = FALSE";
        try (PreparedStatement cleanupStmt = connection.prepareStatement(cleanupQuery)) {
            cleanupStmt.setString(1, userName);
            cleanupStmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        // Generate a random 6-digit OTP
        String otp = String.format("%06d", (int) (Math.random() * 1000000));
        String query = "INSERT INTO user_otp (userName, otp, isUsed) VALUES (?, ?, FALSE)";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, userName);
            pstmt.setString(2, otp);
            pstmt.executeUpdate();
            connection.commit();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        System.out.println("Generated OTP for user " + userName + ": " + otp);
        return otp;
    }

    public boolean validateOTP(String userName, String otp) {
        String query = "SELECT otp, isUsed FROM user_otp WHERE userName = ? AND otp = ? AND isUsed = FALSE";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, userName);
            pstmt.setString(2, otp);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                boolean isUsed = rs.getBoolean("isUsed");
                if (!isUsed) {
                    System.out.println("Valid OTP found. Marking as used...");
                    markOtpAsUsed(userName, otp);
                    return true;
                } else {
                    System.out.println("OTP has already been used.");
                }
            } else {
                System.out.println("No matching OTP found for user: " + userName);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    private void markOtpAsUsed(String userName, String otp) {
        String query = "UPDATE user_otp SET isUsed = TRUE WHERE userName = ? AND otp = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, userName);
            pstmt.setString(2, otp);
            int rowsUpdated = pstmt.executeUpdate();
            System.out.println("Rows updated after marking OTP as used: " + rowsUpdated);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void clearOTP(String userName) {
        String query = "DELETE FROM user_otp WHERE userName = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, userName);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void updatePassword(String userName, String newPassword) {
        String query = "UPDATE cse360users SET password = ? WHERE userName = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, newPassword);
            pstmt.setString(2, userName);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public boolean hasOTP(String userName) {
        String query = "SELECT COUNT(*) FROM user_otp WHERE userName = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, userName);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
    
    // Get list of usernames and roles.
    public List<String[]> getAllUsernamesAndRoles(){
        List<String[]> userList = new ArrayList<>();     
        String query = "SELECT userName, role FROM cse360users";
        try (PreparedStatement pstmt = connection.prepareStatement(query); 
             ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                userList.add(new String[]{rs.getString("userName"), rs.getString("role")});
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return userList;
    }
    
    // Delete a user.
    public boolean deleteUser(String username) {
        String query = "DELETE FROM cse360users WHERE userName = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, username);
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    // Update a user's role.
    public boolean updateUserRole(String userName, String newRole){
        String query = "UPDATE cse360users SET role = ? WHERE userName = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(query)){
            pstmt.setString(1, newRole);
            pstmt.setString(2, userName);
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Count number of admin users.
    public int countAdmins() {
        String query = "SELECT COUNT(*) FROM cse360users WHERE role = 'admin'";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0; 
    }
    
    // NEW: Insert a question into the database and return its generated ID.
    public int insertQuestion(String text, String createdBy) throws SQLException {
        String query = "INSERT INTO questions (text, createdBy) VALUES (?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, text);
            pstmt.setString(2, createdBy);
            pstmt.executeUpdate();
            try (ResultSet rs = pstmt.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }
        throw new SQLException("Failed to insert question, no ID obtained.");
    }
    
    // NEW: Insert an answer into the database.
    public void insertAnswer(int questionId, String answeredBy, String text) throws SQLException {
        String query = "INSERT INTO answers (question_id, answeredBy, text) VALUES (?, ?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setInt(1, questionId);
            pstmt.setString(2, answeredBy);
            pstmt.setString(3, text);
            pstmt.executeUpdate();
        }
    }
    
    // Get all questions from the database.
    public List<String[]> getAllQuestions() throws SQLException {
        List<String[]> questions = new ArrayList<>();
        String query = "SELECT id, text FROM questions";
        try (PreparedStatement pstmt = connection.prepareStatement(query);
             ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                questions.add(new String[]{String.valueOf(rs.getInt("id")), rs.getString("text")});
            }
        }
        return questions;
    }

    // Get all answers from the database.
    public List<String[]> getAllAnswers() throws SQLException {
        List<String[]> answers = new ArrayList<>();
        String query = "SELECT id, text FROM answers";
        try (PreparedStatement pstmt = connection.prepareStatement(query);
             ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                answers.add(new String[]{String.valueOf(rs.getInt("id")), rs.getString("text")});
            }
        }
        return answers;
    }
    
    // NEW: Retrieve the question_id for a given answer id.
    public int getQuestionIdByAnswerId(int answerId) throws SQLException {
        String query = "SELECT question_id FROM answers WHERE id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setInt(1, answerId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("question_id");
                }
            }
        }
        return -1; // Return -1 if not found.
    }

    // Update a question in the database.
    public void updateQuestion(int id, String newText) throws SQLException {
        String query = "UPDATE questions SET text = ? WHERE id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, newText);
            pstmt.setInt(2, id);
            pstmt.executeUpdate();
        }
    }

    // Update an answer in the database.
    public void updateAnswer(int id, String newText) throws SQLException {
        String query = "UPDATE answers SET text = ? WHERE id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, newText);
            pstmt.setInt(2, id);
            pstmt.executeUpdate();
        }
    }
    
    public List<String[]> getQuestionsByUser(String studentUsername) throws SQLException {
        List<String[]> questions = new ArrayList<>();
        String query = "SELECT id, text FROM questions WHERE createdBy = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, studentUsername);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                questions.add(new String[]{String.valueOf(rs.getInt("id")), rs.getString("text")});
            }
        }
        return questions;
    }
    
    public List<String[]> getAnswersByUser(String studentUsername) throws SQLException {
        List<String[]> answers = new ArrayList<>();
        String query = "SELECT id, text FROM answers WHERE answeredBy = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, studentUsername);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                answers.add(new String[]{String.valueOf(rs.getInt("id")), rs.getString("text")});
            }
        }
        return answers;
    }
	  //added this method which is called while submitting answer to a question(sumedha)
    public static String getCurrentUsername() {
        return (currentUsername != null) ? currentUsername : "Unknown"; 
    }
    public List<String[]> getAnswersByQuestion(int questionId) throws SQLException {
        List<String[]> answers = new ArrayList<>();

        String query = "SELECT answeredBy, text FROM answers WHERE question_id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setInt(1, questionId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    String answeredBy = rs.getString("answeredBy");
                    String answerText = rs.getString("text");
                    answers.add(new String[] { answeredBy, answerText });
                }
            }
        }
        return answers;
    }

    
    // Closes the database connection and statement.
    public void closeConnection() {
        try { 
            if(statement != null) statement.close(); 
        } catch(SQLException se2) { 
            se2.printStackTrace();
        } 
        try { 
            if(connection != null) connection.close(); 
        } catch(SQLException se){ 
            se.printStackTrace();
        } 
    }
}