package uk.ac.soton.group2seg.controller;

import javafx.animation.RotateTransition;
import javafx.animation.ScaleTransition;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.shape.Polygon;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.util.Duration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import javafx.scene.shape.Rectangle;
import javafx.scene.paint.Color;
import uk.ac.soton.group2seg.model.LogicalRunway;
import uk.ac.soton.group2seg.model.ModelState;
import uk.ac.soton.group2seg.model.Obstacle;
import uk.ac.soton.group2seg.model.Runway;
import uk.ac.soton.group2seg.view.Arrow;

public class TopDownController {

    private static TopDownController instance = new TopDownController();
    private final Logger logger = LogManager.getLogger(this.getClass());

    @FXML
    public StackPane topDownView;

    private final double RENDER_LENGTH = 650;
    private final double RUNWAY_WIDTH = 66.0;
    private final double WIDTH_SCALE = 66/40;

    private double lowerTodaStart;
    private double upperTodaStart;

    private double CENTER_X;
    private double CENTER_Y;
    private double RUNWAY_X;

    private final Color asdaColor = Color.YELLOW;
    private final Color todaColor = Color.ORANGE;
    private final Color toraColor = Color.RED;
    private final Color ldaColor = Color.WHITE;

    private double currentZoom = 1.0;
    private final double MIN_ZOOM = 0.2;  // 20% of original size
    private final double MAX_ZOOM = 2.0;  // 200% of original size
    private final double ZOOM_DELTA = 0.1;
    private Pane zoomControls;

    private Pane viewPort;
    private Pane viewPane;
    private Pane obstaclePane;
    private Pane compass;
    private Pane linePane;
    private Pane runwayPane;
    private Button resetButton;
    private Rectangle obstacleShape;
    private Obstacle obstacle;
    private ModelState modelState;
    private Runway currentRunway = null;

    private double scale;
    private boolean initialSetupComplete = false;
    private boolean compassActive;
    private double currentRotation;

    public Node getTopDownViewNode()
    {
        return topDownView;
    }

    public TopDownController() {
        instance = this;
    }

    public static TopDownController getInstance() {
        return instance;
    }

    public void setModelState(ModelState model) {
        modelState = model;
    }

    public void initialize() {
        logger.info("Initialising TopDownController");

        viewPort = new Pane();
        viewPane = new Pane();
        runwayPane = new Pane();
        linePane = new Pane();
        obstaclePane = new Pane();

        topDownView.setMaxSize(1000, 1000);
        topDownView.setStyle("-fx-background-color: rgb(7, 51, 19)");
        topDownView.getChildren().addAll(viewPort);
        topDownView.setAlignment(Pos.CENTER);

        // Setup listeners for resize events
        topDownView.widthProperty().addListener((obs, oldVal, newVal) -> {
            logger.info("Width resize event");
            drawView();
        });
        topDownView.heightProperty().addListener((obs, oldVal, newVal) -> {
            logger.info("Height resize event");
            drawView();
        });

        viewPort.getChildren().add(viewPane);

        drawView();

        createZoomControls();

        initialSetupComplete = true;
    }

    private void createZoomControls() {
        if (zoomControls != null) {
            viewPort.getChildren().remove(zoomControls);
        }

        // Create a container for zoom controls
        zoomControls = new VBox(10);
        zoomControls.setStyle("-fx-background-color: rgba(0, 0, 0, 0.5); -fx-padding: 10; -fx-background-radius: 5;");

        // Zoom in button
        Button zoomInBtn = new Button("+");
        zoomInBtn.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-min-width: 30px;");
        zoomInBtn.setOnAction(event -> adjustZoom(ZOOM_DELTA));

        // Zoom out button
        Button zoomOutBtn = new Button("-");
        zoomOutBtn.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-min-width: 30px;");
        zoomOutBtn.setOnAction(event -> adjustZoom(-ZOOM_DELTA));

        // Reset zoom button
        Button resetZoomBtn = new Button("Reset");
        resetZoomBtn.setStyle("-fx-font-size: 12px;");
        resetZoomBtn.setOnAction(event -> resetZoom());

        // Current zoom display
        Label zoomLabel = new Label(String.format("%.0f%%", currentZoom * 100));
        zoomLabel.setStyle("-fx-text-fill: white; -fx-font-size: 12px;");
        zoomLabel.setId("zoomPercentLabel");

        // Add components to the control panel
        zoomControls.getChildren().addAll(zoomInBtn, zoomLabel, zoomOutBtn, resetZoomBtn);

        // Position zoom controls
        zoomControls.setLayoutX(20);
        zoomControls.setLayoutY(20);

        // Add to the view
        viewPort.getChildren().add(zoomControls);

        // Ensure controls stay on top
        zoomControls.toFront();
    }

