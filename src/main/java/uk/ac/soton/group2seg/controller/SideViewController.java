package uk.ac.soton.group2seg.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.ac.soton.group2seg.model.LogicalRunway;
import uk.ac.soton.group2seg.model.ModelState;
import uk.ac.soton.group2seg.model.Obstacle;
import uk.ac.soton.group2seg.model.Runway;

public class SideViewController {
    private static SideViewController instance = new SideViewController();
    private final Logger logger = LogManager.getLogger(this.getClass());

    @FXML public AnchorPane sideView;

    private final double RUNWAY_LENGTH = 650;
    private final double RUNWAY_WIDTH = 66.0;

    private final Color toraColor = Color.RED;
    private final Color todaColor = Color.YELLOW;
    private final Color ldaColor = Color.WHITE;
    private final Color asdaColor = Color.CYAN;

    private Pane viewPane;
    private Pane obstaclePane;
    private Pane linePane;
    private ModelState modelState;
    private Runway currentRunway;
    private Obstacle obstacle;
    private double scale;

    private boolean initialSetupComplete = false;

    public SideViewController() {
        viewPane = new Pane();
        obstaclePane = new Pane();
        linePane = new Pane();
        instance = this;
    }

    public static SideViewController getInstance() {
        return instance;
    }

    public void setModelState(ModelState modelState) {
        this.modelState = modelState;
    }

    public void initialize(){
        logger.info("Initialising side view");
        AnchorPane.setTopAnchor(sideView, 0d);
        AnchorPane.setBottomAnchor(sideView, 0d);
        AnchorPane.setLeftAnchor(sideView, 0d);
        AnchorPane.setRightAnchor(sideView, 0d);

        sideView.setStyle("-fx-background-color: skyblue");

        sideView.getChildren().addAll(viewPane);

        viewPane.setLayoutX(159.5);
        viewPane.setLayoutY(470.75);

        sideView.widthProperty().addListener((obs, oldVal, newVal) -> {
            drawView();
        });
        sideView.heightProperty().addListener((obs, oldVal, newVal) -> {
            drawView();
        });

        drawView();

        initialSetupComplete = true;
    }

    private void drawView() {
        viewPane.getChildren().clear();

        if(!initialSetupComplete && (sideView.getWidth() <=0 || sideView.getHeight() <= 0)) {
            return;
        }

        logger.info("Drawing runway strip");

        Rectangle runway = new Rectangle(650, 15);
        runway.setFill(Color.DARKGREY);

        Rectangle ground = new Rectangle(1000, 600);
        ground.setY(0);
        ground.setX(-185);
        ground.setFill(Color.DARKGREEN);

        viewPane.getChildren().addAll(ground, runway, linePane, obstaclePane);

        try {
            scale = RUNWAY_LENGTH / currentRunway.getRunwayLength();
            drawLines();
        }catch (Exception e) {
            logger.info("No runway selected yet");
        }
    }

    public void updateRunway(){
        this.currentRunway = modelState.getCurrentRunway();
        drawView();
    }

    private void drawLines() {
        linePane.getChildren().clear();
        LogicalRunway lowerRunway = currentRunway.getLowerRunway();
        LogicalRunway higherRunway = currentRunway.getHigherRunway();

        if (obstacle != null) {
            drawObstacleLines();
        }else {
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
            + "\nThreshold calculation = (%d - %d) * %f = %f",logicalRunway.getName(), tora, lda, scale, threshold));

        double baseY = RUNWAY_WIDTH + (i * 100);
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
        Rectangle obstacleShape = new Rectangle(15, obstacle.getHeight(), Color.RED);
        int displacedThreshold = logicalRunway.getDispThreshold();

        double x = (obstacle.getDistLowerThreshold() + displacedThreshold) * scale;
        double y = -1 * obstacle.getHeight();

        obstacleShape.setX(x);
        obstacleShape.setY(y);

        logger.info(String.format("Obstacle being placed at x = %.2f \n "
            + "y = %.2f \n "
            + "Scale = %.2f \n "
            + "Displaced Threshold = %d", x, y, scale, displacedThreshold));
        obstaclePane.getChildren().add(obstacleShape);
    }

    private void drawObstacleLines() {
        linePane.getChildren().clear();

        LogicalRunway lowerRunway = currentRunway.getLowerRunway();
        LogicalRunway higherRunway = currentRunway.getHigherRunway();

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
        drawExactLine("TORA", i, startX, endX, baseY + spacing, tora, toraColor);

        // TORA Line
        drawExactLine("TODA", i, startX, endX, baseY + 2 * spacing, toda, todaColor);

        // TORA Line
        drawExactLine("ASDA", i, startX, endX, baseY + 3 * spacing, asda, asdaColor);

    }
}
