package uk.ac.soton.group2seg.controller;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.ac.soton.group2seg.model.Obstacle;
import uk.ac.soton.group2seg.model.utility.JaxbUtility;

import java.util.HashMap;

public class PredefinedObstacleController {

    private final Logger logger = LogManager.getLogger(this.getClass());

    @FXML
    private ComboBox<String> obstacleComboBox;

    @FXML
    private TextField formDistL;

    @FXML
    private TextField formDistR;

    @FXML
    private TextField formDistCent;

    @FXML
    private TextField formHeight;

    @FXML
    private Button loadButton;

    @FXML
    private Button cancelButton;

    private HashMap<String, Integer> obstacleList;
    private MainController mainController;
    private Stage dialogStage;

    @FXML
    public void initialize() {
        // Load predefined obstacles from XML
        HashMap<String, String> parsedObstacles = JaxbUtility.parseObstacles();
        obstacleList = new HashMap<>();

        if (parsedObstacles != null) {
            // Convert height values to integers and store in obstacleList
            parsedObstacles.forEach((name, height) -> obstacleList.put(name, Integer.parseInt(height)));

            // Populate obstacle dropdown
            obstacleComboBox.getItems().addAll(FXCollections.observableArrayList(obstacleList.keySet()));
        }
    }

    @FXML
    private void handleObstacleSelection() {
        String selectedObstacle = obstacleComboBox.getValue();

        if (selectedObstacle != null) {
            // Get predefined height and update formHeight field
            int height = obstacleList.get(selectedObstacle);
            formHeight.setText(String.valueOf(height));
        }
    }

    @FXML
    private void handleLoadObstacle() {
        String selectedObstacle = obstacleComboBox.getValue();
        String distLText = formDistL.getText();
        String distRText = formDistR.getText();
        String distCentText = formDistCent.getText();
        String heightText = formHeight.getText();

        if (selectedObstacle == null || heightText.isEmpty() || distLText.isEmpty() || distRText.isEmpty() || distCentText.isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Input Validation Error");
            alert.setHeaderText(null);
            alert.setContentText("Fill in all fields");
            alert.setWidth(400);
            alert.setHeight(200);
            alert.showAndWait();
            return;
        }

        try {
            int distL = Integer.parseInt(distLText);
            int distR = Integer.parseInt(distRText);
            int distCent = Integer.parseInt(distCentText);
            int height = Integer.parseInt(heightText);

            // Create and add the obstacle
            generateObstacle(selectedObstacle, height, distL, distR, distCent);
            dialogStage.close();

        } catch (NumberFormatException e) {
            System.err.println("Invalid number format. Please enter valid integers.");
        }
    }

    private void generateObstacle(String obstacleName, int height, int distL, int distR, int distCent) {
        logger.info("Generating obstacle: " + obstacleName + " with height " + height);

        Obstacle obstacle = new Obstacle(height, distL, distR, distCent);

        if (mainController != null) {
            mainController.addObstacle(obstacle);
        }
    }

    @FXML
    private void closeMenu() {
        dialogStage.close();
    }

    public void setMainController(MainController mainController) {
        this.mainController = mainController;
    }

    public void setDialogStage(Stage dialogStage) {
        this.dialogStage = dialogStage;
    }
}
