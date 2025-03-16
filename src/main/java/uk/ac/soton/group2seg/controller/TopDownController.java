package uk.ac.soton.group2seg.controller;

import javafx.fxml.FXML;
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
import uk.ac.soton.group2seg.model.Runway;

public class TopDownController {
    private static TopDownController instance = new TopDownController();

    private final Logger logger = LogManager.getLogger(this.getClass());

    @FXML public AnchorPane topDownView;

    private final double RUNWAY_LENGTH = 900;
    private final double RUNWAY_WIDTH = 100.0;

    private Pane runwayPane;
    private ModelState modelState;
    private Runway currentRunway = null;
    private double scale;
    private boolean initialSetupComplete = false;

    public TopDownController() {
       runwayPane = new Pane();
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

        topDownView.setStyle("-fx-background-color: lightgrey");
        topDownView.getChildren().add(runwayPane);

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
        runwayPane.getChildren().clear();

        if (!initialSetupComplete && (topDownView.getWidth() <= 0 || topDownView.getHeight() <= 0)) {
            return; // Skip if dimensions aren't positive during initialization
        }
        if(currentRunway == null) {
            drawEmptyRunway();
        }else{
            logger.info("Drawing runway: " + currentRunway.getName());
            drawRunway();
        }

        logger.info("Positioning runway - width: " + topDownView.getWidth() +
            ", height: " + topDownView.getHeight());

        // Center runway in the AnchorPane
        double centerX = Math.max(0, (topDownView.getWidth() - RUNWAY_LENGTH) / 2);
        double centerY = Math.max(0, (topDownView.getHeight() - RUNWAY_WIDTH) / 2);


        runwayPane.setLayoutX(centerX);
        runwayPane.setLayoutY(centerY);

        drawClearedAndGraded();


    }

    private void drawClearedAndGraded() {
        double runwayCenterX = RUNWAY_LENGTH / 2;
        double runwayCenterY = RUNWAY_WIDTH / 2;

        // Define points relative to the runway center
        double[] points = {
            runwayCenterX - 560, runwayCenterY + 100,
            runwayCenterX - 350, runwayCenterY + 100,
            runwayCenterX - 300, runwayCenterY + 130,
            runwayCenterX + 300, runwayCenterY + 130,
            runwayCenterX + 350, runwayCenterY + 100,
            runwayCenterX + 560, runwayCenterY + 100,
            runwayCenterX + 560, runwayCenterY - 100,
            runwayCenterX + 350, runwayCenterY - 100,
            runwayCenterX + 300, runwayCenterY - 130,
            runwayCenterX - 300, runwayCenterY - 130,
            runwayCenterX - 350, runwayCenterY - 100,
            runwayCenterX - 560, runwayCenterY - 100
        };
        Polygon clearedAndGraded = new Polygon(points);
        clearedAndGraded.setFill(Color.BLUE);

        logger.info(String.format("Centered on (%f, %f)", runwayCenterX, runwayCenterY) + "\nPoints are: " + clearedAndGraded.getPoints().toString());

        runwayPane.getChildren().add(0, clearedAndGraded);
    }

    private void drawRunway() {
        scale = RUNWAY_LENGTH/ currentRunway.getRunwayLength();

        Rectangle strip = new Rectangle(RUNWAY_LENGTH, RUNWAY_WIDTH);
        strip.setFill(Color.DARKGREY);
        strip.setStroke(Color.BLACK);

        Line centreLine = new Line(0, RUNWAY_WIDTH/2, RUNWAY_LENGTH, RUNWAY_WIDTH/2);
        centreLine.getStrokeDashArray().addAll(15.0, 10.0);
        centreLine.setStroke(Color.WHITE);
        centreLine.setStrokeWidth(5);

        runwayPane.getChildren().addAll(strip, centreLine);

        drawLeftLines();
    }

    private void drawLeftLines() {
        LogicalRunway logicalRunway = currentRunway.getLowerRunway();
        double scaledTora = logicalRunway.getCurrTora() * scale;
        double scaledToda = logicalRunway.getCurrToda() * scale;
        double scaledLda = logicalRunway.getCurrLda() * scale;
        double scaledAsda = logicalRunway.getCurrAsda() * scale;

        Line toraLine = new Line(0d, RUNWAY_WIDTH - 200, scaledTora, RUNWAY_WIDTH - 200);
        toraLine.setStroke(Color.BLACK);
        toraLine.setStrokeWidth(2);

        Line toraThresh1 = new Line(0d, 0d, 0d, RUNWAY_WIDTH-200);
        toraThresh1.getStrokeDashArray().addAll(10.0,5.0);
        toraThresh1.setFill(Color.BLACK);
        Line toraThresh2 = new Line(scaledTora, 0d, scaledTora, RUNWAY_WIDTH-200);
        toraThresh2.getStrokeDashArray().addAll(10.0,5.0);
        toraThresh2.setFill(Color.BLACK);

        runwayPane.getChildren().addAll(toraLine, toraThresh1, toraThresh2);
    }

    private void drawEmptyRunway() {
        // Fixed size runway - won't change dimensions when container resizes
        Rectangle strip = new Rectangle(RUNWAY_LENGTH, RUNWAY_WIDTH);
        strip.setFill(Color.DARKGREY);
        strip.setStroke(Color.BLACK);

        Line centreLine = new Line(0, RUNWAY_WIDTH/2, RUNWAY_LENGTH, RUNWAY_WIDTH/2);
        centreLine.getStrokeDashArray().addAll(15.0, 10.0);
        centreLine.setStroke(Color.WHITE);
        centreLine.setStrokeWidth(5);

        runwayPane.getChildren().addAll(strip, centreLine);
    }
}
