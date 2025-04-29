package uk.ac.soton.group2seg.controller;

import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Line;
import javafx.scene.shape.Polygon;
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

    private final double RUNWAY_LENGTH = 650;
    private final double RUNWAY_WIDTH = 66.0;
    private final double WIDTH_SCALE = 66/40;
    private double CENTER_X;
    private double CENTER_Y;
    private double RUNWAY_X;

    private final Color asdaColor = Color.YELLOW;
    private final Color todaColor = Color.ORANGE;
    private final Color toraColor = Color.RED;
    private final Color ldaColor = Color.WHITE;

    private Pane viewPane;
    private Pane obstaclePane;
    private Pane linePane;
    private Pane runwayPane;
    private Rectangle obstacleShape;
    private Obstacle obstacle;
    private ModelState modelState;
    private Runway currentRunway = null;
    private double scale;
    private boolean initialSetupComplete = false;

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

        viewPane = new Pane();
        runwayPane = new Pane();
        linePane = new Pane();
        obstaclePane = new Pane();

        topDownView.setMaxSize(1000, 1000);
        topDownView.setStyle("-fx-background-color: rgb(7, 51, 19)");
        topDownView.getChildren().addAll(viewPane);
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

        // Initial positioning
        drawView();

        initialSetupComplete = true;
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

        if (!initialSetupComplete && (topDownView.getWidth() <= 0
            || topDownView.getHeight() <= 0)) {
            return; // Skip if dimensions aren't positive during initialization
        }

        CENTER_X = topDownView.getWidth() / 2;
        CENTER_Y = topDownView.getHeight() / 2;

        logger.info(String.format("Center X = %.2f \n"
            + "Center Y = %.2f", CENTER_X, CENTER_Y));

        logger.info("Drawing runway strip");

        //drawRunway();
        drawClearedAndGraded();

        RUNWAY_X = CENTER_X - (RUNWAY_LENGTH / 2);
        Rectangle strip = new Rectangle(RUNWAY_X, CENTER_Y, RUNWAY_LENGTH, RUNWAY_WIDTH);
        strip.setFill(Color.DARKGREY);
        strip.setStroke(Color.WHITE);
        strip.setStrokeWidth(2);

        Line centreLine = new Line(
            RUNWAY_X + 75, CENTER_Y + RUNWAY_WIDTH / 2, RUNWAY_LENGTH, CENTER_Y + RUNWAY_WIDTH / 2);
        centreLine.getStrokeDashArray().addAll(15.0, 10.0);
        centreLine.setStroke(Color.WHITE);
        centreLine.setStrokeWidth(5);

        linePane.setLayoutX(CENTER_X - (RUNWAY_LENGTH / 2));
        linePane.setLayoutY(CENTER_Y);

        obstaclePane.setLayoutX(CENTER_X - (RUNWAY_LENGTH / 2));
        obstaclePane.setLayoutY(CENTER_Y);

        viewPane.getChildren()
            .addAll(drawClearedAndGraded(), strip, centreLine, runwayPane, linePane, obstaclePane);

        try {
            scale = RUNWAY_LENGTH / currentRunway.getRunwayLength();
            drawDesignators();
            drawLines();
        } catch (Exception e) {
            logger.info("No runway selected yet");
        }
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
        Arrow rightArrow = new Arrow(RUNWAY_X + RUNWAY_LENGTH + 60, CENTER_Y + 110,
            RUNWAY_X + RUNWAY_LENGTH + 10, CENTER_Y + 110);
        rightArrow.setFill(Color.WHITE);
        rightArrow.setStrokeWidth(2.5);

        //Label for lower runway designator
        Label lowerLabel = new Label(lowerText);
        lowerLabel.setLayoutX(RUNWAY_X + 25);
        lowerLabel.setLayoutY(CENTER_Y);
        lowerLabel.setRotate(90);
        lowerLabel.setStyle(
            "-fx-font-family: 'Helvetica'; -fx-font-size: 24px; -fx-font-weight: bold; -fx-alignment: center; -fx-text-alignment: center; -fx-text-fill: white");

        Label higherLabel = new Label(higherText);
        higherLabel.setLayoutX(RUNWAY_LENGTH + 25);
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
        double runwayCenterX = RUNWAY_LENGTH / 2;
        double runwayCenterY = RUNWAY_WIDTH / 2;

        // Calculate proportions based on original dimensions
        double lengthRatio = RUNWAY_LENGTH / 900.0;
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
        clearedAndGraded.setLayoutX(CENTER_X - (RUNWAY_LENGTH / 2));
        clearedAndGraded.setLayoutY(CENTER_Y);

        return clearedAndGraded;
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
            startX = RUNWAY_LENGTH;
            endX = RUNWAY_LENGTH - length;
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
            startX = RUNWAY_LENGTH - threshPos;
            endX = RUNWAY_LENGTH - endPos - threshPos;
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

        double x = (obstacle.getDistLowerThreshold() + displacedThreshold) * scale;
        double y = -1 * ((obstacle.getCentreOffset() * WIDTH_SCALE) - 0.5 * (RUNWAY_WIDTH - 7.5));

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

        } else {
            takeoffLinesAway(-1, lowerRunway);
            ldaOver(-1, lowerRunway);

            takeoffLinesTowards(1, higherRunway);
            ldaTowards(1, higherRunway);
        }
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
            startX = RUNWAY_LENGTH - threshold;
            endX = RUNWAY_LENGTH - scaledLda - threshold;
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
        double scaledTora = tora * scale;

        double baseY = RUNWAY_WIDTH + (i * 100);
        double spacing = i * 40; // Ensures at least 30px space between lines

        // TORA Line
        drawSingleLine("TORA", i, scaledTora, baseY + spacing, tora, toraColor);

        // TODA Line
        drawSingleLine("TODA", i, scaledTora, baseY + 2 * spacing, tora, todaColor);

        // ASDA Line
        drawSingleLine("ASDA", i, scaledTora, baseY + 3 * spacing, tora, asdaColor);
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
            logger.info("USING -1 FLAG");

            drawExactLine("TORA",
                i,
                RUNWAY_LENGTH - scaledTora,
                RUNWAY_LENGTH,
                baseY + spacing,
                tora,
                toraColor);

            // TORA Line
            drawExactLine("TODA",
                i,
                RUNWAY_LENGTH - scaledToda,
                RUNWAY_LENGTH,
                baseY + 2 * spacing,
                toda,
                todaColor);

            // TORA Line
            drawExactLine("ASDA",
                i,
                RUNWAY_LENGTH - scaledAsda,
                RUNWAY_LENGTH,
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

            // TORA Line
            drawExactLine("TODA",
                i,
                scaledToda,
                0,
                baseY + 2 * spacing,
                toda,
                todaColor);

            // TORA Line
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
            startX = RUNWAY_LENGTH - scaledLda;
            endX = RUNWAY_LENGTH;
        } else {
            logger.info("USING +1 FLAG");
            startX = scaledLda;
            endX = 0;
        }

        double baseY = RUNWAY_WIDTH + (i * 100);

        drawExactLine("LDA", i, startX, endX, baseY, lda, ldaColor);

    }

}
