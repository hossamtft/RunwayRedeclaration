package uk.ac.soton.group2seg.controller;

import java.awt.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.geometry.Insets;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import java.sql.*;

public class LoginController {

  private final Logger logger = LogManager.getLogger(this.getClass());

  @FXML
  public Label toolTitle;
  @FXML
  public Button quitButton;
  @FXML
  public Button loginButton;
  @FXML
  public Button registerButton;
  @FXML
  public Button guestButton;


  private static final String DB_URL = "jdbc:sqlite:runwayredeclaration.sqlite";

  /**
   * Establish connection to DB
   * @return
   * @throws SQLException
   */
  public Connection connectToDatabase() {
    try {
      return DriverManager.getConnection(DB_URL);
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * When the "register" button is clicked, present the user with the form that they need to fill in.
   *
   */
  @FXML
  public void loadRegistrationForm() {
    Stage registerStage = new Stage();
    registerStage.initModality(Modality.APPLICATION_MODAL);
    registerStage.setTitle("User Registration");

    TextField usernameField = new TextField();
    usernameField.setPromptText("Enter username");

    PasswordField passwordField = new PasswordField();
    passwordField.setPromptText("Enter password");

    ComboBox<String> roleDropdown = new ComboBox<>();
    roleDropdown.getItems().addAll("ATC", "Ground Crew", "Regulator");
    roleDropdown.setPromptText("Select role");

    TextField airportIDField = new TextField();
    airportIDField.setPromptText("Enter airport ID");

    Button submitButton = new Button("Submit");
    submitButton.setDisable(true);

    // Enable submit button when all fields have values
    usernameField.textProperty().addListener((observable, oldValue, newValue) -> checkFields(usernameField, passwordField, roleDropdown, airportIDField, submitButton));
    passwordField.textProperty().addListener((observable, oldValue, newValue) -> checkFields(usernameField, passwordField, roleDropdown, airportIDField, submitButton));
    roleDropdown.valueProperty().addListener((observable, oldValue, newValue) -> checkFields(usernameField, passwordField, roleDropdown, airportIDField, submitButton));
    airportIDField.textProperty().addListener((observable, oldValue, newValue) -> checkFields(usernameField, passwordField, roleDropdown, airportIDField, submitButton));

    submitButton.setOnAction(event -> {
      String username = usernameField.getText();
      String password = passwordField.getText();
      String role = roleDropdown.getValue();
      String airportID = airportIDField.getText();

      logger.info("User registered - Username: {}, Role: {}, Airport ID: {}", username, role, airportID);

      if (validateInputs(username, password, role, airportID)) {
        try (Connection connection = connectToDatabase()) {
          String query = "INSERT INTO Users (Username, Password, Role, AirportID) VALUES (?, ?, ?, ?)";
          PreparedStatement preparedStatement = connection.prepareStatement(query);
          preparedStatement.setString(1, username);
          preparedStatement.setString(2, password);
          preparedStatement.setString(3, role);
          preparedStatement.setString(4, airportID);

          int rowsAffected = preparedStatement.executeUpdate();
          if (rowsAffected > 0) {
            logger.info("User successfully registered!");
          } else {
            logger.error("Failed to register user.");
          }
        } catch (SQLException e) {
          logger.error("Database error: " + e.getMessage());
          e.printStackTrace();
        }
      }

      registerStage.close();
    });

    // Layout for the popup
    GridPane grid = new GridPane();
    grid.setPadding(new Insets(20));
    grid.setVgap(10);
    grid.setHgap(10);
    grid.add(new Label("Username:"), 0, 0);
    grid.add(usernameField, 1, 0);
    grid.add(new Label("Password:"), 0, 1);
    grid.add(passwordField, 1, 1);
    grid.add(new Label("Role in airport:"), 0, 2);
    grid.add(roleDropdown, 1, 2);
    grid.add(new Label("Airport ID:"), 0, 3);
    grid.add(airportIDField, 1, 3);

    HBox buttonBox = new HBox(10, submitButton);
    VBox vbox = new VBox(10, grid, buttonBox);
    vbox.setPadding(new Insets(20));

    Scene scene = new Scene(vbox);
    registerStage.setScene(scene);
    registerStage.showAndWait();
  }

  /**
   * Submit button becomes available whenever all fields are filled/selected.
   * @param username
   * @param password
   * @param role
   * @param airportID
   * @param submitButton
   */
  private void checkFields(TextField username, PasswordField password, ComboBox<String> role, TextField airportID, Button submitButton) {
    submitButton.setDisable(username.getText().isEmpty() || password.getText().isEmpty() || role.getValue() == null || airportID.getText().isEmpty());
  }

  /**
   * Confirm all fields are filled to register and validate user inputs
   * @param username
   * @param password
   * @param role
   * @param airportID
   * @return
   */
  private boolean validateInputs(String username, String password, String role, String airportID) {
    if (username.length() < 6) {
      showError("Username must be at least 6 characters long.");
      return false;
    }

    // password checking
    if (password.length() < 8 || !password.matches(".*[A-Z].*") || !password.matches(".*[a-z].*") || !password.matches(".*\\d.*") || !password.matches(".*[!@#$%^&*(),.?\":{}|<>].*")) {
      showError("Password must be at least 8 characters long and include at least one uppercase letter, one lowercase letter, one number, and one symbol.");
      return false;
    }

    if (role == null) {
      showError("Please select a role.");
      return false;
    }

    if (airportID.isEmpty()) {
      showError("Airport ID cannot be empty.");
      return false;
    }

    return true;
  }

  /**
   * Different type of error messages displayed based on type of invalid input
   * @param message to display
   */
  private void showError(String message) {
    Alert alert = new Alert(Alert.AlertType.ERROR);
    alert.setTitle("Input Validation Error");
    alert.setHeaderText(null);

    alert.setContentText(message);

    alert.setWidth(400);
    alert.setHeight(200);
    alert.showAndWait();

  }
}
