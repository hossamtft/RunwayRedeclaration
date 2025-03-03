package uk.ac.soton.group2seg.controller;

import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import uk.ac.soton.group2seg.model.ModelState;

/**
 * The main controller for the application
 */
public class MainController {

  private ModelState modelState;

  @FXML
  public Label lowerRunwayDetails;

  @FXML
  public Button runwayLoadButton;

  @FXML
  public ComboBox<String> airportListCombo;

  @FXML
  public ComboBox<String> runwayListCombo;


  /**
   * Initialise the application
   * */
  @FXML
  public void initialize() {
    modelState = new ModelState();
    airportListCombo.getItems().addAll(FXCollections.observableArrayList(modelState.getAirportList().keySet()));
  }


  /**
   * Handle button press for airport loading
   * */
  public void handleAirportSelection() {
    String selectedAirport = airportListCombo.getValue();

    if(selectedAirport == null) {
      System.out.println("No airport selected");
      return;
    }

    System.out.println("Loading airport: " + selectedAirport);
    modelState.loadAirport(selectedAirport);

    runwayListCombo.getItems().addAll(FXCollections.observableArrayList(modelState.getRunways()));
    runwayListCombo.setVisible(true);

    runwayLoadButton.setVisible(true);
  }

  public void handleRunwaySelection() {
    String selectedRunway = runwayListCombo.getValue();

    if (selectedRunway == null) {
      System.out.println("No runway selected");
      return;
    }

    modelState.selectRunway(selectedRunway);
    System.out.println("Selected: " + selectedRunway);

    lowerRunwayDetails.setText(modelState.getCurrentRunway().getLowerRunway().getDistances());
  }
}
