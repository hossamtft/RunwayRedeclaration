package uk.ac.soton.group2seg.view;

import javafx.geometry.Pos;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.ac.soton.group2seg.model.ModelState;
import uk.ac.soton.group2seg.model.Runway;

public class RunwayScreen extends StackPane {
    private final Logger logger = LogManager.getLogger(this.getClass());

    private ModelState modelState;


    public RunwayScreen(ModelState modelState) {
        this.modelState = modelState;
        this.setAlignment(Pos.CENTER);

        loadContent();
    }

    public void updateRunway() {
        // Clear previous visual elements
        this.getChildren().clear();

        setStyle("-fx-alignment: center;");
        drawRunway();
        System.out.println("redrawing runway");
    }

    private void loadContent() {
        VBox vbox = new VBox();
        vbox.setAlignment(Pos.CENTER);
        vbox.setStyle("-fx-background-color: green;");
        double SCALE = 800.0 / modelState.getRunwayLength();
        logger.info("Runway scale = " + SCALE);

        Pane runway = drawRunway();

        vbox.getChildren().addAll(runway);
        this.setAlignment(Pos.CENTER);
        this.getChildren().addAll(vbox);
    }

    private Pane drawRunway() {
        Pane runwayPane = new Pane();
        double width = 60;
        int realLength = modelState.getRunwayLength();
        double length = realLength * 0.2;

        Rectangle strip = new Rectangle(length, width);
        strip.setFill(Color.DARKGREY);
        strip.setStroke(Color.BLACK);

        Line centreLine = new Line(strip.getX(), strip.getY() + width/2,
            strip.getX() + length, strip.getY() + width/2);
        centreLine.getStrokeDashArray().addAll(15.0, 10.0);
        centreLine.setStroke(Color.WHITE);
        centreLine.setStrokeWidth(2);

        runwayPane.getChildren().addAll(strip, centreLine);

        return drawLines(runwayPane);
    }

    private Pane drawLines(Pane runwayPane) {
        int toraLength = modelState.getCurrentRunway().getLowerRunway().getCurrTora();
        double scaledTora = toraLength * 0.2;
        int todaLength = modelState.getCurrentRunway().getLowerRunway().getCurrToda();
        double scaledToda = todaLength * 0.2;

        // Create the TORA line (dashed and in red color)
        Line toraLine = new Line(0, 80, scaledTora, 80);
        toraLine.setStroke(Color.RED);
        toraLine.setStrokeWidth(2);

        // Create text to display TORA length
        Text toraLengthText = new Text(scaledTora/ 2 - 30, 100, toraLength + "m TORA");
        toraLengthText.setFill(Color.RED);
        toraLengthText.setStyle("-fx-font-size: 14px;");

        Line todaLine = new Line(0, 105, scaledToda, 105);
        todaLine.setStroke(Color.BLACK);
        todaLine.setStrokeWidth(2);

        // Create text for runway length
        Text todaText = new Text(scaledToda / 2 - 30, 125, todaLength + "m TODA");
        todaText.setFill(Color.BLACK);
        todaText.setStyle("-fx-font-size: 14px;");

        runwayPane.getChildren().addAll(toraLine, toraLengthText, todaLine, todaText);
        return runwayPane;
    }


}
