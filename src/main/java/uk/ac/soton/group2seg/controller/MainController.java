package uk.ac.soton.group2seg.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import uk.ac.soton.group2seg.model.LogicalRunway;
import uk.ac.soton.group2seg.model.ModelState;

/**
 * The main controller for the application
 */
public class MainController {

  @FXML
  public TableView<LogicalRunway> originalTableView;

  @FXML
  public TableColumn<LogicalRunway, String> nameCol;

  @FXML
  public TableColumn<LogicalRunway, Integer> asdaCol;

  @FXML
  public TableColumn<LogicalRunway, Integer> toraCol;

  @FXML
  public TableColumn<LogicalRunway, Integer> todaCol;

  @FXML
  public TableColumn<LogicalRunway, Integer> ldaCol;

  @FXML
  public TableView<LogicalRunway> currentTableView;

  @FXML
  public TableColumn<LogicalRunway, String> currNameCol;

  @FXML
  public TableColumn<LogicalRunway, Integer> currAsdaCol;

  @FXML
  public TableColumn<LogicalRunway, Integer> currToraCol;

  @FXML
  public TableColumn<LogicalRunway, Integer> currTodaCol;

  @FXML
  public TableColumn<LogicalRunway, Integer> currLdaCol;

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

    initialiseTables();
  }

  private void initialiseTables() {
    nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
    asdaCol.setCellValueFactory(new PropertyValueFactory<>("asda"));
    toraCol.setCellValueFactory(new PropertyValueFactory<>("tora"));
    todaCol.setCellValueFactory(new PropertyValueFactory<>("toda"));
    ldaCol.setCellValueFactory(new PropertyValueFactory<>("lda"));

    currNameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
    currAsdaCol.setCellValueFactory(new PropertyValueFactory<>("currAsda"));
    currToraCol.setCellValueFactory(new PropertyValueFactory<>("currTora"));
    currTodaCol.setCellValueFactory(new PropertyValueFactory<>("currToda"));
    currLdaCol.setCellValueFactory(new PropertyValueFactory<>("currLda"));

    ObservableList<LogicalRunway> runwayData = FXCollections.observableArrayList(modelState.getCurrentRunway()
        .getLogicalRunways());

    originalTableView.setItems(runwayData);
    currentTableView.setItems(runwayData);

  }

}