    private void adjustZoom(double zoomDelta) {
        double newZoom = currentZoom + zoomDelta;
        newZoom = Math.max(MIN_ZOOM, Math.min(MAX_ZOOM, newZoom));

        if (newZoom != currentZoom) {
            double scaleFactor = newZoom / currentZoom;

            currentZoom = newZoom;

            applyZoom(scaleFactor);

            updateZoomLabel();

            logger.info(String.format("Zoom adjusted to %.2f", currentZoom));
        }
    }

    private void applyZoom(double scaleFactor) {
        ScaleTransition scaleTransition = new ScaleTransition(Duration.millis(200), viewPane);

        scaleTransition.setToX(viewPane.getScaleX() * scaleFactor);
        scaleTransition.setToY(viewPane.getScaleY() * scaleFactor);

        //Keep viewpane centered
        viewPane.setTranslateX(viewPane.getTranslateX() * scaleFactor);
        viewPane.setTranslateY(viewPane.getTranslateY() * scaleFactor);

        scaleTransition.play();
    }

    private void resetZoom() {
        currentZoom = 1.0;

        viewPane.setScaleX(1.0);
        viewPane.setScaleY(1.0);
        viewPane.setTranslateX(0);
        viewPane.setTranslateY(0);

        updateZoomLabel();

        logger.info("Zoom reset to 100%");
    }

    private void updateZoomLabel() {
        Label zoomLabel = (Label) zoomControls.lookup("#zoomPercentLabel");
        if (zoomLabel != null) {
            zoomLabel.setText(String.format("%.0f%%", currentZoom * 100));
        }
    }

    public void updateRunway() {
        this.currentRunway = modelState.getCurrentRunway();
        obstacle = null;
        obstaclePane.getChildren().clear();
        linePane.getChildren().clear();
        runwayPane.getChildren().clear();
        drawView();
    }

    private void drawView() {
        viewPane.getChildren().clear();
        viewPane.setRotate(0.0);
        compassActive = false;
        currentRotation = 0.0;

        if (!initialSetupComplete && (topDownView.getWidth() <= 0
            || topDownView.getHeight() <= 0)) {
            return; // Skip if dimensions aren't positive during initialization
        }

        CENTER_X = topDownView.getWidth() / 2;
        CENTER_Y = (topDownView.getHeight() / 2) - 33;

        logger.info(String.format("Center X = %.2f \n"
            + "Center Y = %.2f", CENTER_X, CENTER_Y));

        logger.info("Drawing runway strip");

        RUNWAY_X = CENTER_X - (RENDER_LENGTH / 2);
        Rectangle strip = new Rectangle(RUNWAY_X, CENTER_Y, RENDER_LENGTH, RUNWAY_WIDTH);
        strip.setFill(Color.DARKGREY);
        strip.setStroke(Color.WHITE);
        strip.setStrokeWidth(2);

        Line centreLine = new Line(
            RUNWAY_X + 75, CENTER_Y + RUNWAY_WIDTH / 2, RENDER_LENGTH, CENTER_Y + RUNWAY_WIDTH / 2);
        centreLine.getStrokeDashArray().addAll(15.0, 10.0);
        centreLine.setStroke(Color.WHITE);
        centreLine.setStrokeWidth(5);

        linePane.setLayoutX(RUNWAY_X);
        linePane.setLayoutY(CENTER_Y);

        obstaclePane.setLayoutX(RUNWAY_X);
        obstaclePane.setLayoutY(CENTER_Y);



        viewPane.getChildren()
            .addAll(drawClearedAndGraded(), strip, centreLine, runwayPane, linePane, obstaclePane);

        try {
            scale = RENDER_LENGTH / currentRunway.getRunwayLength();
            drawDesignators();
            drawLines();
            renderStrip();
            createCompass();
        } catch (Exception e) {
            logger.info("No runway selected yet");
        }
    }

