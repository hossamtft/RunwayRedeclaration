package uk.ac.soton.group2seg.controller;

import java.io.IOException;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.ac.soton.group2seg.model.LogicalRunway;
import uk.ac.soton.group2seg.model.ModelState;
import uk.ac.soton.group2seg.model.Obstacle;
import uk.ac.soton.group2seg.view.RunwayVisual;

/**
 * The main controller for the application
 */
public class MainController {
  private final Logger logger = LogManager.getLogger(this.getClass());

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

  @FXML
  public Label lowerRunwayDetails;

  @FXML
  public Button runwayLoadButton;

  @FXML
  public ComboBox<String> airportListCombo;

  @FXML
  public ComboBox<String> runwayListCombo;

  @FXML
  public VBox runwayContainer;

  @FXML
  public Button addObstButton;

  @FXML
  private VBox tabPaneContainer;

  @FXML
  private Button toggleTabPaneButton;

  @FXML
  private TextArea asdaTextArea;

  @FXML
  private TextArea toraTextArea;

  @FXML
  private TextArea todaTextArea;

  @FXML
  private TextArea ldaTextArea;

  private ModelState modelState;
  private Calculator calculator;

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
      logger.debug("No selected airport");
      return;
    }

    logger.info("Loading airport: " + selectedAirport);
    modelState.loadAirport(selectedAirport);

    runwayListCombo.getItems().addAll(FXCollections.observableArrayList(modelState.getRunways()));
    runwayListCombo.setVisible(true);

    runwayLoadButton.setVisible(true);
  }

  public void handleRunwaySelection() {
    String selectedRunway = runwayListCombo.getValue();

    if (selectedRunway == null) {
      logger.debug("No selected runway");
      return;
    }

    modelState.selectRunway(selectedRunway);
    logger.info("Loading runway: " + selectedRunway);

    this.calculator = new Calculator(modelState.getCurrentRunway());
    ldaTextArea.textProperty().bind(calculator.getLdaBreakdown());
    toraTextArea.textProperty().bind(calculator.getToraBreakdown());
    todaTextArea.textProperty().bind(calculator.getTodaBreakdown());
    asdaTextArea.textProperty().bind(calculator.getAsdaBreakdown());
    addObstButton.setVisible(true);

    initialiseTables();
    initialiseRunwayView();

  }

  private void initialiseRunwayView() {
    logger.info("Initialising runway view");
    RunwayVisual runwayVisual = new RunwayVisual(3500);
    runwayContainer.getChildren().clear();
    runwayContainer.setAlignment(Pos.CENTER);
    runwayContainer.setPrefSize(3500 + 20, 100);  // Adjust height as needed for the line and text
    runwayContainer.getChildren().add(runwayVisual);

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

  public void updateTables() {
    // call method when calculations are done.
    currentTableView.getItems().clear();
    originalTableView.getItems().clear();
    originalTableView.setItems(FXCollections.observableArrayList(modelState.getCurrentRunway().getLogicalRunways()));
    currentTableView.setItems(FXCollections.observableArrayList(modelState.getCurrentRunway().getLogicalRunways()));
  }

  public void loadObstacleForm() {
    try {
      // Load the FXML file
      FXMLLoader loader = new FXMLLoader();
      loader.setLocation(getClass().getResource("/view/ObstacleForm.fxml"));
      Parent root = loader.load();

      // Create a new stage for the dialog
      Stage dialogStage = new Stage();
      dialogStage.setTitle("Add Obstacle");
      dialogStage.initModality(Modality.WINDOW_MODAL);
      // Get the current stage (parent of the button)
      Stage primaryStage = (Stage) addObstButton.getScene().getWindow();
      dialogStage.initOwner(primaryStage);

      // Set the scene
      Scene scene = new Scene(root);
      dialogStage.setScene(scene);

      // Get the controller
      ObstacleFormController controller = loader.getController();
      controller.setDialogStage(dialogStage);
      controller.setMainController(this);

      // Show the dialog and wait until the user closes it
      dialogStage.showAndWait();

    } catch (IOException e) {
      e.printStackTrace();
    }

  }

  public void addObstacle(Obstacle obstacle) {
    logger.debug("Adding obstacle: " + obstacle);
    calculator.redeclareRunway(obstacle);

    updateTables();

  }

  public void toggleTabPaneVisibility() {
    boolean isVisible = tabPaneContainer.isVisible();
    tabPaneContainer.setVisible(!isVisible);
    tabPaneContainer.setManaged(!isVisible);
    if (isVisible) {
      toggleTabPaneButton.setText("Show Calculation Breakdown");
    } else {
      toggleTabPaneButton.setText("Hide Calculation Breakdown");
    }
  }
}
