package application;

import javafx.stage.*;
import javafx.scene.*;
import javafx.scene.layout.*;
import javafx.scene.control.*;
import javafx.geometry.*;

/**
 * This class implements an alert box for the user interface for displaying
 * error messages.
 * 
 * @author Fangying Zhan
 *
 */
public class AlertBox {

	public static void display(String title, String message) {
		Stage window = new Stage();

		// Block events to other windows
		window.initModality(Modality.APPLICATION_MODAL);
		window.setTitle(title);
		window.setMinHeight(150);
		window.setMinWidth(250);

		Label label = new Label();
		label.setText(message);
		Button closeButton = new Button("OK");
		closeButton.setOnAction(e -> window.close());

		VBox layout = new VBox(10);
		layout.getChildren().addAll(label, closeButton);
		layout.setAlignment(Pos.CENTER);

		// Display window and wait for it to be closed before returning
		Scene scene = new Scene(layout);
		window.setScene(scene);
		window.showAndWait();
	}

}