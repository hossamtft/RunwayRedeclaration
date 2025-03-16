package uk.ac.soton.group2seg.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import uk.ac.soton.group2seg.model.Airport;
import uk.ac.soton.group2seg.model.Runway;
import uk.ac.soton.group2seg.model.LogicalRunway;
import uk.ac.soton.group2seg.model.utility.JaxbUtility;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class AddAirportFormController {
    private static final Logger logger = LogManager.getLogger(AddAirportFormController.class);

    @FXML private TextField airportIdField;
    @FXML private TextField airportNameField;
    @FXML private VBox runwaysContainer;
    @FXML private Node runway1;

    private List<RunwayInputController> runwayControllers = new ArrayList<>();
    private Consumer<Airport> onAirportAddedCallback;
    private static final String DB_URL = "jdbc:sqlite:runwayredeclaration.sqlite";

    /**
     * Initialize the controller
     */
    @FXML
    public void initialize() {
        // Get the controller for the first runway form
        RunwayInputController firstRunwayController = getRunwayController(runway1);
        runwayControllers.add(firstRunwayController);
    }

    public Connection connectToDB() {
        try {
            return DriverManager.getConnection(DB_URL);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Add another runway input form
     */
    @FXML
    public void addRunwayForm() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/RunwayInputForm.fxml"));
            Node runwayForm = loader.load();

            // Get the controller and add it to our list
            RunwayInputController controller = loader.getController();
            runwayControllers.add(controller);

            // Enable the remove button for this runway (since it's not the first one)
            controller.enableRemoveButton(this::handleRunwayRemoved);

            // Add to the container
            runwaysContainer.getChildren().add(runwayForm);

        } catch (IOException e) {
            logger.error("Error adding runway form", e);
            showErrorAlert("Could not add runway form", e.getMessage());
        }
    }

    /**
     * Handle removal of a runway form
     *
     * @param controller The controller of the runway form to remove
     */
    private void handleRunwayRemoved(RunwayInputController controller) {
        // Get the runway node
        Node runwayNode = controller.getRoot();

        // Remove from the container
        runwaysContainer.getChildren().remove(runwayNode);

        // Remove from our list
        runwayControllers.remove(controller);
    }

    /**
     * Save the airport and its runways
     */
    @FXML
    public void handleSave() {
        if (!validateForm()) {
            return;
        }
        try {
            // Create the airport using the (id, name) constructor
            String airportId = airportIdField.getText().trim();
            String airportName = airportNameField.getText().trim();
            Airport airport = new Airport(airportId, airportName);

            // Add runways to the airport
            for (RunwayInputController controller : runwayControllers) {
                Runway runway = controller.getRunway();
                if (runway != null) {
                    airport.addRunway(runway);
                }
            }
            try {
                try (Connection connection = connectToDB()) {
                    String airportQuery = "INSERT INTO Airports (AirportID, AirportName) VALUES(?, ?)";
                    PreparedStatement airportStatement = connection.prepareStatement(airportQuery);
                    airportStatement.setString(1, airportId);
                    airportStatement.setString(2, airportName);
                    int airportRowsAffected = airportStatement.executeUpdate();
                    if (airportRowsAffected == 1) {
                        logger.info("Airport {} added successfully", airportName);
                    }
                    for (Runway runwayX : airport.getRunwayList()) {
                        String runwayQuery = "INSERT INTO Runways (RunwayID, AirportID) VALUES(?, ?)";
                        PreparedStatement runwayStatement = connection.prepareStatement(runwayQuery);
                        runwayStatement.setString(1, runwayX.getName());
                        runwayStatement.setString(2, airportId);
                        int runwayRowsAffected = runwayStatement.executeUpdate();
                        if (runwayRowsAffected == 1) {
                            logger.info("Runway {} added successfully", runwayX.getName());
                        }
                        for (LogicalRunway logicalRunway : runwayX.getLogicalRunways()) {
                            String logicalRunwayQuery = "INSERT INTO LogicalRunways (LogicalRunwayName, TORA, TODA, ASDA, LDA, RunwayID, AirportID) VALUES(?, ?, ?, ?, ?, ?, ?)";
                            PreparedStatement logicalRunwayStatement = connection.prepareStatement(logicalRunwayQuery);
                            logicalRunwayStatement.setString(1, logicalRunway.getName());
                            logicalRunwayStatement.setInt(2, logicalRunway.getTora());
                            logicalRunwayStatement.setInt(3, logicalRunway.getToda());
                            logicalRunwayStatement.setInt(4, logicalRunway.getAsda());
                            logicalRunwayStatement.setInt(5, logicalRunway.getLda());
                            logicalRunwayStatement.setString(6, runwayX.getName());
                            logicalRunwayStatement.setString(7, airportId);
                            int logicalRunwayRowsAffected = logicalRunwayStatement.executeUpdate();
                            if (logicalRunwayRowsAffected > 0) {
                                logger.info("{} LogicalRunways added successfully", logicalRunway.getName());
                            }
                        }
                    }


                }

                // Example: Using JaxbUtility - adjust based on your actual implementation
                JaxbUtility jaxbUtility = new JaxbUtility();
                jaxbUtility.addAirport(airport);
            } catch (Exception e) {
                logger.error("Failed to save airport data", e);
                throw new RuntimeException("Failed to save airport: " + e.getMessage(), e);
            }

            // Notify caller that airport was added
            if (onAirportAddedCallback != null) {
                onAirportAddedCallback.accept(airport);
            }

            // Close the window
            closeWindow();

        } catch (Exception e) {
            logger.error("Error saving airport", e);
            showErrorAlert("Error Saving Airport", "Failed to save airport: " + e.getMessage());
        }
    }
    /**
     * Cancel adding the airport
     */
    @FXML
    public void handleCancel() {
        closeWindow();
    }

    private boolean validateForm() {
        if (airportIdField.getText().trim().isEmpty()) {
            showErrorAlert("Validation Error", "Airport ID is required");
            return false;
        }

        if (airportNameField.getText().trim().isEmpty()) {
            showErrorAlert("Validation Error", "Airport name is required");
            return false;
        }

        // Validate that at least one runway is valid
        boolean hasValidRunway = false;
        for (RunwayInputController controller : runwayControllers) {
            if (controller.isValid()) {
                hasValidRunway = true;
                break;
            }
        }

        if (!hasValidRunway) {
            showErrorAlert("Validation Error", "At least one valid runway is required");
            return false;
        }

        return true;
    }

    /**
     * Set a callback to be called when an airport is added
     *
     * @param callback The callback
     */
    public void setOnAirportAddedCallback(Consumer<Airport> callback) {
        this.onAirportAddedCallback = callback;
    }

    /**
     * Close the window
     */
    private void closeWindow() {
        Stage stage = (Stage) airportNameField.getScene().getWindow();
        stage.close();
    }

    /**
     * Helper to get the runway controller from a loaded runway form
     *
     * @param runwayNode The runway form node
     * @return The controller
     */
    private RunwayInputController getRunwayController(Node runwayNode) {
        return (RunwayInputController) runwayNode.getProperties().get("controller");
    }

    /**
     * Show an error alert
     *
     * @param title The alert title
     * @param message The alert message
     */
    private void showErrorAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(title);
        alert.setContentText(message);
        alert.showAndWait();
    }
}