    private void renderStrip() {
        double stopwayLower = (currentRunway.getLowerRunway().getAsda() - currentRunway.getLowerRunway().getTora()) * scale;
        double clearwayLower = (currentRunway.getLowerRunway().getToda() - currentRunway.getLowerRunway().getTora()) * scale;

        lowerTodaStart = RENDER_LENGTH + clearwayLower;
        Rectangle lowerClearway = new Rectangle(CENTER_X + 325, CENTER_Y, clearwayLower, RUNWAY_WIDTH);
        lowerClearway.setFill(Color.ORANGE);
        lowerClearway.setOpacity(0.25);

        Rectangle lowerStopway = new Rectangle(CENTER_X + 325, CENTER_Y, stopwayLower, RUNWAY_WIDTH);
        lowerStopway.setFill(Color.RED);
        lowerStopway.setOpacity(0.3);

        double stopwayHigher = (currentRunway.getHigherRunway().getAsda() - currentRunway.getHigherRunway().getTora()) * scale;
        double clearwayHigher = (currentRunway.getHigherRunway().getToda() - currentRunway.getHigherRunway().getTora()) * scale;

        upperTodaStart = 0 - clearwayHigher;
        Rectangle upperClearway = new Rectangle((CENTER_X - 325 - clearwayHigher), CENTER_Y, clearwayHigher, RUNWAY_WIDTH);
        upperClearway.setFill(Color.ORANGE);
        upperClearway.setOpacity(0.25);

        runwayPane.getChildren().addAll(lowerClearway, lowerStopway, upperClearway);
        double stripLength = currentRunway.getRunwayLength();

        logger.info("Strip length = " + stripLength +
            "\nLower runway stopway = " + stopwayLower +
            "\nHigher runway stopway = " + stopwayHigher +
            "\nLower runway clearway = " + clearwayLower +
            "\nHigher runway clearway = " + clearwayHigher);

    }

    private void drawDesignators() {
        runwayPane.getChildren().clear();

        String lowerRunway = currentRunway.getLowerRunway().getName();
        String lowerText = lowerRunway.replaceAll("([0-9]+)([A-Z]?)", "$1\n$2").trim();
        Arrow leftArrow = new Arrow(RUNWAY_X - 60, CENTER_Y - 50, RUNWAY_X - 10, CENTER_Y - 50);
        leftArrow.setFill(Color.WHITE);
        leftArrow.setStrokeWidth(2.5);

        String higherRunway = currentRunway.getHigherRunway().getName();
        String higherText = higherRunway.replaceAll("([0-9]+)([A-Z]?)", "$1\n$2").trim();
        Arrow rightArrow = new Arrow(RUNWAY_X + RENDER_LENGTH + 60, CENTER_Y + 110,
            RUNWAY_X + RENDER_LENGTH + 10, CENTER_Y + 110);
        rightArrow.setFill(Color.WHITE);
        rightArrow.setStrokeWidth(2.5);


        //TODO: Fix designator alignment on rendering (currently too far up)
        //Label for lower runway designator
        Label lowerLabel = new Label(lowerText);
        lowerLabel.setLayoutX(RUNWAY_X + 25);
        lowerLabel.setLayoutY(CENTER_Y);
        lowerLabel.setRotate(90);
        lowerLabel.setStyle(
            "-fx-font-family: 'Helvetica'; -fx-font-size: 24px; -fx-font-weight: bold; -fx-alignment: center; -fx-text-alignment: center; -fx-text-fill: white");

        Label higherLabel = new Label(higherText);
        higherLabel.setLayoutX(RENDER_LENGTH + 25);
        higherLabel.setLayoutY(CENTER_Y);
        higherLabel.setRotate(270);
        higherLabel.setStyle(
            "-fx-font-family: 'Helvetica'; -fx-font-size: 24px; -fx-font-weight: bold; -fx-alignment: center; -fx-text-alignment: center; -fx-text-fill: white");

        runwayPane.getChildren().addAll(lowerLabel, leftArrow, rightArrow, higherLabel);
    }

