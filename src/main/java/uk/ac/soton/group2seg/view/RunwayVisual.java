package uk.ac.soton.group2seg.view;

import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.scene.layout.Pane;
import javafx.scene.text.Text;


/**
 * @author louistownsend
 */
public class RunwayVisual extends Pane {
  private final double lengthMetres;

  private static final double SCALE = 0.2;

  public RunwayVisual(int length) {
    this.lengthMetres = length;

    // Important: Set alignment to center
    setStyle("-fx-alignment: center;");

    drawRunway();
  }


  private void drawRunway() {
    double width = 60;
    double runwayLength = lengthMetres * SCALE;

    Rectangle runway = new Rectangle(runwayLength, width);
    runway.setFill(Color.DARKGREY);
    runway.setStroke(Color.BLACK);

    Line centreLine = new Line(runway.getX(), runway.getY() + width/2,
        runway.getX() + runwayLength, runway.getY() + width/2);
    centreLine.getStrokeDashArray().addAll(15.0, 10.0);
    centreLine.setStroke(Color.WHITE);
    centreLine.setStrokeWidth(2);

    Line distanceLine = new Line(0, width + 20, runwayLength, width + 20);  // Position the line below the rectangle
    distanceLine.setStroke(Color.BLACK);  // Set the color of the line
    distanceLine.setStrokeWidth(2);  // Make the line a bit thicker for visibility

    Text runwayLengthText = new Text(runwayLength / 2 - 30, width + 40, lengthMetres + "m");
    runwayLengthText.setFill(Color.BLACK);  // Set the color of the text
    runwayLengthText.setStyle("-fx-font-size: 20px;");  // Increase the font size for visibility

    this.getChildren().addAll(runway, distanceLine, centreLine, runwayLengthText);
    this.setPrefSize(runwayLength + 20, width + 60);

  }

}