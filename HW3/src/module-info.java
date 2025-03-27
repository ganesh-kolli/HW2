module TP2 {
	requires javafx.controls;
	requires java.sql;
	requires javafx.graphics;
	requires java.base;
	requires org.junit.jupiter.api;
	requires javafx.swing;
	
	opens application to javafx.graphics, javafx.fxml;
}