    /**
     * Render the cleared and graded area around the runway strip
     *
     * @return The generated polygon
     */
    private Polygon drawClearedAndGraded() {
        double runwayCenterX = RENDER_LENGTH / 2;
        double runwayCenterY = RUNWAY_WIDTH / 2;

        // Calculate proportions based on original dimensions
        double lengthRatio = RENDER_LENGTH / 900.0;
        double widthRatio = RUNWAY_WIDTH / 100.0;

        // Define points relative to the runway center using proportions
        double[] points = {
            runwayCenterX - (560 * lengthRatio), runwayCenterY + (100 * widthRatio),
            runwayCenterX - (350 * lengthRatio), runwayCenterY + (100 * widthRatio),
            runwayCenterX - (300 * lengthRatio), runwayCenterY + (130 * widthRatio),
            runwayCenterX + (300 * lengthRatio), runwayCenterY + (130 * widthRatio),
            runwayCenterX + (350 * lengthRatio), runwayCenterY + (100 * widthRatio),
            runwayCenterX + (560 * lengthRatio), runwayCenterY + (100 * widthRatio),
            runwayCenterX + (560 * lengthRatio), runwayCenterY - (100 * widthRatio),
            runwayCenterX + (350 * lengthRatio), runwayCenterY - (100 * widthRatio),
            runwayCenterX + (300 * lengthRatio), runwayCenterY - (130 * widthRatio),
            runwayCenterX - (300 * lengthRatio), runwayCenterY - (130 * widthRatio),
            runwayCenterX - (350 * lengthRatio), runwayCenterY - (100 * widthRatio),
            runwayCenterX - (560 * lengthRatio), runwayCenterY - (100 * widthRatio)
        };
        Polygon clearedAndGraded = new Polygon(points);
        clearedAndGraded.setFill(Color.rgb(58, 30, 179));

        // Adjust position of the cleared and graded area based on the center
        clearedAndGraded.setLayoutX(CENTER_X - (RENDER_LENGTH / 2));
        clearedAndGraded.setLayoutY(CENTER_Y);

        return clearedAndGraded;
    }

    private void createCompass() {
        if (compass != null) {
            viewPort.getChildren().remove(compass);
            viewPort.getChildren().remove(resetButton);
        }

        compass = new StackPane();
        double compassSize = 60.0;
        compass.setPrefSize(compassSize, compassSize);

        Circle compassCircle = new Circle(compassSize / 2);
        compassCircle.setFill(Color.rgb(20, 20, 30, 0.7));
        compassCircle.setStroke(Color.WHITE);
        compassCircle.setStrokeWidth(2);

        Pane compassDesign = new Pane();

        // North direction
        Line northLine = new Line(compassSize/2, 5, compassSize/2, compassSize/2 - 5);
        northLine.setStroke(Color.RED);
        northLine.setStrokeWidth(2);
        Text northText = new Text("N");
        northText.setFill(Color.RED);
        northText.setFont(Font.font(10));
        northText.setX(compassSize/2 - 3);
        northText.setY(10);

        // Other cardinal directions
        Line eastLine = new Line(compassSize - 5, compassSize/2, compassSize/2 + 5, compassSize/2);
        eastLine.setStroke(Color.WHITE);
        eastLine.setStrokeWidth(1.5);
        Text eastText = new Text("E");
        eastText.setFill(Color.WHITE);
        eastText.setFont(Font.font(10));
        eastText.setX(compassSize - 10);
        eastText.setY(compassSize/2 + 4);

        Line southLine = new Line(compassSize/2, compassSize - 5, compassSize/2, compassSize/2 + 5);
        southLine.setStroke(Color.WHITE);
        southLine.setStrokeWidth(1.5);
        Text southText = new Text("S");
        southText.setFill(Color.WHITE);
        southText.setFont(Font.font(10));
        southText.setX(compassSize/2 - 3);
        southText.setY(compassSize - 5);

        Line westLine = new Line(5, compassSize/2, compassSize/2 - 5, compassSize/2);
        westLine.setStroke(Color.WHITE);
        westLine.setStrokeWidth(1.5);
        Text westText = new Text("W");
        westText.setFill(Color.WHITE);
        westText.setFont(Font.font(10));
        westText.setX(5);
        westText.setY(compassSize/2 + 4);

        compassDesign.getChildren().addAll(
            northLine, eastLine, southLine, westLine,
            northText, eastText, southText, westText
        );

        // Add components to compass
        compass.getChildren().addAll(compassCircle, compassDesign);

        // Position compass in top-right corner with some padding
        compass.setLayoutX(topDownView.getWidth() - compassSize - 20);
        compass.setLayoutY(20);

        resetButton = new Button("RESET");
        resetButton.setLayoutX(compass.getLayoutX() - 60);
        resetButton.setLayoutY(20);

        // Add hover effect
        compass.setOnMouseEntered(e -> {
            compassCircle.setFill(Color.rgb(30, 30, 40, 0.8));
            compass.setScaleX(1.1);
            compass.setScaleY(1.1);
        });

        compass.setOnMouseExited(e -> {
            compassCircle.setFill(Color.rgb(20, 20, 30, 0.7));
            compass.setScaleX(1.0);
            compass.setScaleY(1.0);
        });

        // Add click event to rotate view
        compass.setOnMouseClicked(this::handleCompassClick);

        int bearing = getBearing(currentRunway.getLowerRunway().getName());
        compass.setRotate(90 - bearing);

        // Add the compass to view
        viewPort.getChildren().addAll(compass, resetButton);
    }

