package uk.ac.soton.group2seg.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
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

    @FXML public AnchorPane topDownView;

    private final double RUNWAY_LENGTH = 650;
    private final double RUNWAY_WIDTH = 66.0;

    private Color toraColor = Color.RED;
    private Color todaColor = Color.YELLOW;
    private Color ldaColor = Color.WHITE;
    private Color asdaColor = Color.CYAN;

    private Pane linePane;
    private Pane runwayPane;
    private ModelState modelState;
    private Runway currentRunway = null;
    private double scale;
    private boolean initialSetupComplete = false;

    public TopDownController() {
       runwayPane = new Pane();
       linePane = new Pane();
       instance = this;
    }

    public static TopDownController getInstance() {
        return instance;
    }

    public void initialize() {
        logger.info("Initialising TopDownController");
        AnchorPane.setTopAnchor(topDownView, 0d);
        AnchorPane.setBottomAnchor(topDownView, 0d);
        AnchorPane.setLeftAnchor(topDownView, 0d);
        AnchorPane.setRightAnchor(topDownView, 0d);

        topDownView.setStyle("-fx-background-color: rgb(7, 51, 19)");
        topDownView.getChildren().addAll(runwayPane, linePane);

        // Setup listeners for resize events
        topDownView.widthProperty().addListener((obs, oldVal, newVal) -> {
            updateView();
        });
        topDownView.heightProperty().addListener((obs, oldVal, newVal) -> {
            updateView();
        });

        // Initial positioning
        updateView();

        initialSetupComplete = true;
    }

    public void setModelState(ModelState model) {
        modelState = model;
    }

    public void updateRunway() {
        this.currentRunway = modelState.getCurrentRunway();
        updateView();
    }

    private void updateView() {
        linePane.getChildren().clear();
        runwayPane.getChildren().clear();

        if (!initialSetupComplete && (topDownView.getWidth() <= 0 || topDownView.getHeight() <= 0)) {
            return; // Skip if dimensions aren't positive during initialization
        }

        drawRunway();
        // Center runway in the AnchorPane
        double centerX = Math.max(0, (topDownView.getWidth() - RUNWAY_LENGTH) / 2);
        double centerY = Math.max(0, (topDownView.getHeight() - RUNWAY_WIDTH) / 2);


        runwayPane.getChildren().addAll(linePane);

        runwayPane.setLayoutX(centerX);
        runwayPane.setLayoutY(centerY);

//        linePane.setLayoutX(centerX);
//        linePane.setLayoutY(centerY);

        drawClearedAndGraded();


    }

    /**
     * Render the runway strip
     * */
    private void drawRunway() {
        Rectangle strip = new Rectangle(RUNWAY_LENGTH, RUNWAY_WIDTH);
        strip.setFill(Color.DARKGREY);
        strip.setStroke(Color.WHITE);
        strip.setStrokeWidth(2);

        Line centreLine = new Line(75, RUNWAY_WIDTH/2, RUNWAY_LENGTH - 75, RUNWAY_WIDTH/2);
        centreLine.getStrokeDashArray().addAll(15.0, 10.0);
        centreLine.setStroke(Color.WHITE);
        centreLine.setStrokeWidth(5);

        runwayPane.getChildren().addAll(strip, centreLine);

        try{
            scale = RUNWAY_LENGTH/ currentRunway.getRunwayLength();
            drawDesignators();
            drawLines();
        }catch (Exception e) {
            logger.info("No runway selected yet");
        }

        //TODO: drawThresholds();
    }

    private void drawDesignators() {
        String lowerRunway = currentRunway.getLowerRunway().getName();
        String lowerText = lowerRunway.replaceAll("([0-9]+)([A-Z]?)", "$1\n$2").trim();
        Arrow leftArrow = new Arrow(-60,-50,-10,-50);
        leftArrow.setFill(Color.WHITE);
        leftArrow.setStrokeWidth(2.5);

        String higherRunway = currentRunway.getHigherRunway().getName();
        String higherText = higherRunway.replaceAll("([0-9]+)([A-Z]?)", "$1\n$2").trim();
        Arrow rightArrow = new Arrow(RUNWAY_LENGTH + 60,110,RUNWAY_LENGTH + 10,110);
        rightArrow.setFill(Color.WHITE);
        rightArrow.setStrokeWidth(2.5);

        //Label for lower runway designator
        Label lowerLabel = new Label(lowerText);
        lowerLabel.setLayoutX(25);
        lowerLabel.setLayoutY(0);
        lowerLabel.setRotate(90);
        lowerLabel.setStyle("-fx-font-family: 'Helvetica'; -fx-font-size: 24px; -fx-font-weight: bold; -fx-alignment: center; -fx-text-alignment: center; -fx-text-fill: white");

        Label higherLabel = new Label(higherText);
        higherLabel.setLayoutX(RUNWAY_LENGTH-50);
        higherLabel.setLayoutY(0);
        higherLabel.setRotate(270);
        higherLabel.setStyle("-fx-font-family: 'Helvetica'; -fx-font-size: 24px; -fx-font-weight: bold; -fx-alignment: center; -fx-text-alignment: center; -fx-text-fill: white");

        runwayPane.getChildren().addAll(lowerLabel, leftArrow, rightArrow, higherLabel);
    }

    /**
     * Render the cleared and graded area around the runway strip
     * */
    private void drawClearedAndGraded() {
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

        logger.info(String.format("Centered on (%f, %f)", runwayCenterX, runwayCenterY) + "\nPoints are: " + clearedAndGraded.getPoints().toString());

        runwayPane.getChildren().add(0, clearedAndGraded);
    }

    private void drawLines() {
        linePane.getChildren().clear();
        LogicalRunway lowerRunway = modelState.getCurrentRunway().getLowerRunway();
        LogicalRunway higherRunway = modelState.getCurrentRunway().getHigherRunway();

        if(modelState.getObstacle() != null) {
            addObstacle(modelState.getObstacle());
        }else{
            drawLineSet(-1, lowerRunway);
            drawLineSet(1, higherRunway);
        }
    }


    /**
     * Draw lines using officially declared runway distances
     * @param i Flag denoting which logical runway (-1 = lower bearing runway i.e. 04, 09L etc.)
     * @param logicalRunway The logical runway
     */
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
            + "\nThreshold calculation = (%d - %d) * %f = %f",logicalRunway.getName(), tora, lda, scale, threshold));

        double baseY = RUNWAY_WIDTH + (i * 200);
        double spacing = i * 40; // Ensures at least 30px space between lines

        // LDA Line
        drawLineSpec("LDA", i, threshold, scaledLda, baseY, lda, ldaColor);

        // TORA Line
        drawSingleLine("TORA", i, scaledTora, baseY + spacing, tora, toraColor);

        // TODA Line
        drawSingleLine("TODA", i, scaledToda, baseY + 2* spacing, toda, todaColor);

        // ASDA Line
        drawSingleLine("ASDA", i, scaledAsda, baseY + 3 * spacing, asda, asdaColor);
    }

    public void addObstacle(Obstacle obstacle){
        linePane.getChildren().clear();
        LogicalRunway lowerRunway = modelState.getCurrentRunway().getLowerRunway();
        LogicalRunway higherRunway = modelState.getCurrentRunway().getHigherRunway();

        //Pass lower runway to method to render obstacle
        renderObstacle(obstacle, lowerRunway);

        if(obstacle.getIsCloserLower()) {
            //If the obstacle is closer to threshold with lower bearing
            //Integer is a flag to denote line root
            takeoffLinesAway(1, higherRunway);
            ldaOver(1, higherRunway);

            takeoffLinesTowards(-1, lowerRunway);
            ldaTowards(-1, lowerRunway);

        }else{
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
        double threshold = (tora - lda) *scale;

        double startX;
        double endX;

        if(i == -1) {
            startX = threshold;
            endX = scaledLda + threshold;
        }else {
            startX = RUNWAY_LENGTH - threshold;
            endX = RUNWAY_LENGTH - scaledLda - threshold;
        }
        logger.info(String.format("Threshold: %f \nEndX: %f", startX, endX));

        double yPos = RUNWAY_WIDTH + (i * 200);

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

        double baseY = RUNWAY_WIDTH + (i * 200);
        double spacing = i * 40; // Ensures at least 30px space between lines

        // TORA Line
        drawSingleLine("TORA", i, scaledTora, baseY + spacing, tora, toraColor);

        // TODA Line
        drawSingleLine("TODA", i, scaledTora, baseY + 2* spacing, tora, todaColor);

        // ASDA Line
        drawSingleLine("ASDA", i, scaledTora, baseY + 3 * spacing, tora, asdaColor);
    }

    private void ldaOver(int i, LogicalRunway logicalRunway) {
        int lda = logicalRunway.getCurrLda();
        double scaledLda = lda * scale;

        double startX;
        double endX;

        if(i == -1) {
            logger.info("USING -1 FLAG");
            startX = RUNWAY_LENGTH - scaledLda;
            endX = RUNWAY_LENGTH;
        }else {
            logger.info("USING +1 FLAG");
            startX = scaledLda;
            endX = 0;
        }

        double baseY = RUNWAY_WIDTH + (i * 200);

        drawExactLine("LDA", i, startX, endX, baseY, lda, ldaColor);

    }

    //TODO: Use separate TORA, TODA & ASDA values :(.
    private void takeoffLinesAway(int i, LogicalRunway logicalRunway) {
        int tora = logicalRunway.getCurrTora();
        int toda = logicalRunway.getCurrToda();
        int asda = logicalRunway.getCurrAsda();

        double scaledTora = tora * scale;
        double scaledToda = toda * scale;
        double scaledAsda = asda * scale;

        double startX;
        double endX;
        if(i == -1) {
            logger.info("USING -1 FLAG");
            startX = RUNWAY_LENGTH - scaledTora;
            endX = RUNWAY_LENGTH;
        }else {
            logger.info("USING +1 FLAG");
            startX = scaledTora;
            endX = 0;
        }

        logger.info(String.format("Runway: %s \n"
            + "TORA: %d \n"
            + "StartX: %f \n EndX: %f", logicalRunway.getName(), tora, startX, endX));


        double baseY = RUNWAY_WIDTH + (i * 200);
        double spacing = i * 40; // Ensures at least 30px space between lines

        // TORA Line
        drawExactLine("TORA", i, startX, endX, baseY + spacing, tora,toraColor);

        // TORA Line
        drawExactLine("TODA", i, startX, endX, baseY + 2 * spacing, tora,todaColor);

        // TORA Line
        drawExactLine("ASDA", i, startX, endX, baseY + 3 * spacing, tora,asdaColor);

    }

    private void renderObstacle(Obstacle obstacle, LogicalRunway logicalRunway) {
        Rectangle obstacleShape = new Rectangle(15.0, 15.0, Color.RED);
        double verticalScale = RUNWAY_WIDTH / 30;
        int displacedThreshold = logicalRunway.getDispThreshold();

        double x = (obstacle.getDistLowerThreshold() + displacedThreshold) * scale;
        double y = (RUNWAY_WIDTH / 2) - 7.5 + (obstacle.getCentreOffset() * verticalScale);

        obstacleShape.setX(x);
        obstacleShape.setY(y);

        linePane.getChildren().add(obstacleShape);
    }

    /**
     * Helper method to draw a single runway parameter line with labels and dashed threshold markers.
     */
    private void drawSingleLine(String label, int i, double length, double yPos, int value, Color color) {
        double startX;
        double endX;

        if(i == -1) {
            startX = 0;
            endX = length;
        }else{
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

    private void drawLineSpec(String label, int i, double threshPos, double endPos, double yPos, int value, Color color) {
        double startX;
        double endX;

        if(i == -1) {
            startX = threshPos;
            endX = endPos + threshPos ;
        } else{
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

    private void drawExactLine(String label, int i, double startX, double endX, double yPos, int value, Color color){
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

}
