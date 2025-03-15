package uk.ac.soton.group2seg.view;

import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.scene.layout.Pane;
import javafx.scene.text.Text;
import uk.ac.soton.group2seg.model.Runway;

public class RunwayVisual extends Pane {

  private static final double SCALE = 0.2;
  private Runway currentRunway;

  public RunwayVisual(Runway currentRunway) {
    this.currentRunway = currentRunway;
    updateRunway();  // Draw the runway initially
  }

  // Method to update the runway visualization with the latest values
  public void updateRunway() {
    // Fetch updated TORA and TODA from the current runway object
    int toraLength = currentRunway.getHigherRunway().getTora();
    int todaLength = currentRunway.getHigherRunway().getToda();

    // Clear previous visual elements
    this.getChildren().clear();

    setStyle("-fx-alignment: center;");
    drawRunway(toraLength, todaLength);
    System.out.println("redrawing runway");
  }

  private void drawRunway(int toraLength, int todaLength) {
    double width = 60; // Fixed width of the runway
    double runwayLength = toraLength * SCALE; // Scale the TORA length for visualization

    // Create the runway rectangle
    Rectangle runway = new Rectangle(runwayLength, width);
    runway.setFill(Color.DARKGREY);
    runway.setStroke(Color.BLACK);

    // Create a dashed center line on the runway
    Line centreLine = new Line(runway.getX(), runway.getY() + width / 2,
            runway.getX() + runwayLength, runway.getY() + width / 2);
    centreLine.getStrokeDashArray().addAll(15.0, 10.0);
    centreLine.setStroke(Color.WHITE);
    centreLine.setStrokeWidth(2);

//    // Create the TORA line (dashed and in red color)
//    Line toraLine = new Line(0, width + 40, runwayLength, width + 40);
//    toraLine.getStrokeDashArray().addAll(15.0, 10.0);
//    toraLine.setStroke(Color.RED);
//    toraLine.setStrokeWidth(2);
//
//    // Create text to display TORA length
//    Text toraLengthText = new Text(runwayLength / 2 - 30, width + 60, toraLength + "m TORA");
//    toraLengthText.setFill(Color.RED);
//    toraLengthText.setStyle("-fx-font-size: 20px;");
//
//    // Create a distance line below the runway
//    Line distanceLine = new Line(0, width + 20, runwayLength, width + 20);
//    distanceLine.setStroke(Color.BLACK);
//    distanceLine.setStrokeWidth(2);
//
//    // Create text for runway length
//    Text runwayLengthText = new Text(runwayLength / 2 - 30, width + 40, toraLength + "m length");
//    runwayLengthText.setFill(Color.BLACK);
//    runwayLengthText.setStyle("-fx-font-size: 20px;");

    // Add all elements to the pane
    this.getChildren().addAll(runway,  centreLine);

    // Set the preferred size of the pane to fit the elements
    this.setPrefSize(runwayLength + 20, width + 80); // Adding some extra space for lines and text
  }
}