    public void handleCompassClick(MouseEvent event) {
        if (currentRunway == null) {
            logger.info("Cannot rotate view - no runway selected");
            return;
        }

        // Toggle between compass active and inactive mode
        compassActive = !compassActive;

        if (compassActive) {
            // Get runway bearing from lower logical runway
            int bearing = getBearing(currentRunway.getLowerRunway().getName());

            // Calculate rotation angle needed
            double targetRotation = -90 + bearing;

            // Apply rotation to main view components
            rotateViewToRunwayBearing(targetRotation);

            // Update current rotation
            currentRotation = targetRotation;

            logger.info("View pane rotation = " + viewPane.getRotate());
            logger.info("View rotated to runway bearing: " + bearing + " degrees");
        } else {
            // Reset rotation back to default (North up)
            logger.info("View pane rotation = " + viewPane.getRotate());
            rotateViewToRunwayBearing(-currentRotation);
            currentRotation = 0.0;

            logger.info("View reset to normal orientation");
        }
    }

    /**
     * Get bearing from runway designator (e.g. "09L" = 90 degrees)
     * @param runwayName The name of the runway (e.g. "09L", "27R")
     * @return The bearing in degrees
     */
    private int getBearing(String runwayName) {
        // Extract the numeric part from the runway name
        String numericPart = runwayName.substring(0, 2);
        try {
            int runwayNumber = Integer.parseInt(numericPart);
            // Runway designators are in tens of degrees
            return runwayNumber * 10;
        } catch (NumberFormatException e) {
            logger.error("Error parsing runway number: " + e.getMessage());
            return 0;
        }
    }

    /**
     * Rotates the view to align with the runway bearing
     * @param angleToRotate Degrees to rotate
     */
    private void rotateViewToRunwayBearing(double angleToRotate) {
        logger.info("Rotating pane to " + angleToRotate);
        // Apply rotation with animation
        RotateTransition rotateTransition = new RotateTransition(Duration.millis(750));
        rotateTransition.setByAngle(angleToRotate);

        // Apply rotation to the main view pane
        rotateTransition.setNode(viewPane);
        rotateTransition.play();

        // Keep compass oriented correctly (counter-rotate)
        RotateTransition compassRotation = new RotateTransition(Duration.millis(750), compass);
        compassRotation.setByAngle(angleToRotate);
        compassRotation.play();
    }

    private void drawLines() {
        linePane.getChildren().clear();
        LogicalRunway lowerRunway = currentRunway.getLowerRunway();
        LogicalRunway higherRunway = currentRunway.getHigherRunway();

        if (obstacle != null) {
            drawObstacleLines();
        } else {
            drawLineSet(-1, lowerRunway);
            drawLineSet(1, higherRunway);
        }
    }

