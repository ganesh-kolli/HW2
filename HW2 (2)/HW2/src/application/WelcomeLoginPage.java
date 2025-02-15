package application;

import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import databasePart1.*;

/**
 * The WelcomeLoginPage class displays a welcome screen for authenticated users.
 * It allows users to navigate to their respective pages based on their role or quit the application.
 */
public class WelcomeLoginPage {
	
	private final DatabaseHelper databaseHelper;

    public WelcomeLoginPage(DatabaseHelper databaseHelper) {
        this.databaseHelper = databaseHelper;
    }
    public void show( Stage primaryStage, User user) {
    	
    	VBox layout = new VBox(5);
	    layout.setStyle("-fx-alignment: center; -fx-padding: 20;");
	    
	    Label welcomeLabel = new Label("Welcome!!");
	    welcomeLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
	    
	    // Button to navigate to the user's respective page based on their role
	    Button continueButton = new Button("Continue to your Page");
	    continueButton.setOnAction(a -> {
	    	String role =user.getRole();
	    	System.out.println(role);
	    	
	    	if(role.equals("admin")) {
	    		//pass dataBaseHelper to adminHomePage 
	    		new AdminHomePage(databaseHelper).show(primaryStage);
	    	}
	    	else if(role.equals("user")) {
	    		new UserHomePage(databaseHelper).show(primaryStage);
	    	}
	    });
	    
        // Ganesh: Added logout button to allow users to log out and return to the login page
        Button logoutButton = new Button("Logout");
        logoutButton.setOnAction(a -> {
            new UserLoginPage(databaseHelper).show(primaryStage);
        });

        // Ganesh: Added switch roles dropdown to allow admins to switch between admin and user pages
        MenuButton switchRolesButton = new MenuButton("Switch Roles");
        if ("admin".equals(user.getRole())) {
            MenuItem adminItem = new MenuItem("Admin");
            adminItem.setOnAction(a -> new AdminHomePage(databaseHelper).show(primaryStage));
            
            MenuItem userItem = new MenuItem("User");
            userItem.setOnAction(a -> new UserHomePage(databaseHelper).show(primaryStage));
            
            switchRolesButton.getItems().addAll(adminItem, userItem);
        }	    
	    
	    
	    // "Invite" button for admin to generate invitation codes
	    if ("admin".equals(user.getRole())) {
            Button inviteButton = new Button("Invite");
            inviteButton.setOnAction(a -> {
                new InvitationPage().show(databaseHelper, primaryStage);
            });
            layout.getChildren().add(inviteButton);
        }

	    layout.getChildren().addAll(welcomeLabel,continueButton, switchRolesButton, logoutButton);
	    Scene welcomeScene = new Scene(layout, 800, 400);

	    // Set the scene to primary stage
	    primaryStage.setScene(welcomeScene);
	    primaryStage.setTitle("Welcome Page");
    }
}