package uk.ac.soton.group2seg.controller;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import java.util.List;
import java.util.Map;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.ac.soton.group2seg.model.*;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import uk.ac.soton.group2seg.BCrypt.BCrypt;
import uk.ac.soton.group2seg.model.LogicalRunway;
import uk.ac.soton.group2seg.model.ModelState;
import uk.ac.soton.group2seg.model.Obstacle;
import java.sql.*;

/**
 * The main controller for the application
 */
public class MainController {

    private final Logger logger = LogManager.getLogger(this.getClass());
    private static final String DB_URL = "jdbc:sqlite:runwayredeclaration.sqlite";
    @FXML
    public Label currentSessionLabel;
    private String username;
    private String userRole;
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
    public Button addPreDefObstButton;
    @FXML
    public Label disabledMessageLabel;
    @FXML
    public Button toggleDashboardButton;
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
    @FXML
    private SplitPane splitPane;

    private TopDownController topDownController;
    private SideViewController sideViewController;
    private ModelState modelState;
    private Calculator calculator;

    private HashMap<String, String> obstacleList;
    private Obstacle currentObstacle;

    private Airport currentAirport;


    /**
     * Initialise the application
     */
    @FXML
    public void initialize() {
        topDownController = TopDownController.getInstance();
        sideViewController = SideViewController.getInstance();
        modelState = new ModelState();
        updateAirportList();
        topDownController.setModelState(modelState);
        sideViewController.setModelState(modelState);

        modelState.currentAirportProperty().addListener((obs, oldVal, newVal) -> updateAccessLevelsAndButtons());
        modelState.currentRunwayProperty().addListener((obs, oldVal, newVal) ->  updateAccessLevelsAndButtons());


        // Disable mouse events on the divider node
        Platform.runLater(() -> {
            splitPane.lookupAll(".split-pane-divider").forEach(div -> {
                div.setMouseTransparent(true); // makes it non-interactive
            });
        });

        asdaTextArea.setText("No calculation has been performed yet.\nPlease select an airport, runway, and add an obstacle to see ASDA calculation breakdown.");
        toraTextArea.setText("No calculation has been performed yet.\nPlease select an airport, runway, and add an obstacle to see TORA calculation breakdown.");
        todaTextArea.setText("No calculation has been performed yet.\nPlease select an airport, runway, and add an obstacle to see TODA calculation breakdown.");
        ldaTextArea.setText("No calculation has been performed yet.\nPlease select an airport, runway, and add an obstacle to see LDA calculation breakdown.");
        disabledMessageLabel.setVisible(false);
        addObstButton.setDisable(true);
        addPreDefObstButton.setDisable(true);

    }


    public void setUserCredentials(String username, String userRole) {
        this.username = username;
        this.userRole = userRole;
        logger.info("Logged in as " + username + " with role " + userRole);

        if (currentSessionLabel != null) {
            currentSessionLabel.setText("Signed in as: " + username + ", " + userRole);
        }
    }

    public void updateAccessLevelsAndButtons() {
        // Get the current state of airport and runway selection
        boolean airportSelected = modelState.getCurrentAirport() != null;
        boolean runwaySelected = modelState.getCurrentRunway() != null;
        boolean enableButtons = airportSelected && runwaySelected;

        // Check if the user role is set
        if (userRole == null) {
            logger.warn("User role is not set; skipping access level adjustments.");
            return;
        }

        addObstButton.setDisable(!enableButtons);
        addPreDefObstButton.setDisable(!enableButtons);

        // Different roles will have access to different buttons
        switch (userRole.toLowerCase()) {
            case "admin":
                disabledMessageLabel.setVisible(!enableButtons);
                logger.info("Admin role: All features enabled.");
                break;
            case "atc":
            case "regulator":
                addObstButton.setVisible(false);
                addPreDefObstButton.setVisible(false);
                toggleDashboardButton.setVisible(false);
                disabledMessageLabel.setVisible(false);
                logger.info(userRole + " role: Obstacle and dashboard features disabled.");
                break;

            case "ground crew":
                // Ground crew can add obstacles
                addObstButton.setVisible(true);
                addPreDefObstButton.setVisible(true);
                toggleDashboardButton.setVisible(false);
                disabledMessageLabel.setVisible(!enableButtons);
                logger.info("Ground Crew role: Dashboard feature disabled.");
                break;

            default:
                // Unknown role, treated as a guest
//                addObstButton.setVisible(false);
//                addPreDefObstButton.setVisible(false);
//                toggleDashboardButton.setVisible(false);
//                disabledMessageLabel.setVisible(false);
//                logger.info("Unknown role (" + userRole + "): Obstacle and dashboard features disabled.");
                disabledMessageLabel.setVisible(!enableButtons);
                logger.info("Admin role: All features enabled.");
                break;
        }
    }