    private void drawLineSet(int i, LogicalRunway logicalRunway) {
        int tora = logicalRunway.getTora();
        int toda = logicalRunway.getToda();
        int lda = logicalRunway.getLda();
        int asda = logicalRunway.getAsda();

        double scaledTora = tora * scale;
        double scaledToda = toda * scale;
        double scaledLda = lda * scale;
        double scaledAsda = asda * scale;
        double threshold = (tora - lda) * scale;

        logger.info(String.format("Runway: %s"
                + "\nThreshold calculation = (%d - %d) * %f = %f", logicalRunway.getName(), tora, lda,
            scale, threshold));

        double baseY = RUNWAY_WIDTH + (i * 100);
        double spacing = i * 40; // Ensures at least 30px space between lines

        // LDA Line
        drawLineSpec("LDA", i, threshold, scaledLda, baseY, lda, ldaColor);

        // TORA Line
        drawSingleLine("TORA", i, scaledTora, baseY + spacing, tora, toraColor);

        // TODA Line
        drawSingleLine("TODA", i, scaledToda, baseY + 2 * spacing, toda, todaColor);

        // ASDA Line
        drawSingleLine("ASDA", i, scaledAsda, baseY + 3 * spacing, asda, asdaColor);
    }

    /**
     * Helper method to draw a single runway parameter line with labels and dashed threshold
     * markers
     *
     * @param label  Label for the line
     * @param i      Flag to dictate where line renders. 1 renders below runway strip, -1 renders
     *               above
     * @param length The length of the line to render
     * @param yPos   The y position of the line to render
     * @param value  The distance value to label the line
     * @param color  The colour to render the line
     */
    private void drawSingleLine(String label, int i, double length, double yPos, int value,
        Color color) {
        double startX;
        double endX;

        if (i == -1) {
            startX = 0;
            endX = length;
        } else {
            startX = RENDER_LENGTH;
            endX = RENDER_LENGTH - length;
        }

        // Main line
        Line line = new Line(startX, yPos, endX, yPos);
        line.setStroke(color);
        line.setStrokeWidth(2);

        // Label
        Label lineLabel = new Label(String.format("%s: %d", label, value));
        lineLabel.setStyle("-fx-font-size: 16");
        lineLabel.setTextFill(color); // Set label color to match the line
        lineLabel.setLayoutX((endX));
        lineLabel.setLayoutY(yPos - 20); // Place label slightly below the line

        // Dashed threshold markers
        Line thresholdStart = new Line(startX, 0d, startX, yPos);
        thresholdStart.getStrokeDashArray().addAll(10.0, 5.0);
        thresholdStart.setStroke(color);

        Line thresholdEnd = new Line(endX, 0d, endX, yPos);
        thresholdEnd.getStrokeDashArray().addAll(10.0, 5.0);
        thresholdEnd.setStroke(color);

        // Add all elements to the pane
        linePane.getChildren().addAll(line, thresholdStart, thresholdEnd, lineLabel);
    }

    private void drawLineSpec(String label, int i, double threshPos, double endPos, double yPos,
        int value, Color color) {
        double startX;
        double endX;

        if (i == -1) {
            startX = threshPos;
            endX = endPos + threshPos;
        } else {
            startX = RENDER_LENGTH - threshPos;
            endX = RENDER_LENGTH - endPos - threshPos;
        }
        logger.info(String.format("Threshold: %f \nEndX: %f", startX, endX));

        // Main line
        Line line = new Line(startX, yPos, endX, yPos);
        line.setStroke(color);
        line.setStrokeWidth(2);

        // Label
        Label lineLabel = new Label(String.format("%s: %d", label, value));
        lineLabel.setStyle("-fx-font-size: 16");
        lineLabel.setTextFill(color); // Set label color to match the line
        lineLabel.setLayoutX((endX));
        lineLabel.setLayoutY(yPos - 20); // Place label slightly below the line

        // Dashed threshold markers
        Line thresholdStart = new Line(startX, 0d, startX, yPos);
        thresholdStart.getStrokeDashArray().addAll(10.0, 5.0);
        thresholdStart.setStroke(color);

        Line thresholdEnd = new Line(endX, 0d, endX, yPos);
        thresholdEnd.getStrokeDashArray().addAll(10.0, 5.0);
        thresholdEnd.setStroke(color);

        // Add all elements to the pane
        linePane.getChildren().addAll(line, thresholdStart, thresholdEnd, lineLabel);
    }

