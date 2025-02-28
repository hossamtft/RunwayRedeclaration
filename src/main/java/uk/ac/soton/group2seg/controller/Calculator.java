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
    private LogicalRunway lowerRunway;
    private LogicalRunway higherRunway;

    // Constants
    private final int RESA = 240; // in meters, Runway End Safety Area
    private final int STRIP_END = 60;
    private final int BLAST_PROTECTION_MINIMUM = 300;
    private final int BLAST_PROTECTION_MAXIMUM = 500;
    private final int SLOPE_RATIO = 50; // 1:50



    public Calculator(Runway runway) {
        this.runway = runway;
        this.lowerRunway = runway.getLowerRunway();
        this.higherRunway = runway.getHigherRunway();
    }

    public void recalculateRunwayParameters(Obstacle obstacle) {

        String closerToThresh = obstaclePosition(obstacle);

        if (closerToThresh == "Left") {
            takingOffTowards(higherRunway, obstacle);
            calculateLDALandingTowards(lowerRunway, obstacle); // check this later
        }
        else if (closerToThresh == "Right") {
            calculateLDALandingTowards(higherRunway, obstacle); // check these
            takingOffTowards(lowerRunway, obstacle);
        }
        // something wrong need to also add Landing Over scenario and taking off away scenario

    }


    public String obstaclePosition(Obstacle obstacle) {
        if (obstacle.getDistLeftThreshold() > obstacle.getDistRightThreshold()) {
            // Closer to left threshold
            // Would be going away/over for higher runway ( TORA TODA ASDA ) , and towards obstacle for lower runway ( LDA )
            return "Left"; // as in closer to left
        }
        else {
            // Closer to right threshold
            // Would be going towards obstacle for higher runway ( LDA )  and away/over from obstacle for lower runway ( TORA TODA ASDA )
            return "Right"; // as in closer to right
        }

    }

    public int calculateLDALandingOver(LogicalRunway sideRunway, Obstacle obstacle) {
        int originalLDA = sideRunway.getLda();
        int slopeRequirmentHeight = obstacle.getHeight() * SLOPE_RATIO;
        int slopeRequiremntOrRESA = Math.max(slopeRequirmentHeight, RESA);
        int newLDA = originalLDA - slopeRequiremntOrRESA - STRIP_END;
        // For Logging/Steps
        // Formula = newLDA = OriginalLDA - (Possibly ObstacleDistance)  - max(Height*50, RESA) - Strip End
        // TODO: Add Obstacle Distance
        return newLDA;
    }

    public int calculateLDALandingTowards(LogicalRunway sideRunway, Obstacle obstacle) {
        int originalLDA = sideRunway.getLda();
        int distanceFromThreshold = obstacle.getDistLeftThreshold();
        int newLDA = distanceFromThreshold - RESA - STRIP_END;
        //For Logging/ Steps
        // Formula = newLDA = distance from displaced threshold - RESA - Strip End
        return newLDA;
    }

    public int takingOffTowards(LogicalRunway sideRunway, Obstacle obstacle) {
        int slopeRequirmentHeight = obstacle.getHeight() * SLOPE_RATIO;
        int slopeRequiremntOrRESA = Math.max(slopeRequirmentHeight, RESA);
        int distanceOfObstacleFromThreshold = 0;// placeholder
        // TODO: Add Obstacle Distance
        int newTora = sideRunway.getTora() + distanceOfObstacleFromThreshold - slopeRequiremntOrRESA - STRIP_END;
        int newAsda = newTora;
        int newToda = newTora;
        return newTora;
        // For Logging/Steps
        // Formula TORA = Obstacle Distance + Displace Threshold - MAX(RESA, Obstacle height*50) - Stripend
        // TORA = TODA = ASDA
    }

    public int takingOffAway(LogicalRunway sideRunway, Obstacle obstacle) {
        //For Logging/Steps
        //Formula:
        // newTora = originalTora - ObstacleDistance - BlastProtection
        // newAsda = originalAsda - ObstacleDistance - BlastProtection
        // newToda = OriginalToda - ObstacleDistance - BlastProtection
        return 0;
    }

}

