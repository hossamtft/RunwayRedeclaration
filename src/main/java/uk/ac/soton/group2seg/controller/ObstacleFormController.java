package uk.ac.soton.group2seg.controller;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.stage.Stage;
import uk.ac.soton.group2seg.model.LogicalRunway;
import uk.ac.soton.group2seg.model.Obstacle;
import uk.ac.soton.group2seg.model.Runway;

import java.net.URL;
import java.util.ResourceBundle;

public class ObstacleFormController implements Initializable {

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

  @FXML
  public ToggleButton autoFillToggle;

  private final Logger logger = LogManager.getLogger(this.getClass());
  private MainController mainController;
  private Stage dialogStage;
  private boolean submitted = false;
  private LogicalRunway lowerRunway;
  private LogicalRunway higherRunway;
  private boolean updatingFields = false;
  private boolean autoFillEnabled = true;

  @Override
  public void initialize(URL location, ResourceBundle resources) {
    if (autoFillToggle != null) {
      autoFillToggle.setSelected(true);
      autoFillToggle.setText("Auto-fill: ON");

      autoFillToggle.selectedProperty().addListener((observable, oldValue, newValue) -> {
        autoFillEnabled = newValue;
        autoFillToggle.setText(autoFillEnabled ? "Auto-fill: ON" : "Auto-fill: OFF");
        if (!autoFillEnabled) {
          formDistL.setEditable(true);
          formDistR.setEditable(true);
        }
      });
    }

    // Set up listeners for threshold distance fields
    formDistL.textProperty().addListener(new ChangeListener<String>() {
      @Override
      public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
        if (!updatingFields && !newValue.isEmpty()) {
          try {
            // Update the opposite threshold distance
            updateOppositeThreshold(formDistL, formDistR);
          } catch (NumberFormatException e) {
            // Do nothing if the input is not a valid integer
          }
        }
      }
    });

    formDistR.textProperty().addListener(new ChangeListener<String>() {
      @Override
      public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
        if (!updatingFields && !newValue.isEmpty()) {
          try {
            // Update the opposite threshold distance
            updateOppositeThreshold(formDistR, formDistL);
          } catch (NumberFormatException e) {
            // Do nothing if the input is not a valid integer
          }
        }
      }
    });
  }

  public Integer updateOppositeThreshold(TextField sourceDistance, TextField finalDistance) {
    if (!autoFillEnabled) {
      return null;
    }
    try {
      int distFromThreshold = Integer.parseInt(sourceDistance.getText());
      updatingFields = true;
      int runwayLength;
      int oppositeDistance;
      runwayLength = Math.min(lowerRunway.getLda(), higherRunway.getLda());
      if (distFromThreshold >= 0) {
        oppositeDistance = runwayLength - distFromThreshold;
      }
      else {
        oppositeDistance = runwayLength + Math.abs(distFromThreshold);
      }

      finalDistance.setText(String.valueOf(oppositeDistance));
      updatingFields = false;
      logger.debug("Updated opposite threshold: " + oppositeDistance + "m");
      return oppositeDistance;
    } catch (NumberFormatException e) {
      logger.debug("Invalid number format in threshold distance field", e);
      return null;
    }
  }

  public void generateObstacle() {
    if (isInputValid()) {
      logger.info("Generating obstacle");
      int distL = Integer.parseInt(formDistL.getText());
      int distR = Integer.parseInt(formDistR.getText());
      int distCent = (-1)*Integer.parseInt(formDistCent.getText());
      int height = Integer.parseInt(formHeight.getText());

      Obstacle obstacle = new Obstacle(height, distL, distR, distCent, "Obstacle");

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
      Alert alert = new Alert(Alert.AlertType.ERROR);
      alert.setTitle("Input Validation Error");
      alert.setHeaderText(null);
      alert.setContentText(errorMessage);
      alert.setWidth(400);
      alert.setHeight(200);
      alert.showAndWait();
      return false;
    }
  }

  public void setRunways(Runway runway) {
    lowerRunway = runway.getLowerRunway();
    higherRunway = runway.getHigherRunway();
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