    private void drawExactLine(String label, int i, double startX, double endX, double yPos,
        int value, Color color) {
        // Main line
        Line line = new Line(startX, yPos, endX, yPos);
        line.setStroke(color);
        line.setStrokeWidth(2);

        // Label
        Label lineLabel = new Label(String.format("%s: %d", label, value));
        lineLabel.setStyle("-fx-font-size: 16");
        lineLabel.setTextFill(color); // Set label color to match the line
        lineLabel.setLayoutX((endX));
        lineLabel.setLayoutY(yPos - 20); // Place label slightly below the line

        // Dashed threshold markers
        Line thresholdStart = new Line(startX, 0d, startX, yPos);
        thresholdStart.getStrokeDashArray().addAll(10.0, 5.0);
        thresholdStart.setStroke(color);

        Line thresholdEnd = new Line(endX, 0d, endX, yPos);
        thresholdEnd.getStrokeDashArray().addAll(10.0, 5.0);
        thresholdEnd.setStroke(color);

        // Add all elements to the pane
        linePane.getChildren().addAll(line, thresholdStart, thresholdEnd, lineLabel);
        logger.info("Drawing " + label + " line starting at: " + startX + "\nto: " + endX);
    }

    public void addObstacle(Obstacle obstacle) {
        logger.info("Adding obstacle to side view");

        this.obstacle = obstacle;

        renderObstacle();
        drawObstacleLines();
    }

    private void renderObstacle() {
        logger.info("Rendering obstacle");
        obstaclePane.getChildren().clear();

        LogicalRunway logicalRunway = currentRunway.getLowerRunway();
        obstacleShape = new Rectangle(15, 15, Color.RED);
        int displacedThreshold = logicalRunway.getDispThreshold();

        double x = (obstacle.getDistLowerThreshold() + displacedThreshold) * scale - 7.5;
        double y = -1 * ((obstacle.getCentreOffset() * WIDTH_SCALE) - 0.5 * (RUNWAY_WIDTH - 8));

        obstacleShape.setX(x);
        obstacleShape.setY(y);

        logger.info(String.format("Obstacle being placed at x = %.2f \n "
            + "y = %.2f \n "
            + "Scale = %.2f \n "
            + "Displaced Threshold = %d", x, CENTER_Y, scale, displacedThreshold));
        obstaclePane.getChildren().add(obstacleShape);
    }

    private void drawObstacleLines() {
        linePane.getChildren().clear();

        LogicalRunway lowerRunway = currentRunway.getLowerRunway();
        LogicalRunway higherRunway = currentRunway.getHigherRunway();

        if (obstacle.getIsCloserLower()) {
            //If the obstacle is closer to threshold with lower bearing
            //Integer is a flag to denote line root
            takeoffLinesAway(1, higherRunway);
            ldaOver(1, higherRunway);

            takeoffLinesTowards(-1, lowerRunway);
            ldaTowards(-1, lowerRunway);

            drawResaHigher();

        } else {
            takeoffLinesAway(-1, lowerRunway);
            ldaOver(-1, lowerRunway);

            takeoffLinesTowards(1, higherRunway);
            ldaTowards(1, higherRunway);

            drawResaLower();
        }
    }

    private void drawResaLower() {
        logger.info("Drawing RESA rectangle");
        Rectangle resa = new Rectangle((240 * scale), RUNWAY_WIDTH);
        resa.setX(obstacleShape.getX() + 15);
        resa.setY(0);
        resa.setFill(Color.RED);
        resa.setOpacity(0.4);

        obstaclePane.getChildren().addLast(resa);
    }

    private void drawResaHigher() {
        logger.info("Drawing RESA rectangle");
        Rectangle resa = new Rectangle((240 * scale), RUNWAY_WIDTH);
        resa.setX(obstacleShape.getX() - resa.getWidth());
        resa.setY(0);
        resa.setFill(Color.RED);
        resa.setOpacity(0.4);

        obstaclePane.getChildren().addLast(resa);
    }

