package uk.ac.soton.group2seg.controller;

import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import uk.ac.soton.group2seg.model.ModelState;

public class MainController {

  ModelState modelState;

  @FXML
  public ComboBox<String> airportListCombo;

  @FXML
  private Label label;

  @FXML
  public void initialize() {
    modelState = new ModelState();
    airportListCombo.getItems().addAll(FXCollections.observableArrayList(modelState.getAirportList().keySet()));
  }


  public void handleSelection() {
    String selectedAirport = airportListCombo.getValue();

    if(selectedAirport == null) {
      System.out.println("No airport selected");
      return;
    }

    System.out.println("Loading airport: " + selectedAirport);
    modelState.loadAirport(selectedAirport);
  }
}
