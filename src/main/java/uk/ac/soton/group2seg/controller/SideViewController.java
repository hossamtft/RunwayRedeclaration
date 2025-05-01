package uk.ac.soton.group2seg.controller;

import javafx.animation.ScaleTransition;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.ac.soton.group2seg.model.LogicalRunway;
import uk.ac.soton.group2seg.model.ModelState;
import uk.ac.soton.group2seg.model.Obstacle;
import uk.ac.soton.group2seg.model.Runway;
import uk.ac.soton.group2seg.view.Arrow;

public class SideViewController {
    private static SideViewController instance = new SideViewController();
    private final Logger logger = LogManager.getLogger(this.getClass());

    @FXML public StackPane sideView;

    private final double RENDER_LENGTH = 650;
    private final double RUNWAY_WIDTH = 15.0;
    private double CENTER_X;
    private double CENTER_Y;

    private double lowerTodaStart;
    private double upperTodaStart;

    private final Color asdaColor = Color.YELLOW;
    private final Color todaColor = Color.ORANGE;
    private final Color toraColor = Color.RED;
    private final Color ldaColor = Color.WHITE;

    private double currentZoom = 1.0;
    private final double MIN_ZOOM = 0.2;
    private final double MAX_ZOOM = 2.0;
    private final double ZOOM_DELTA = 0.1;
    private Pane zoomControls;

    private Pane viewPort;
    private Pane viewPane;
    private Pane runwayPane;
    private Pane obstaclePane;
    private Pane linePane;
    private Rectangle obstacleShape;
    private ModelState modelState;
    private Runway currentRunway;
    private Obstacle obstacle;
    private double scale;

    private boolean initialSetupComplete = false;

    public Node getSideDownViewNode()
    {
        return sideView;
    }

