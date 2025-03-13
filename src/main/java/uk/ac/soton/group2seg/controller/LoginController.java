package uk.ac.soton.group2seg.controller;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
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
import uk.ac.soton.group2seg.BCrypt.BCrypt;

import java.io.IOException;
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

  public Connection connectToDatabase() {
    try {
      return DriverManager.getConnection(DB_URL);
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }

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
    roleDropdown.getItems().addAll("Admin", "ATC", "Ground Crew", "Regulator");
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

      if (validateInputs(username, password, role, airportID)) {
        try (Connection connection = connectToDatabase()) {
          String hashedPassword = hashPassword(password);
          String query = "INSERT INTO Users (Username, Password, Role, AirportID) VALUES (?, ?, ?, ?)";
          PreparedStatement preparedStatement = connection.prepareStatement(query);
          preparedStatement.setString(1, username);
          preparedStatement.setString(2, hashedPassword);
          preparedStatement.setString(3, role);
          preparedStatement.setString(4, airportID);
          registerStage.close();
          logger.info("User registered - Username: {}, Role: {}, Airport ID: {}", username, role, airportID);
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

  private void checkFields(TextField username, PasswordField password, ComboBox<String> role, TextField airportID, Button submitButton) {
    submitButton.setDisable(username.getText().isEmpty() || password.getText().isEmpty() || role.getValue() == null || airportID.getText().isEmpty());
  }

  private boolean validateInputs(String username, String password, String role, String airportID) {
    if (username.length() < 6) {
      showError("Username must be at least 6 characters long.");
      return false;
    }

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


  private void showError(String message) {
    Alert alert = new Alert(Alert.AlertType.ERROR);
    alert.setTitle("Input Validation Error");
    alert.setHeaderText(null);
    alert.setContentText(message);
    alert.setWidth(400);
    alert.setHeight(200);
    alert.showAndWait();
  }


  @FXML
  public void initialize() {
    setupButtonHandlers();
    logger.info("LoginController initialized");
  }

  private void setupButtonHandlers() {
    quitButton.setOnAction(event -> {
      logger.info("Quit button clicked. Shutting down application.");
      Platform.exit();
    });

    loginButton.setOnAction(event -> {
      logger.info("Login button clicked");
      showLoginDialog();
    });

    guestButton.setOnAction(event -> {
      logger.info("Continue as Guest button clicked");
      proceedAsGuest();
    });
  }

  private void showLoginDialog() {
    Stage loginStage = new Stage();
    loginStage.initModality(Modality.APPLICATION_MODAL);
    loginStage.setTitle("User Login");

    TextField usernameField = new TextField();
    usernameField.setPromptText("Enter username");

    PasswordField passwordField = new PasswordField();
    passwordField.setPromptText("Enter password");

    Button loginSubmitButton = new Button("Login");
    Button cancelButton = new Button("Cancel");

    usernameField.textProperty().addListener((observable, oldValue, newValue) ->
            loginSubmitButton.setDisable(usernameField.getText().isEmpty() || passwordField.getText().isEmpty()));
    passwordField.textProperty().addListener((observable, oldValue, newValue) ->
            loginSubmitButton.setDisable(usernameField.getText().isEmpty() || passwordField.getText().isEmpty()));

    loginSubmitButton.setDisable(true);

    loginSubmitButton.setOnAction(event -> {
      String username = usernameField.getText();
      String password = passwordField.getText();

      if (authenticateUser(username, password)) {
        logger.info("User successfully logged in: {}", username);
        loginStage.close();
        openMainApplication(username);
      } else {
        logger.info("Login failed for user: {}", username);
        showError("Invalid username or password");
      }
    });

    cancelButton.setOnAction(event -> loginStage.close());

    GridPane grid = new GridPane();
    grid.setPadding(new Insets(20));
    grid.setVgap(10);
    grid.setHgap(10);
    grid.add(new Label("Username:"), 0, 0);
    grid.add(usernameField, 1, 0);
    grid.add(new Label("Password:"), 0, 1);
    grid.add(passwordField, 1, 1);

    HBox buttonBox = new HBox(10, loginSubmitButton, cancelButton);
    buttonBox.setPadding(new Insets(10, 0, 0, 0));
    VBox vbox = new VBox(10, grid, buttonBox);
    vbox.setPadding(new Insets(20));

    Scene scene = new Scene(vbox);
    loginStage.setScene(scene);
    loginStage.showAndWait();
  }

  private boolean authenticateUser(String username, String password) {
    try (Connection connection = connectToDatabase()) {
      String query = "SELECT Password FROM Users WHERE Username = ?";
      PreparedStatement preparedStatement = connection.prepareStatement(query);
      preparedStatement.setString(1, username);

      ResultSet resultSet = preparedStatement.executeQuery();
      if (resultSet.next()) {
        String hashedPassword = resultSet.getString("Password");
        return checkPassword(password, hashedPassword);
      }
    } catch (SQLException e) {
      logger.error("Database error during authentication: " + e.getMessage());
      e.printStackTrace();
    }
    return false;
  }

  private void proceedAsGuest() {
    logger.info("Proceeding as guest user");
    openMainApplication("guest");
  }

  private void openMainApplication(String username) {
    try {
      FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/RunwayView.fxml"));
      Parent root = loader.load();

      Stage mainStage = new Stage();
      Scene scene = new Scene(root, 1350, 800); // Set dimensions
      mainStage.setScene(scene);
      mainStage.setTitle("Runway View");

      mainStage.centerOnScreen();
      mainStage.show();

      Stage loginStage = (Stage) loginButton.getScene().getWindow();
      loginStage.close();
    } catch (IOException e) {
      logger.error("Failed to load RunwayView.fxml: " + e.getMessage());
      e.printStackTrace();
    }
  }

  private String hashPassword(String password) {
    return BCrypt.hashpw(password, BCrypt.gensalt());
  }

  private boolean checkPassword(String password, String hashedPassword) {
    return BCrypt.checkpw(password, hashedPassword);
  }
}