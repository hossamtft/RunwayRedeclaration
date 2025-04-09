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
  public Button guestButton;

  private static final String DB_URL = "jdbc:sqlite:runwayredeclaration.sqlite";

  public Connection connectToDatabase() {
    try {
      return DriverManager.getConnection(DB_URL);
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }


  public void showError(String message) {
    Platform.runLater(() -> {
      Alert alert = new Alert(Alert.AlertType.ERROR);
      alert.setTitle("Error");
      alert.setHeaderText(null);
      alert.setContentText(message);
      alert.showAndWait();
    });
  }


  public boolean usernameUnique(String username) {
    String nameQuery = "SELECT COUNT(*) FROM Users WHERE Username = ?";

    try (Connection connection = connectToDatabase();
        PreparedStatement preparedStatement = connection.prepareStatement(nameQuery)) {

      preparedStatement.setString(1, username);
      ResultSet resultSet = preparedStatement.executeQuery();

      if (resultSet.next()) {
        return resultSet.getInt(1) == 0;
      }
    } catch (SQLException e) {
      logger.error("DB error encountered when validating username uniqueness: " + e.getMessage());
      e.printStackTrace();
    }

    return false;
  }


  public boolean validAirport(String airportID){
    String airportQuery = "SELECT COUNT(*) FROM Airports WHERE AirportID = ?";

    try(Connection connection = connectToDatabase();
      PreparedStatement preparedStatement = connection.prepareStatement(airportQuery)){

      preparedStatement.setString(1, airportID);
      ResultSet resultSet = preparedStatement.executeQuery();

      if (resultSet.next()){
          return resultSet.getInt(1 ) != 0;
      }
    } catch (SQLException e){
      logger.error("DB error encountered when checking airport existence: " + e.getMessage());
    }

      return false;
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

      String role = authenticateUser(username, password);
      if (role != null) {
        logger.info("User successfully logged in: {}", username);
        loginStage.close();
        openMainApplication(username, role);
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
    scene.getStylesheets().add(getClass().getResource("/css/form.css").toExternalForm());
    loginStage.showAndWait();
  }

  private String authenticateUser(String username, String password) {
    try (Connection connection = connectToDatabase()) {
      String query = "SELECT Password, Role FROM Users WHERE Username = ?";
      PreparedStatement preparedStatement = connection.prepareStatement(query);
      preparedStatement.setString(1, username);

      ResultSet resultSet = preparedStatement.executeQuery();
      if (resultSet.next()) {
        String hashedPassword = resultSet.getString("Password");
        String role = resultSet.getString("Role");
        if (checkPassword(password, hashedPassword)) {
          return role;
        }
      }
    } catch (SQLException e) {
      logger.error("Database error during authentication: " + e.getMessage());
      e.printStackTrace();
    }
    return null; // Return null if authentication fails
  }

  private void proceedAsGuest() {
    logger.info("Proceeding as guest user");
    openMainApplication("No Username", "guest");
  }

  private void openMainApplication(String username, String userRole) {
    try {
      FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/RunwayView.fxml"));
      Parent root = loader.load();

      MainController mainController = loader.getController();
      mainController.setUserCredentials(username, userRole);
      mainController.updateAccessAndObstacleButtonsState();


      Stage mainStage = (Stage) loginButton.getScene().getWindow();
      Scene scene = new Scene(root, 1350, 800); // Set dimensions
      mainStage.setScene(scene);
      mainStage.setTitle("Runway View");

      mainStage.centerOnScreen();

    } catch (IOException e) {
      logger.error("Failed to load RunwayView.fxml: " + e.getMessage());
      e.printStackTrace();
    }
  }

  public String hashPassword(String password) {
    return BCrypt.hashpw(password, BCrypt.gensalt());
  }

  public boolean checkPassword(String password, String hashedPassword) {
    return BCrypt.checkpw(password, hashedPassword);
  }
}