    public Connection connectToDatabase() {
        try {
            return DriverManager.getConnection(DB_URL);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
    private void updateAirportList() {
        if (airportListCombo.getItems() == null) {
            airportListCombo.setItems(FXCollections.observableArrayList());
        } else {
            airportListCombo.getItems().clear();
        }
        airportListCombo.getItems()
            .addAll(FXCollections.observableArrayList(modelState.getAirportList().keySet()));
        airportListCombo.getItems().add("Add New Airport");
    }

    /**
     * Handle button press for airport loading
     */
    public void handleAirportSelection() {
        String selectedAirport = airportListCombo.getValue();
        logger.info("Selected airport");

        if (selectedAirport == null) {
            logger.debug("No selected airport");
            return;
        }
        if (selectedAirport.equals("Add New Airport")) {
            openAddAirportForm();
            return;
        }

        logger.info("Loading airport: " + selectedAirport);
        modelState.loadAirport(selectedAirport);
        this.currentAirport = modelState.getCurrentAirport();

        runwayListCombo.getItems().clear();
        runwayListCombo.getItems()
                .addAll(FXCollections.observableArrayList(modelState.getRunways()));
        runwayListCombo.setVisible(true);

        runwayLoadButton.setVisible(true);
    }

    private void openAddAirportForm() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/AddAirportForm.fxml"));
            Parent root = loader.load();

            AddAirportFormController controller = loader.getController();

            // Set up a callback when the airport is successfully added
            controller.setOnAirportAddedCallback(airport -> {
                // Refresh the airport list in the combo box
                modelState.updateAirportList();
                updateAirportList();
            });

            Stage stage = new Stage();
            stage.setTitle("Add New Airport");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();

        } catch (IOException e) {
            logger.error("Error loading add airport form", e);
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("Could not open the add airport form");
            alert.setContentText("An error occurred while trying to open the add airport form: " + e.getMessage());
            alert.showAndWait();
        }
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

        initialiseTables();
        drawRunway();
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

        ObservableList<LogicalRunway> runwayData = FXCollections.observableArrayList(
                modelState.getCurrentRunway().getLogicalRunways());

        originalTableView.setItems(runwayData);
        currentTableView.setItems(runwayData);

    }

