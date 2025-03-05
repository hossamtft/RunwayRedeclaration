package uk.ac.soton.group2seg.view;

import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.scene.layout.Pane;


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

    this.getChildren().addAll(runway, centreLine);

    // Set preferred size to accommodate the runway
    this.setPrefSize(runwayLength + 20, width + 20); // Add some padding
  }
}