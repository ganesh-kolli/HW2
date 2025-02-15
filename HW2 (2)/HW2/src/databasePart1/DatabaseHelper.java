package databasePart1;
import java.sql.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
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

	//  Database credentials 
	static final String USER = "sa"; 
	static final String PASS = ""; 

	private Connection connection = null;
	private Statement statement = null; 
	//	PreparedStatement pstmt

	public void connectToDatabase() throws SQLException {
		try {
			Class.forName(JDBC_DRIVER); // Load the JDBC driver
			System.out.println("Connecting to database...");
			connection = DriverManager.getConnection(DB_URL, USER, PASS);
			statement = connection.createStatement(); 
			// You can use this command to clear the database and restart from fresh.
			//statement.execute("DROP ALL OBJECTS");

			createTables();  // Create the necessary tables if they don't exist
		} catch (ClassNotFoundException e) {
			System.err.println("JDBC Driver not found: " + e.getMessage());
		}
	}
	
	public Connection getConnection() throws SQLException {
	    if (connection == null || connection.isClosed()) {
	        System.out.println("Reopening database connection...");
	        connectToDatabase();  // Reconnect if closed
	    }
	    return this.connection;
	}


	private void createTables() throws SQLException {
		String userTable = "CREATE TABLE IF NOT EXISTS cse360users ("
				+ "id INT AUTO_INCREMENT PRIMARY KEY, "
				+ "userName VARCHAR(255) UNIQUE, "
				+ "password VARCHAR(255), "
				+ "role VARCHAR(20))";
		statement.execute(userTable);
		
		//String dropTable = "DROP TABLE IF EXISTS InvitationCodes";
		//statement.execute(dropTable);
		
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
		    
		 // Ganesh: Creating questions table
	        String questionTable = "CREATE TABLE IF NOT EXISTS questions ("
	                + "id INT PRIMARY KEY, "
	                + "text VARCHAR(500), "
	                + "author VARCHAR(255))";
	        statement.execute(questionTable);

	}
	
	// Ganesh: SQL Queries for Questions
	public void insertQuestion(int id, String text, String author) {
	    String sql = "INSERT INTO questions (id, text, author) VALUES (?, ?, ?)";
	    try {
	        if (connection == null || connection.isClosed()) {
	            connectToDatabase(); // Ensure connection is open
	        }
	        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
	            pstmt.setInt(1, id);
	            pstmt.setString(2, text);
	            pstmt.setString(3, author); // Store the actual username
	            pstmt.executeUpdate();
	            System.out.println("Question inserted successfully by " + author);
	        }
	    } catch (SQLException e) {
	        System.out.println("Error inserting question: " + e.getMessage());
	    }
	}
	public List<String> getAllQuestions() {
	    List<String> questions = new ArrayList<>();
	    String sql = "SELECT id, text, author FROM questions";

	    try {
	        if (connection == null || connection.isClosed()) {
	            connectToDatabase(); // Ensure connection is open
	        }
	        try (PreparedStatement pstmt = connection.prepareStatement(sql);
	             ResultSet rs = pstmt.executeQuery()) {
	            while (rs.next()) {
	                int id = rs.getInt("id");
	                String text = rs.getString("text");
	                String author = rs.getString("author");
	                questions.add(id + ": " + text + " - Asked by " + author); // Display real username
	            }
	        }
	    } catch (SQLException e) {
	        System.out.println("Error fetching questions: " + e.getMessage());
	    }
	    return questions;
	}
	// Ganesh: Method to update a question's text by ID
	public void updateQuestion(int id, String newText) {
	    String sql = "UPDATE questions SET text = ? WHERE id = ?";
	    try {
	        if (connection == null || connection.isClosed()) {
	            connectToDatabase(); // Ensure connection is open
	        }
	        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
	            pstmt.setString(1, newText);
	            pstmt.setInt(2, id);
	            int rowsUpdated = pstmt.executeUpdate();
	            if (rowsUpdated > 0) {
	                System.out.println("Question updated successfully!");
	            } else {
	                System.out.println("Error: Question with ID " + id + " not found.");
	            }
	        }
	    } catch (SQLException e) {
	        System.out.println("Error updating question: " + e.getMessage());
	    }
	}
	// Ganesh: Method to delete a question by ID
	public void deleteQuestion(int id) {
	    String sql = "DELETE FROM questions WHERE id = ?";
	    try {
	        if (connection == null || connection.isClosed()) {
	            connectToDatabase(); // Ensure connection is open
	        }
	        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
	            pstmt.setInt(1, id);
	            int rowsDeleted = pstmt.executeUpdate();
	            if (rowsDeleted > 0) {
	                System.out.println("Question deleted successfully!");
	            } else {
	                System.out.println("Error: Question with ID " + id + " not found.");
	            }
	        }
	    } catch (SQLException e) {
	        System.out.println("Error deleting question: " + e.getMessage());
	    }
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

	// Validates a user's login credentials.
	public boolean login(User user) throws SQLException {
		String query = "SELECT * FROM cse360users WHERE userName = ? AND password = ? AND role = ?";
		try (PreparedStatement pstmt = connection.prepareStatement(query)) {
			pstmt.setString(1, user.getUserName());
			pstmt.setString(2, user.getPassword());
			pstmt.setString(3, user.getRole());
			try (ResultSet rs = pstmt.executeQuery()) {
				return rs.next();
			}
		}
	}
	
	// Checks if a user already exists in the database based on their userName.
	public boolean doesUserExist(String userName) {
	    String query = "SELECT COUNT(*) FROM cse360users WHERE userName = ?";
	    try (PreparedStatement pstmt = connection.prepareStatement(query)) {
	        
	        pstmt.setString(1, userName);
	        ResultSet rs = pstmt.executeQuery();
	        
	        if (rs.next()) {
	            // If the count is greater than 0, the user exists
	            return rs.getInt(1) > 0;
	        }
	    } catch (SQLException e) {
	        e.printStackTrace();
	    }
	    return false; // If an error occurs, assume user doesn't exist
	}
	
	// Retrieves the role of a user from the database using their UserName.
	public String getUserRole(String userName) {
	    String query = "SELECT role FROM cse360users WHERE userName = ?";
	    try (PreparedStatement pstmt = connection.prepareStatement(query)) {
	        pstmt.setString(1, userName);
	        ResultSet rs = pstmt.executeQuery();
	        
	        if (rs.next()) {
	            return rs.getString("role"); // Return the role if user exists
	        }
	    } catch (SQLException e) {
	        e.printStackTrace();
	    }
	    return null; // If no user exists or an error occurs
	}
	
	// Generates a new invitation code and inserts it into the database.
	public String generateInvitationCode() {
	    String code = UUID.randomUUID().toString().substring(0, 4); // Generate a random 4-character code
	    long deadlineMillis = System.currentTimeMillis() + 2 * 60 * 1000; //2 minute deadline
	    Timestamp deadline = new Timestamp(deadlineMillis); // Set deadline as 2 minute
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
	            // Mark the code as used
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
	    // Step 1: Clean up any unused OTPs for the user before generating a new one
	    String cleanupQuery = "DELETE FROM user_otp WHERE userName = ? AND isUsed = FALSE";
	    try (PreparedStatement cleanupStmt = connection.prepareStatement(cleanupQuery)) {
	        cleanupStmt.setString(1, userName);
	        cleanupStmt.executeUpdate();
	    } catch (SQLException e) {
	        e.printStackTrace();
	    }

	    // Step 2: Generate a random 6-digit OTP
	    String otp = String.format("%06d", (int) (Math.random() * 1000000));

	    // Step 3: Insert the new OTP into the database with isUsed = FALSE
	    String query = "INSERT INTO user_otp (userName, otp, isUsed) VALUES (?, ?, FALSE)";
	    try (PreparedStatement pstmt = connection.prepareStatement(query)) {
	        pstmt.setString(1, userName);
	        pstmt.setString(2, otp);
	        pstmt.executeUpdate();
	        connection.commit();  // Ensure the OTP is saved to the database
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
	                markOtpAsUsed(userName, otp);  // Mark OTP as used
	                return true;  // OTP is valid
	            } else {
	                System.out.println("OTP has already been used.");
	            }
	        } else {
	            System.out.println("No matching OTP found for user: " + userName);
	        }
	    } catch (SQLException e) {
	        e.printStackTrace();
	    }
	    return false;  // OTP is invalid or already used
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
	        return false; // If an error occurs, assume no OTP exists
	    }
	//Create method to get userName and Role as a list//
		public List<String[]> getAllUsernamesAndRoles(){
			List<String[]> userList = new ArrayList<>(); 	
			String query = "SELECT userName, role FROM cse360users";
			
			try (PreparedStatement pstmt = connection.prepareStatement(query); ResultSet rs = pstmt.executeQuery()) {
				while (rs.next()) {
					userList.add(new String[]{rs.getString("userName"), rs.getString("role")});
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
			return userList;
		}
		
		//Create method to delete Users
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
		
		// Div (New Method to Update User's Role)
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
		

	    // Method to count number of Admin Users
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
	 // Ganesh: Close connection only when the application exits
	    public void closeConnection() {
	        try { 
	            if (statement != null && !statement.isClosed()) {
	                statement.close();
	                statement = null; // Reset
	            }
	        } catch (SQLException se2) { 
	            se2.printStackTrace();
	        } 
	        try { 
	            if (connection != null && !connection.isClosed()) {
	                connection.close();
	                connection = null; // Reset
	            }
	        } catch (SQLException se) { 
	            se.printStackTrace(); 
	        } 
	    }


	

}