    private void ldaTowards(int i, LogicalRunway logicalRunway) {
        int tora = logicalRunway.getTora();
        int lda = logicalRunway.getLda();
        int currLda = logicalRunway.getCurrLda();

        double scaledLda = currLda * scale;
        double threshold = (tora - lda) * scale;

        double startX;
        double endX;

        if (i == -1) {
            startX = threshold;
            endX = scaledLda + threshold;
        } else {
            startX = RENDER_LENGTH - threshold;
            endX = RENDER_LENGTH - (scaledLda + threshold);
        }
        logger.info(String.format("Threshold: %f \nEndX: %f", startX, endX));

        double yPos = RUNWAY_WIDTH + (i * 100);

        // Main line
        Line line = new Line(startX, yPos, endX, yPos);
        line.setStroke(ldaColor);
        line.setStrokeWidth(2);

        // Label
        Label lineLabel = new Label(String.format("LDA: %d m", currLda));
        lineLabel.setStyle("-fx-font-size: 16");
        lineLabel.setTextFill(ldaColor); // Set label color to match the line
        lineLabel.setLayoutX((endX));
        lineLabel.setLayoutY(yPos - 20); // Place label slightly below the line

        // Dashed threshold markers
        Line thresholdStart = new Line(startX, 0d, startX, yPos);
        thresholdStart.getStrokeDashArray().addAll(10.0, 5.0);
        thresholdStart.setStroke(ldaColor);

        Line thresholdEnd = new Line(endX, 0d, endX, yPos);
        thresholdEnd.getStrokeDashArray().addAll(10.0, 5.0);
        thresholdEnd.setStroke(ldaColor);

        // Add all elements to the pane
        linePane.getChildren().addAll(line, thresholdStart, thresholdEnd, lineLabel);
    }

    private void takeoffLinesTowards(int i, LogicalRunway logicalRunway) {
        int tora = logicalRunway.getCurrTora();
        int toda = logicalRunway.getCurrToda();
        int asda = logicalRunway.getCurrAsda();

        double scaledTora = tora * scale;
        double scaledToda = toda * scale;
        double scaledAsda = asda * scale;

        double baseY = RUNWAY_WIDTH + (i * 100);
        double spacing = i * 40; // Ensures at least 30px space between lines

        // TORA Line
        drawSingleLine("TORA", i, scaledTora, baseY + spacing, tora, toraColor);

        // TODA Line
        drawSingleLine("TODA", i, scaledToda, baseY + 2 * spacing, tora, todaColor);

        // ASDA Line
        drawSingleLine("ASDA", i, scaledAsda, baseY + 3 * spacing, tora, asdaColor);
    }

    private void takeoffLinesAway(int i, LogicalRunway logicalRunway) {
        int tora = logicalRunway.getCurrTora();
        int toda = logicalRunway.getCurrToda();
        int asda = logicalRunway.getCurrAsda();

        double scaledTora = tora * scale;
        double scaledToda = toda * scale;
        double scaledAsda = asda * scale;

        double baseY = RUNWAY_WIDTH + (i * 100);
        double spacing = i * 40; // Ensures at least 30px space between lines

        if (i == -1) {
            logger.info("USING -1 FLAG \n Lower runway");

            drawExactLine("TORA",
                i,
                RENDER_LENGTH - scaledTora,
                RENDER_LENGTH,
                baseY + spacing,
                tora,
                toraColor);

            drawExactLine("TODA",
                i,
                RENDER_LENGTH - scaledToda,
                lowerTodaStart,
                baseY + 2 * spacing,
                toda,
                todaColor);

            drawExactLine("ASDA",
                i,
                RENDER_LENGTH - scaledAsda,
                RENDER_LENGTH,
                baseY + 3 * spacing,
                asda,
                asdaColor);
        } else {
            logger.info("USING +1 FLAG");

            drawExactLine("TORA",
                i,
                scaledTora,
                0,
                baseY + spacing,
                tora,
                toraColor);

            drawExactLine("TODA",
                i,
                scaledToda,
                upperTodaStart,
                baseY + 2 * spacing,
                toda,
                todaColor);

            drawExactLine("ASDA",
                i,
                scaledAsda,
                0,
                baseY + 3 * spacing,
                asda,
                asdaColor);
        }

    }

    private void ldaOver(int i, LogicalRunway logicalRunway) {
        int lda = logicalRunway.getCurrLda();
        double scaledLda = lda * scale;

        double startX;
        double endX;

        if (i == -1) {
            logger.info("USING -1 FLAG");
            startX = RENDER_LENGTH - scaledLda;
            endX = RENDER_LENGTH;
        } else {
            logger.info("USING +1 FLAG");
            startX = scaledLda;
            endX = 0;
        }

        double baseY = RUNWAY_WIDTH + (i * 100);

        drawExactLine("LDA", i, startX, endX, baseY, lda, ldaColor);

    }

}
