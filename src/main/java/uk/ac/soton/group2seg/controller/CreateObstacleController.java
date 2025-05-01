package uk.ac.soton.group2seg.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import uk.ac.soton.group2seg.model.utility.JaxbUtility;

public class CreateObstacleController {

    @FXML
    private TextField obstacleNameField;

    @FXML
    private TextField obstacleHeightField;

    private Stage dialogStage;

    public void setDialogStage(Stage dialogStage) {
        this.dialogStage = dialogStage;
    }

    @FXML
    private void handleSaveObstacle() {
        String name = obstacleNameField.getText();
        String heightText = obstacleHeightField.getText();

        if (name.isEmpty() || heightText.isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Input Validation Error");
            alert.setHeaderText(null);
            alert.setContentText("Please fill in all fields.");
            alert.showAndWait();
            return;
        }

        try {
            int height = Integer.parseInt(heightText);

            // Save the obstacle to the XML file
            JaxbUtility.saveObstacle(name, height);

            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Success");
            alert.setHeaderText(null);
            alert.setContentText("Obstacle created successfully!");
            alert.showAndWait();

            dialogStage.close();

        } catch (NumberFormatException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Input Error");
            alert.setHeaderText(null);
            alert.setContentText("Height must be a valid integer.");
            alert.showAndWait();
        }
    }

    @FXML
    private void handleCancel() {
        dialogStage.close();
    }
}