    public SideViewController() {
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

        viewPort = new Pane();
        runwayPane = new Pane();
        viewPane = new Pane();
        obstaclePane = new Pane();
        linePane = new Pane();

        sideView.setMaxSize(1000, 1000);

        sideView.setStyle("-fx-background-color: skyblue");
        sideView.setOpacity(100);

        sideView.getChildren().addAll(viewPort);

        sideView.setAlignment(Pos.CENTER);


        sideView.widthProperty().addListener((obs, oldVal, newVal) -> {
            logger.info("Width resize event");
            drawView();
        });
        sideView.heightProperty().addListener((obs, oldVal, newVal) -> {
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

    private void drawView() {
        viewPane.getChildren().clear();

        if(!initialSetupComplete && (sideView.getWidth() <=0 || sideView.getHeight() <= 0)) {
            return;
        }

        CENTER_X = sideView.getWidth() / 2;
        CENTER_Y = sideView.getHeight() / 2;

        logger.info(String.format("Center X = %.2f \n"
            + "Center Y = %.2f", CENTER_X, CENTER_Y));

        logger.info("Drawing runway strip");

        double runwayX = CENTER_X - (RENDER_LENGTH / 2);
        Rectangle runway = new Rectangle(runwayX, CENTER_Y, RENDER_LENGTH, 15);
        runway.setFill(Color.DARKGREY);

        // Arrows (adjusted for new runway position)
        Arrow leftArrow = new Arrow(runwayX - 60, CENTER_Y - 50, runwayX - 10, CENTER_Y - 50);
        leftArrow.setFill(Color.WHITE);
        leftArrow.setStrokeWidth(2.5);

        Arrow rightArrow = new Arrow(runwayX + RENDER_LENGTH
            + 60, CENTER_Y + 110, runwayX + RENDER_LENGTH + 10, CENTER_Y + 110);
        rightArrow.setFill(Color.WHITE);
        rightArrow.setStrokeWidth(2.5);

        linePane.setLayoutX(CENTER_X - (RENDER_LENGTH / 2));
        linePane.setLayoutY(CENTER_Y);

        obstaclePane.setLayoutX(CENTER_X - (RENDER_LENGTH / 2));
        obstaclePane.setLayoutY(CENTER_Y);

        Rectangle ground = new Rectangle(sideView.getWidth(), sideView.getHeight()/2);
        ground.setY(CENTER_Y);
        ground.setFill(Color.rgb(7, 51, 19));

        viewPane.getChildren().addAll(ground, runway, leftArrow, rightArrow, runwayPane, linePane, obstaclePane);
        viewPane.layout();

        try {
            scale = RENDER_LENGTH / currentRunway.getRunwayLength();
            drawLines();
            renderStrip();
        }catch (Exception e) {
            logger.info("No runway selected yet");
        }
    }

    private void renderStrip() {
        runwayPane.getChildren().clear();
        double stopwayLower = (currentRunway.getLowerRunway().getAsda() - currentRunway.getLowerRunway().getTora()) * scale;
        double clearwayLower = (currentRunway.getLowerRunway().getToda() - currentRunway.getLowerRunway().getTora()) * scale;

        lowerTodaStart = RENDER_LENGTH + clearwayLower;
        Rectangle lowerClearway = new Rectangle(CENTER_X + 325, CENTER_Y, clearwayLower, RUNWAY_WIDTH);
        lowerClearway.setFill(Color.PURPLE);

        Rectangle lowerStopway = new Rectangle(CENTER_X + 325, CENTER_Y, stopwayLower, RUNWAY_WIDTH);
        lowerStopway.setFill(Color.RED);

        double stopwayHigher = (currentRunway.getHigherRunway().getAsda() - currentRunway.getHigherRunway().getTora()) * scale;
        double clearwayHigher = (currentRunway.getHigherRunway().getToda() - currentRunway.getHigherRunway().getTora()) * scale;

        upperTodaStart = 0 - clearwayHigher;
        Rectangle upperClearway = new Rectangle((CENTER_X - 325 - clearwayHigher), CENTER_Y, clearwayHigher, RUNWAY_WIDTH);
        upperClearway.setFill(Color.PURPLE);

        runwayPane.getChildren().addAll(lowerClearway, lowerStopway, upperClearway);
        double stripLength = currentRunway.getRunwayLength();

        logger.info("Strip length = " + stripLength +
            "\nLower runway stopway = " + stopwayLower +
            "\nHigher runway stopway = " + stopwayHigher +
            "\nLower runway clearway = " + clearwayLower +
            "\nHigher runway clearway = " + clearwayHigher);

    }



    public void updateRunway(){
        this.currentRunway = modelState.getCurrentRunway();
        obstacle = null;
        obstaclePane.getChildren().clear();
        linePane.getChildren().clear();
        runwayPane.getChildren().clear();
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
     * Helper method to draw a single runway parameter line with labels and dashed threshold markers
     * @param label Label for the line
     * @param i Flag to dictate where line renders. 1 renders below runway strip, -1 renders above
     * @param length The length of the line to render
     * @param yPos The y position of the line to render
     * @param value The distance value to label the line
     * @param color The colour to render the line
     */
    private void drawSingleLine(String label, int i, double length, double yPos, int value, Color color) {
        double startX;
        double endX;

        if(i == -1) {
            startX = 0;
            endX = length;
        }else{
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

    private void drawLineSpec(String label, int i, double threshPos, double endPos, double yPos, int value, Color color) {
        double startX;
        double endX;

        if(i == -1) {
            startX = threshPos;
            endX = endPos + threshPos ;
        } else{
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
        obstacleShape = new Rectangle(15, obstacle.getHeight(), Color.RED);
        int displacedThreshold = logicalRunway.getDispThreshold();

        double x = ((obstacle.getDistLowerThreshold() + displacedThreshold) * scale) - 7.5;
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

            drawResaHigher();

        }else{
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
        double threshold = (tora - lda) *scale;

        double startX;
        double endX;

        if(i == -1) {
            startX = threshold;
            endX = scaledLda + threshold;
        }else {
            startX = RENDER_LENGTH - threshold;
            endX = RENDER_LENGTH - scaledLda - threshold;
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
        drawSingleLine("TODA", i, scaledTora, baseY + 2* spacing, tora, todaColor);

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

        if(i == -1) {
            logger.info("USING -1 FLAG");
            startX = RENDER_LENGTH - scaledLda;
            endX = RENDER_LENGTH;
        }else {
            logger.info("USING +1 FLAG");
            startX = scaledLda;
            endX = 0;
        }

        double baseY = RUNWAY_WIDTH + (i * 100);

        drawExactLine("LDA", i, startX, endX, baseY, lda, ldaColor);

        renderGlideSlope(startX);

    }

    private void renderGlideSlope(double startX) {
        int obstacleHeight = obstacle.getHeight();
        double obstacleShapeX = obstacleShape.getX();
        double slope;
        double endX;
        double endY;

        //Extend the glide slope past the obstacle
        //Calculate the slope angle and the new end coordinate from obstacle position
        if(obstacleShapeX > startX) {
            logger.info("Obstacle on right side of runway");
            slope = obstacleHeight / (obstacleShapeX - startX) ;
            endX = Math.min((obstacleShapeX + 100.0), 850);
            endY = slope * Math.abs(endX - startX);
        }else{
            logger.info("Obstacle on left side of runway");
            slope = obstacleHeight / (startX - obstacleShapeX);
            endX = Math.max((obstacleShapeX - 100.0), -200);
            endY = slope * Math.abs(startX - endX);
        }

        logger.info(String.format("Rendering glideslope with: \n "
                + "Slope = %.2f \n"
                + "Obstacle height = %d \n"
                + "startX = %.2f \n"
                + "endX = %.2f \n"
                + "endY = %.2f",
            slope, obstacleHeight, startX, obstacleShapeX, endY));

        //TODO: Change colour of glide slope and increase y coordinate to avoid clipping on obstacle
        Line glideSlope = new Line(startX, 0, endX, -1 * (endY + 2));
        glideSlope.setStroke(Color.MAGENTA);
        glideSlope.setStrokeWidth(5);

//        Line glideSlope2 = new Line(obstacleShapeX, -1 * obstacleHeight, endX, -1 * endY );
//        glideSlope2 .setStroke(Color.BLACK);
//        glideSlope2.setStrokeWidth(5);

        linePane.getChildren().addAll(glideSlope);
    }


}
