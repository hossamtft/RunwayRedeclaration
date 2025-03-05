package uk.ac.soton.group2seg.controller;

import javafx.event.ActionEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import uk.ac.soton.group2seg.model.Obstacle;

public class ObstacleFormController {

  @FXML
  public TextField formDistL;

  @FXML
  public TextField formDistR;

  @FXML
  public TextField formDistCent;

  @FXML
  public TextField formHeight;

  @FXML
  public Button obstFormGen;

  @FXML
  public Button ObsFormCancel;

  private final Logger logger = LogManager.getLogger(this.getClass());
  private MainController mainController;
  private Stage dialogStage;
  private boolean submitted = false;


  public void generateObstacle() {
    if (isInputValid()) {
      logger.info("Generating obstacle");
      int distL = Integer.parseInt(formDistL.getText());
      int distR = Integer.parseInt(formDistR.getText());
      int distCent = Integer.parseInt(formDistCent.getText());
      int height = Integer.parseInt(formHeight.getText());

      Obstacle obstacle = new Obstacle(height, distL, distR, distCent);

      if (mainController != null) {
        mainController.addObstacle(obstacle);
      }

      submitted = true;
      dialogStage.close();
    }
  }

  private boolean isInputValid() {
    String errorMessage = "";

    try {
      if (formDistL.getText() == null || formDistL.getText().isEmpty()) {
        errorMessage += "Distance to threshold 01-18 is required!\n";
      } else {
        Integer.parseInt(formDistL.getText());
      }

      if (formDistR.getText() == null || formDistR.getText().isEmpty()) {
        errorMessage += "Distance to threshold 19-36 is required!\n";
      } else {
        Integer.parseInt(formDistR.getText());
      }

      if (formDistCent.getText() == null || formDistCent.getText().isEmpty()) {
        errorMessage += "Distance to centreline is required!\n";
      } else {
        Integer.parseInt(formDistCent.getText());
      }

      if (formHeight.getText() == null || formHeight.getText().isEmpty()) {
        errorMessage += "Obstacle height is required!\n";
      } else {
        Integer.parseInt(formHeight.getText());
      }
    } catch (NumberFormatException e) {
      errorMessage += "Please enter valid integers for all distances and height!\n";
    }

    if (errorMessage.isEmpty()) {
      return true;
    } else {
      System.err.println(errorMessage);
      return false;
    }
  }

  public void setDialogStage(Stage dialogStage) {
    this.dialogStage = dialogStage;
  }

  public void setMainController(MainController mainController) {
    this.mainController = mainController;
  }

  public void closeMenu() {
    dialogStage.close();
  }
}
