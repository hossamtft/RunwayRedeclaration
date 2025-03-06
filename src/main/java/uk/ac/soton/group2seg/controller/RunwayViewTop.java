package uk.ac.soton.group2seg.controller;

import javafx.scene.layout.Pane;
import uk.ac.soton.group2seg.model.LogicalRunway;
import javafx.scene.shape.Rectangle;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;


public class RunwayViewTop
{

    private Pane runwayPane;
    private LogicalRunway runway;


    public RunwayViewTop()
    {
        this.runwayPane = new Pane();
    }

    public void createRunwayTop(LogicalRunway runway)
    {
        this.runway = runway;
        runwayPane.getChildren().clear();
        int runwayLength = runway.getTora();

        Rectangle runwayVisual = new Rectangle(0, 0, runwayLength, 50);
        runwayVisual.setFill(Color.GRAY);

        Text runwayName = new Text(10, 60, "Runway: " + runway.getName());
        Text runwayDetails = new Text(10, 80, "TORA: " + runway.getTora() + "m  TODA: " + runway.getToda() + "m  ASDA: " + runway.getAsda() + "m");

        runwayPane.getChildren().addAll(runwayVisual, runwayName, runwayDetails);
    }

    public Pane getRunwayPane() {
        return runwayPane;
    }

    public void distances()
    {

    }
}
