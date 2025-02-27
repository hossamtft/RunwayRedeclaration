package uk.ac.soton.group2seg.controller;


import uk.ac.soton.group2seg.model.LogicalRunway;
import uk.ac.soton.group2seg.model.Obstacle;
import uk.ac.soton.group2seg.model.Runway;

/**
 * @author louistownsend
 */
public class Calculator {
    // Runway Calculation is done on
    private Runway runway;
    private LogicalRunway leftRunway;
    private LogicalRunway rightRunway;

    // Constants
    private final int RESA = 240; // in meters, Runway End Safety Area
    private final int STRIP_END = 60;
    private final int BLAST_PROTECTION_MINIMUM = 300;
    private final int BLAST_PROTECTION_MAXIMUM = 500;
    private final int SLOPE_RATIO = 50; // 1:50



    public Calculator(Runway runway) {
        this.runway = runway;
        this.leftRunway = runway.getLeftRunway();
        this.rightRunway = runway.getRightRunway();


    }

    public void recalculateRunwayParameters(Obstacle obstacle) {
        int obstacleHeight = obstacle.getHeight();
        int obstacleWidth = obstacle.getWidth();
        int obstacleDistanceFromLeftThresh = obstacle.getDistLeftThreshold();
        final int RESA = 240; // in meters, Runway End Safety Area

    }
}

