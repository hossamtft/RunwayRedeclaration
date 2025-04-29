package uk.ac.soton.group2seg.controller;

import javafx.scene.Node;
import javafx.scene.SnapshotParameters;
import javafx.scene.image.WritableImage;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import java.awt.image.BufferedImage;
import javafx.embed.swing.SwingFXUtils;

import javafx.stage.FileChooser;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextInputDialog;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import javafx.scene.control.TextArea;

import java.util.List;
import java.util.Map;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.Event;
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
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.pdfbox.pdmodel.graphics.image.LosslessFactory;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.w3c.dom.NodeList;
import uk.ac.soton.group2seg.model.*;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.*;
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

    @FXML
    private TextArea calculationsTextArea;

    private TopDownController topDownController;
    private SideViewController sideViewController;
    private ModelState modelState;
    private Calculator calculator;

    private HashMap<String, String> obstacleList;
    private Obstacle currentObstacle;

    private Airport currentAirport;


    /**
     * Program specification method
     * @param event
     * @throws URISyntaxException
     * @throws IOException
     */
    public void About(Event event) throws URISyntaxException, IOException {
        LoginController loginController=new LoginController();
        loginController.About((ActionEvent) event);
    }
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
        modelState.addObstacle(obstacle);

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
        Document doc = docBuilder.newDocument();

        Element rootElem = doc.createElement("fullExport");
        doc.appendChild(rootElem);

        // === Airport ===
        Element airportElem = doc.createElement("airport");
        rootElem.appendChild(airportElem);

        Element nameElem = doc.createElement("name");
        nameElem.appendChild(doc.createTextNode(currentAirport.getName()));
        airportElem.appendChild(nameElem);

        Element idElem = doc.createElement("id");
        idElem.appendChild(doc.createTextNode("CSTM"));
        airportElem.appendChild(idElem);

        Element runwaysElem = doc.createElement("runways");
        airportElem.appendChild(runwaysElem);

        for (Runway runway : currentAirport.getRunwayList()) {
            Element runwayElem = doc.createElement("runway");
            runwaysElem.appendChild(runwayElem);

            Element runwayNameElem = doc.createElement("runwayName");
            runwayNameElem.appendChild(doc.createTextNode(runway.getName()));
            runwayElem.appendChild(runwayNameElem);

            // Lower Logical Runway
            LogicalRunway lower = runway.getLowerRunway();
            if (lower != null) {
                runwayElem.appendChild(createLogicalRunwayElement(doc, lower));
            }

            // Higher Logical Runway
            LogicalRunway higher = runway.getHigherRunway();
            if (higher != null) {
                runwayElem.appendChild(createLogicalRunwayElement(doc, higher));
            }
        }

        // === Obstacles ===
        Element obstacleListElem = doc.createElement("obstacleList");
        rootElem.appendChild(obstacleListElem);

        for (Obstacle o : modelState.getObstacleList()) {
            Element obstacleElem = doc.createElement("obstacle");

            Element name = doc.createElement("name");
            name.appendChild(doc.createTextNode(o.getName()));
            obstacleElem.appendChild(name);

            Element height = doc.createElement("height");
            height.appendChild(doc.createTextNode(String.valueOf(o.getHeight())));
            obstacleElem.appendChild(height);

            Element distToLower = doc.createElement("distToLowerThreshold");
            distToLower.appendChild(doc.createTextNode(String.valueOf(o.getDistLowerThreshold())));
            obstacleElem.appendChild(distToLower);

            Element distToHigher = doc.createElement("distToHigherThreshold");
            distToHigher.appendChild(doc.createTextNode(String.valueOf(o.getDistHigherThreshold())));
            obstacleElem.appendChild(distToHigher);

            Element centreOffset = doc.createElement("centreOffset");
            centreOffset.appendChild(doc.createTextNode(String.valueOf(o.getCentreOffset())));
            obstacleElem.appendChild(centreOffset);

            obstacleListElem.appendChild(obstacleElem);
        }

        // === Write to file ===
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");

        DOMSource source = new DOMSource(doc);
        StreamResult result = new StreamResult(file);
        transformer.transform(source, result);
    }

    private Element createLogicalRunwayElement(Document doc, LogicalRunway lr) {
        Element logicalRunwayElem = doc.createElement("logicalRunway");

        Element nameElem = doc.createElement("name");
        nameElem.appendChild(doc.createTextNode(lr.getName()));
        logicalRunwayElem.appendChild(nameElem);

        Element toraElem = doc.createElement("tora");
        toraElem.appendChild(doc.createTextNode(String.valueOf(lr.getTora())));
        logicalRunwayElem.appendChild(toraElem);

        Element todaElem = doc.createElement("toda");
        todaElem.appendChild(doc.createTextNode(String.valueOf(lr.getToda())));
        logicalRunwayElem.appendChild(todaElem);

        Element asdaElem = doc.createElement("asda");
        asdaElem.appendChild(doc.createTextNode(String.valueOf(lr.getAsda())));
        logicalRunwayElem.appendChild(asdaElem);

        Element ldaElem = doc.createElement("lda");
        ldaElem.appendChild(doc.createTextNode(String.valueOf(lr.getLda())));
        logicalRunwayElem.appendChild(ldaElem);

        return logicalRunwayElem;
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

    private void writeMultiline(PDPageContentStream stream, String text) throws IOException {
        for (String line : text.split("\n")) {
            stream.showText(line);
            stream.newLine();
        }
    }

    private BufferedImage captureNodeAsImage(Node node) {
        WritableImage fxImage = node.snapshot(new SnapshotParameters(), null);
        return SwingFXUtils.fromFXImage(fxImage, null);
    }

    @FXML
    private void calculationsPDF() {
        BufferedImage topDownImage = captureNodeAsImage(topDownController.getTopDownViewNode());
        BufferedImage sideOnImage = captureNodeAsImage(sideViewController.getSideDownViewNode());

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save PDF");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF files", "*.pdf"));
        fileChooser.setInitialFileName("calculations.pdf");

        File file = fileChooser.showSaveDialog(null);
        if (file == null) return;

        try (PDDocument document = new PDDocument()) {
            PDPage page = new PDPage(PDRectangle.A4);
            document.addPage(page);

            PDPageContentStream contentStream = new PDPageContentStream(document, page);
            contentStream.setFont(PDType1Font.HELVETICA, 12);
            contentStream.beginText();
            contentStream.setLeading(16f);
            contentStream.newLineAtOffset(50, 750);

            contentStream.showText("ASDA:");
            contentStream.newLine();
            writeMultiline(contentStream, asdaTextArea.getText());

            contentStream.newLine();
            contentStream.showText("TORA:");
            contentStream.newLine();
            writeMultiline(contentStream, toraTextArea.getText());

            contentStream.newLine();
            contentStream.showText("TODA:");
            contentStream.newLine();
            writeMultiline(contentStream, todaTextArea.getText());

            contentStream.newLine();
            contentStream.showText("LDA:");
            contentStream.newLine();
            writeMultiline(contentStream, ldaTextArea.getText());

            contentStream.endText();
            contentStream.close();

            PDPage imagePage = new PDPage(PDRectangle.A4);
            document.addPage(imagePage);

            PDPageContentStream imageStream = new PDPageContentStream(document, imagePage);
            float margin = 50;
            float maxImageWidth = PDRectangle.A4.getWidth() - 2 * margin;
            float maxImageHeight = 300f;

// === TOP DOWN ===
            PDImageXObject topImg = LosslessFactory.createFromImage(document, topDownImage);
            float topScale = Math.min(maxImageWidth / topDownImage.getWidth(), maxImageHeight / topDownImage.getHeight());
            float topWidth = topDownImage.getWidth() * topScale;
            float topHeight = topDownImage.getHeight() * topScale;

            float topX = (PDRectangle.A4.getWidth() - topWidth) / 2;
            float topY = PDRectangle.A4.getHeight() - topHeight - 100;

            imageStream.drawImage(topImg, topX, topY, topWidth, topHeight);

            imageStream.beginText();
            imageStream.setFont(PDType1Font.HELVETICA_BOLD, 14);
            imageStream.newLineAtOffset(margin, topY + topHeight + 15);
            imageStream.showText("Top Down View");
            imageStream.endText();

            PDImageXObject sideImg = LosslessFactory.createFromImage(document, sideOnImage);
            float sideScale = Math.min(maxImageWidth / sideOnImage.getWidth(), maxImageHeight / sideOnImage.getHeight());
            float sideWidth = sideOnImage.getWidth() * sideScale;
            float sideHeight = sideOnImage.getHeight() * sideScale;

            float sideX = (PDRectangle.A4.getWidth() - sideWidth) / 2;
            float sideY = topY - sideHeight - 80;

            imageStream.drawImage(sideImg, sideX, sideY, sideWidth, sideHeight);

            imageStream.beginText();
            imageStream.setFont(PDType1Font.HELVETICA_BOLD, 14);
            imageStream.newLineAtOffset(margin, sideY + sideHeight + 15);
            imageStream.showText("Side On View");
            imageStream.endText();

            imageStream.close();

            document.save(file);
            System.out.println("PDF exported with visuals: " + file.getAbsolutePath());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void importXML() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open Full Export XML File");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("XML Files", "*.xml"));

        File file = fileChooser.showOpenDialog(null);
        if (file == null) return;

        try {
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(file);
            doc.getDocumentElement().normalize();

            // === Airport ===
            Element airportElem = (Element) doc.getElementsByTagName("airport").item(0);
            String airportName = airportElem.getElementsByTagName("name").item(0).getTextContent();
            String airportId = airportElem.getElementsByTagName("id").item(0).getTextContent();

            Airport newAirport = new Airport(airportId, airportName);

            // === Runways ===
            NodeList runwayNodes = airportElem.getElementsByTagName("runway");
            for (int i = 0; i < runwayNodes.getLength(); i++) {
                Element runwayElem = (Element) runwayNodes.item(i);
                String runwayName = runwayElem.getElementsByTagName("runwayName").item(0).getTextContent();

                // Logical runways
                NodeList logicalRunways = runwayElem.getElementsByTagName("logicalRunway");

                Element lowerElem = (Element) logicalRunways.item(0);
                String lowerName = lowerElem.getElementsByTagName("name").item(0).getTextContent();
                int lowerTora = Integer.parseInt(lowerElem.getElementsByTagName("tora").item(0).getTextContent());
                int lowerToda = Integer.parseInt(lowerElem.getElementsByTagName("toda").item(0).getTextContent());
                int lowerAsda = Integer.parseInt(lowerElem.getElementsByTagName("asda").item(0).getTextContent());
                int lowerLda = Integer.parseInt(lowerElem.getElementsByTagName("lda").item(0).getTextContent());

                Element higherElem = (Element) logicalRunways.item(1);
                String higherName = higherElem.getElementsByTagName("name").item(0).getTextContent();
                int higherTora = Integer.parseInt(higherElem.getElementsByTagName("tora").item(0).getTextContent());
                int higherToda = Integer.parseInt(higherElem.getElementsByTagName("toda").item(0).getTextContent());
                int higherAsda = Integer.parseInt(higherElem.getElementsByTagName("asda").item(0).getTextContent());
                int higherLda = Integer.parseInt(higherElem.getElementsByTagName("lda").item(0).getTextContent());

                LogicalRunway lowerLR = new LogicalRunway(lowerName, lowerTora, lowerToda, lowerAsda, lowerLda);
                LogicalRunway higherLR = new LogicalRunway(higherName, higherTora, higherToda, higherAsda, higherLda);

                Runway runway = new Runway(lowerLR, higherLR);
                runway.setName(runwayName);
                newAirport.addRunway(runway);
            }

            newAirport.initialise();

            modelState.currentAirportProperty().set(newAirport);

            if (!newAirport.getRunwayList().isEmpty()) {
                Runway firstRunway = newAirport.getRunwayList().get(0);
                newAirport.selectRunway(firstRunway.getName());
                modelState.currentRunwayProperty().set(newAirport.getCurrentRunway());

                this.calculator = new Calculator(modelState.getCurrentRunway());
                ldaTextArea.textProperty().bind(calculator.getLdaBreakdown());
                toraTextArea.textProperty().bind(calculator.getToraBreakdown());
                todaTextArea.textProperty().bind(calculator.getTodaBreakdown());
                asdaTextArea.textProperty().bind(calculator.getAsdaBreakdown());

                initialiseTables();
                drawRunway();
            }

            Element obstacleListElem = (Element) doc.getElementsByTagName("obstacleList").item(0);
            NodeList obstacles = obstacleListElem.getElementsByTagName("obstacle");

            for (int i = 0; i < obstacles.getLength(); i++) {
                Element obsElem = (Element) obstacles.item(i);
                String name = obsElem.getElementsByTagName("name").item(0).getTextContent();
                int height = Integer.parseInt(obsElem.getElementsByTagName("height").item(0).getTextContent());
                int distLower = Integer.parseInt(obsElem.getElementsByTagName("distToLowerThreshold").item(0).getTextContent());
                int distHigher = Integer.parseInt(obsElem.getElementsByTagName("distToHigherThreshold").item(0).getTextContent());
                int centreOffset = Integer.parseInt(obsElem.getElementsByTagName("centreOffset").item(0).getTextContent());

                Obstacle obstacle = new Obstacle(height, distLower, distHigher, centreOffset, name);
                modelState.addObstacle(obstacle);
            }


            // === UI Updates ===
            airportListCombo.setValue(airportName);
            runwayListCombo.getItems().clear();
            runwayListCombo.getItems().addAll(modelState.getRunways());
            runwayListCombo.setVisible(true);
            runwayLoadButton.setVisible(true);
            runwayListCombo.setValue(modelState.getCurrentRunway().getName());
            updateAccessLevelsAndButtons();

            List<Obstacle> copyOfObstacles = new ArrayList<>(modelState.getObstacleList());
            for (Obstacle importedObstacle : copyOfObstacles) {
                modelState.setObstacle(importedObstacle);
                addObstacle(importedObstacle);
            }

            System.out.println("Import completed successfully!");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}