    public void updateTables() {
        // Update the tables after recalculating the runway with the obstacle
        currentTableView.getItems().clear();
        originalTableView.getItems().clear();
        originalTableView.setItems(
                FXCollections.observableArrayList(modelState.getCurrentRunway().getLogicalRunways()));
        currentTableView.setItems(
                FXCollections.observableArrayList(modelState.getCurrentRunway().getLogicalRunways()));

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

            if (modelState.getCurrentRunway() != null) {
                Runway currentRunway = modelState.getCurrentRunway();
                controller.setRunways(currentRunway);
            }

            // Show the dialog and wait until the user closes it
            dialogStage.showAndWait();

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void loadPredefinedObstacleForm() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/view/PredefinedObstacleList.fxml"));
            Parent root = loader.load();

            // Get the controller
            PredefinedObstacleController controller = loader.getController();
            controller.setMainController(this);

            if (modelState.getCurrentRunway() != null) {
                controller.setRunways(modelState.getCurrentRunway());
            }

            // Create and show the stage
            Stage dialogStage = new Stage();
            dialogStage.setTitle("Select Predefined Obstacle");
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.initOwner(addObstButton.getScene().getWindow());
            dialogStage.setScene(new Scene(root));

            controller.setDialogStage(dialogStage);
            dialogStage.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void addObstacle(Obstacle obstacle) {
        logger.debug("Adding obstacle: " + obstacle);
        System.out.println("Adding obstacle");
        logger.log(Level.DEBUG, "Adding obstacle");

        modelState.setObstacle(obstacle);

        // Recalculate the runway after the obstacle is added
        calculator.redeclareRunway(obstacle);
        topDownController.addObstacle(obstacle);
        sideViewController.addObstacle(obstacle);

        updateTables();

    }

    private void drawRunway() {
        topDownController.updateRunway();
        sideViewController.updateRunway();
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

        usernameField.textProperty().addListener((obs, oldVal, newVal) ->
            checkFields(usernameField, passwordField, roleDropdown, airportIDField, submitButton));
        passwordField.textProperty().addListener((obs, oldVal, newVal) ->
            checkFields(usernameField, passwordField, roleDropdown, airportIDField, submitButton));
        roleDropdown.valueProperty().addListener((obs, oldVal, newVal) ->
            checkFields(usernameField, passwordField, roleDropdown, airportIDField, submitButton));
        airportIDField.textProperty().addListener((obs, oldVal, newVal) ->
            checkFields(usernameField, passwordField, roleDropdown, airportIDField, submitButton));

        submitButton.setOnAction(event -> {
            String username = usernameField.getText();
            String password = passwordField.getText();
            String role = roleDropdown.getValue();
            String airportID = airportIDField.getText();

            if (validateInputs(username, password, role, airportID)) {
                try (Connection connection = connectToDatabase()) {
                    String hashedPassword = hashPassword(password);
                    String query = "INSERT INTO Users (Username, Password, Role, AirportID) VALUES (?, ?, ?, ?)";
                    PreparedStatement ps = connection.prepareStatement(query);
                    ps.setString(1, username);
                    ps.setString(2, hashedPassword);
                    ps.setString(3, role);
                    ps.setString(4, airportID);
                    registerStage.close();
                    logger.info("User registered - Username: {}, Role: {}, Airport ID: {}", username, role, airportID);
                    int rowsAffected = ps.executeUpdate();
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
        scene.getStylesheets().add(getClass().getResource("/css/form.css").toExternalForm());
        registerStage.showAndWait();
    }

    private void checkFields(TextField username, PasswordField password, ComboBox<String> role, TextField airportID, Button submitButton) {
        submitButton.setDisable(username.getText().isEmpty()
            || password.getText().isEmpty()
            || role.getValue() == null
            || airportID.getText().isEmpty());
    }

    public boolean validateInputs(String username, String password, String role, String airportID) {
        if (username.length() < 6 || !usernameUnique(username)) {
            showError("Username must be unique and at least 6 characters long.");
            return false;
        }

        if (!validatePassword(password)) {
            return false;
        }

        if (role == null) {
            showError("Please select a role.");
            return false;
        }

        if (airportID.isEmpty() || !validAirport(airportID)) {
            showError("Provide a valid UK Aiport ID!");
            return false;
        }

        return true;
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

    private boolean validatePassword(String password) {
        if (password.length() < 8 || !password.matches(".*[A-Z].*") || !password.matches(".*[a-z].*")
            || !password.matches(".*\\d.*") || !password.matches(".*[!@#$%^&*(),.?\":{}|<>_-].*")) {
            showError("Password must be at least 8 characters long and include upper/lowercase, number, and symbol.");
            return false;
        }
        return true;
    }

    public boolean usernameUnique(String username) {
        String query = "SELECT COUNT(*) FROM Users WHERE Username = ?";
        try (Connection conn = connectToDatabase();
            PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setString(1, username);
            ResultSet rs = ps.executeQuery();
            return rs.next() && rs.getInt(1) == 0;
        } catch (SQLException e) {
            logger.error("DB error when checking username uniqueness: " + e.getMessage());
            return false;
        }
    }

    public boolean validAirport(String airportID) {
        String query = "SELECT COUNT(*) FROM Airports WHERE AirportID = ?";
        try (Connection conn = connectToDatabase();
            PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setString(1, airportID);
            ResultSet rs = ps.executeQuery();
            return rs.next() && rs.getInt(1) != 0;
        } catch (SQLException e) {
            logger.error("DB error when checking airport existence: " + e.getMessage());
            return false;
        }
    }

    public String hashPassword(String password) {
        return BCrypt.hashpw(password, BCrypt.gensalt());
    }

    public void setCurrentAirport(Airport airport) {
        this.currentAirport = airport;
    }

    @FXML
    private void handleExportAsXml() {

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save XML File");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("XML Files", "*.xml"));
        File file = fileChooser.showSaveDialog(null);

        if (file != null) {
            try {
                exportToXml(file);
            } catch (Exception e) {
                e.printStackTrace(); // Or show error dialog
            }
        }
    }

    private void exportToXml(File file) throws Exception {
        DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder docBuilder = docFactory.newDocumentBuilder();

        // Root element
        Document doc = docBuilder.newDocument();
        Element rootElement = doc.createElement("AirportData");
        doc.appendChild(rootElement);

        Element airport = doc.createElement("Airport");
        airport.setAttribute("name", currentAirport.getName());
        rootElement.appendChild(airport);

        // add selected runway
        Runway selectedRunway = modelState.getCurrentRunway();
        Element runway = doc.createElement("Runway");
        runway.setAttribute("name", selectedRunway.getName());
        airport.appendChild(runway);

        if (modelState.getObstacle() != null) {
            Obstacle o = modelState.getObstacle();
            Element obstacle = doc.createElement("Obstacle");
            //obstacle.setAttribute("name", o.getName());
            obstacle.setAttribute("height", String.valueOf(o.getHeight()));
            // Add more attributes as needed
            obstacle.setAttribute("distToHigherThreshold", String.valueOf(o.getDistHigherThreshold()));
            obstacle.setAttribute("distToLowerThreshold", String.valueOf(o.getDistLowerThreshold()));
            obstacle.setAttribute("distToCentreline", String.valueOf(o.getCentreOffset()));
            rootElement.appendChild(obstacle);
        }

// Create the Transformer to write the XML
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");

// Write the content to the file
        DOMSource source = new DOMSource(doc);
        StreamResult result = new StreamResult(file);
        transformer.transform(source, result);
    }

    /**
     * A button available exclusively to users with the admin role. It handles user registration,
     * as it's more appropriate for new airport staff to be assigned their credentials
     * rather than creating accounts themselves.
     * @param event
     */
    @FXML
    public void openAdminDashboard(ActionEvent event) {
        Stage dashboardStage = new Stage();
        dashboardStage.initModality(Modality.APPLICATION_MODAL);
        dashboardStage.setTitle("Admin Dashboard");

        Label titleLabel = new Label("Admin Dashboard");
        titleLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold;");
        titleLabel.setMaxWidth(Double.MAX_VALUE);
        titleLabel.setAlignment(Pos.CENTER);

        Button registerButton = new Button("Register New User");
        Button editUsersButton = new Button("Edit Existing Users");

        registerButton.getStyleClass().add("admin-buttons");
        editUsersButton.getStyleClass().add("admin-buttons");

        registerButton.setOnAction(e -> loadRegistrationForm());
        editUsersButton.setOnAction(e -> openEditUserSearchDialog());

        VBox layout = new VBox(20, titleLabel, registerButton, editUsersButton);
        layout.setAlignment(Pos.CENTER);
        layout.setPadding(new Insets(50));
        layout.setPrefWidth(600);
        layout.setPrefHeight(400);

        Scene scene = new Scene(layout);
        scene.getStylesheets().add(getClass().getResource("/css/form.css").toExternalForm());

        dashboardStage.setScene(scene);
        dashboardStage.showAndWait();
    }

    /**
     * Two ways of searching for a record to edit - both using the username.
     */
    public void openEditUserSearchDialog() {
        Stage searchStage = new Stage();
        searchStage.initModality(Modality.APPLICATION_MODAL);
        searchStage.setTitle("Edit User - Search");

        // Manual search - by typing
        TextField manualField = new TextField();
        manualField.setPromptText("Enter username");

        Button manualSearchButton = new Button("Search");

        HBox manualBox = new HBox(10, manualField, manualSearchButton);

        // Username retrieval - pick a username from the list
        TextField listPromptField = new TextField("Or select a username from the list below:");
        listPromptField.setEditable(false);

        ListView<String> userList = new ListView<>();
        userList.setPrefHeight(150);
        userList.setItems(fetchUsernamesFromDatabase());

        Button listSelectButton = new Button("OK");
        VBox listBox = new VBox(10, listPromptField, userList, listSelectButton);

        VBox layout = new VBox(20, new Label("Search for a username:"), manualBox, new Separator(), listBox);
        layout.setPadding(new Insets(20));

        manualSearchButton.setOnAction(e -> {
            String username = manualField.getText();
            if (usernameExists(username)) {
                searchStage.close();
                openEditOptionsDialog(username);
            } else {
                showError("Username not found.");
            }
        });

        listSelectButton.setOnAction(e -> {
            String selected = userList.getSelectionModel().getSelectedItem();
            if (selected != null) {
                searchStage.close();
                openEditOptionsDialog(selected);
            } else {
                showError("No user selected.");
            }
        });

        Scene scene = new Scene(layout);
        scene.getStylesheets().add(getClass().getResource("/css/form.css").toExternalForm());
        searchStage.setScene(scene);
        searchStage.showAndWait();
    }

    /**
     * Retrieve all the usernames from the database. Admin can choose the username easily if the
     * database is small, no need for manual searching.
     * @return list of usernames
     */
    private ObservableList<String> fetchUsernamesFromDatabase() {
        ObservableList<String> usernames = FXCollections.observableArrayList();
        String query = "SELECT Username FROM Users";
        try (Connection conn = connectToDatabase();
            PreparedStatement ps = conn.prepareStatement(query);
            ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                usernames.add(rs.getString("Username"));
            }
        } catch (SQLException e) {
            logger.error("Failed to fetch usernames: " + e.getMessage());
        }
        return usernames;
    }

    /**
     * Looks for the string provided as an argument in the username column of the database
     * @param username
     * @return
     */
    private boolean usernameExists(String username) {
        String query = "SELECT 1 FROM Users WHERE Username = ?";
        try (Connection conn = connectToDatabase();
            PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setString(1, username);
            ResultSet rs = ps.executeQuery();
            return rs.next();
        } catch (SQLException e) {
            logger.error("Error checking username existence: " + e.getMessage());
            return false;
        }
    }


    /**
     * Allows the admin to choose which fields of the record they want to change.
     * @param username
     */
    public void openEditOptionsDialog(String username) {
        Stage optionStage = new Stage();
        optionStage.initModality(Modality.APPLICATION_MODAL);
        optionStage.setTitle("Edit User - Choose Fields");
        optionStage.setMinWidth(350);


        Label label = new Label("Tick the fields you want to edit:");
        label.setWrapText(true);

        CheckBox usernameCheck = new CheckBox("Username");
        CheckBox passwordCheck = new CheckBox("Password");
        CheckBox roleCheck = new CheckBox("Role");
        CheckBox airportCheck = new CheckBox("Airport ID");

        Button okButton = new Button("OK");

        okButton.setOnAction(e -> {
            // Check if at least one checkbox is selected
            if (!usernameCheck.isSelected() && !passwordCheck.isSelected() &&
                !roleCheck.isSelected() && !airportCheck.isSelected()) {
                showError("Please select at least one field to edit.");
            } else {
                optionStage.close();
                openFieldEditingDialog(
                    username,
                    usernameCheck.isSelected(),
                    passwordCheck.isSelected(),
                    roleCheck.isSelected(),
                    airportCheck.isSelected()
                );
            }
        });

        VBox content = new VBox(10, label, usernameCheck, passwordCheck, roleCheck, airportCheck, okButton);
        content.setPadding(new Insets(20));
        content.setAlignment(Pos.CENTER);

        Region filler = new Region();
        VBox.setVgrow(filler, Priority.ALWAYS);

        content.getChildren().add(filler);

        Scene scene = new Scene(content);
        scene.getStylesheets().add(getClass().getResource("/css/form.css").toExternalForm());

        optionStage.setScene(scene);
        optionStage.show();
    }


    /**
     * Opens the dialog to allow the admin to input new values for the fields that they checked to edit
     * @param currentUsername
     * @param editUsername
     * @param editPassword
     * @param editRole
     * @param editAirportID
     */
    public void openFieldEditingDialog(String currentUsername, boolean editUsername, boolean editPassword,
        boolean editRole, boolean editAirportID) {
        Stage inputStage = new Stage();
        inputStage.initModality(Modality.APPLICATION_MODAL);
        inputStage.setTitle("Edit User - Input Values");
        inputStage.setMinWidth(350);

        VBox fieldsBox = new VBox(10);
        Map<String, Control> fieldInputs = new HashMap<>();

        PasswordField confirmPasswordField = null;

        if (editUsername) {
            TextField newUserField = new TextField();
            newUserField.setPromptText("Enter new username");
            fieldInputs.put("username", newUserField);
            fieldsBox.getChildren().add(new Label("New Username for " + currentUsername + ":"));
            fieldsBox.getChildren().add(newUserField);
        }

        if (editPassword) {
            PasswordField newPasswordField = new PasswordField();
            newPasswordField.setPromptText("Enter new password");
            confirmPasswordField = new PasswordField();
            confirmPasswordField.setPromptText("Confirm new password");
            fieldInputs.put("password", newPasswordField);
            fieldsBox.getChildren().add(new Label("New Password for " + currentUsername + ":"));
            fieldsBox.getChildren().add(newPasswordField);
            fieldsBox.getChildren().add(new Label("Confirm New Password for " + currentUsername + ":"));
            fieldsBox.getChildren().add(confirmPasswordField);
        }

        if (editRole) {
            ComboBox<String> roleBox = new ComboBox<>();
            roleBox.getItems().addAll("Admin", "ATC", "Ground Crew", "Regulator");
            roleBox.setPromptText("Select new role");
            fieldInputs.put("role", roleBox);
            fieldsBox.getChildren().add(new Label("New Role for " + currentUsername + ":"));
            fieldsBox.getChildren().add(roleBox);
        }

        if (editAirportID) {
            TextField newAirportField = new TextField();
            newAirportField.setPromptText("Enter new Airport ID");
            fieldInputs.put("airportID", newAirportField);
            fieldsBox.getChildren().add(new Label("New Airport ID for " + currentUsername + ":"));
            fieldsBox.getChildren().add(newAirportField);
        }

        Button submitButton = new Button("Submit");
        PasswordField finalConfirmPasswordField = confirmPasswordField;
        submitButton.setOnAction(e -> {
            try (Connection conn = connectToDatabase()) {
                StringBuilder sql = new StringBuilder("UPDATE Users SET ");
                List<String> updates = new ArrayList<>();
                List<Object> values = new ArrayList<>();

                // used to display a meaningful alert if a field is empty
                boolean anyEmpty =
                    (fieldInputs.containsKey("username") && ((TextField) fieldInputs.get("username")).getText().trim().isEmpty()) ||
                    (fieldInputs.containsKey("password") && ((PasswordField) fieldInputs.get("password")).getText().trim().isEmpty()) ||
                    (finalConfirmPasswordField != null && finalConfirmPasswordField.getText().trim().isEmpty()) ||
                    (fieldInputs.containsKey("role") && ((ComboBox<?>) fieldInputs.get("role")).getValue() == null) ||
                    (fieldInputs.containsKey("airportID") && ((TextField) fieldInputs.get("airportID")).getText().trim().isEmpty());

                if (anyEmpty) {
                    showError("You have not selected a role or left one or more fields empty.");
                    return;
                }

                if (fieldInputs.containsKey("username")) {
                    String newUsername = ((TextField) fieldInputs.get("username")).getText();
                    if (!usernameUnique(newUsername)) {
                        showError("Username already exists.");
                        return;
                    }
                    updates.add("Username = ?");
                    values.add(newUsername);
                }

                if (fieldInputs.containsKey("password")) {
                    String newPassword = ((PasswordField) fieldInputs.get("password")).getText();
                    String confirmPassword = finalConfirmPasswordField.getText();

                    if (!validatePassword(newPassword)) return;

                    if (!newPassword.equals(confirmPassword)) {
                        showError("Passwords do not match.");
                        return;
                    }

                    updates.add("Password = ?");
                    values.add(hashPassword(newPassword));
                }

                if (fieldInputs.containsKey("role")) {
                    updates.add("Role = ?");
                    values.add(((ComboBox<?>) fieldInputs.get("role")).getValue());
                }

                if (fieldInputs.containsKey("airportID")) {
                    String newID = ((TextField) fieldInputs.get("airportID")).getText();
                    if (!validAirport(newID)) {
                        showError("Invalid Airport ID.");
                        return;
                    }
                    updates.add("AirportID = ?");
                    values.add(newID);
                }

                if (updates.isEmpty()) {
                    showError("No fields selected.");
                    return;
                }

                sql.append(String.join(", ", updates));
                sql.append(" WHERE Username = ?");

                PreparedStatement ps = conn.prepareStatement(sql.toString());
                for (int i = 0; i < values.size(); i++) {
                    ps.setObject(i + 1, values.get(i));
                }
                ps.setString(values.size() + 1, currentUsername);
                ps.executeUpdate();

                inputStage.close();
                logger.info("User updated successfully.");
            } catch (SQLException ex) {
                logger.error("Database error during update: " + ex.getMessage());
                showError("Error updating user.");
            }
        });

        VBox layout = new VBox(15, fieldsBox, submitButton);
        layout.setPadding(new Insets(20));

        Scene scene = new Scene(layout);
        scene.getStylesheets().add(getClass().getResource("/css/form.css").toExternalForm());
        inputStage.setScene(scene);
        inputStage.showAndWait();
    }

}



