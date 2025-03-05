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
    private final int BLAST_PROTECTION = 300;
    private final int SLOPE_RATIO = 50; // 1:50



    public Calculator(Runway runway) {
        this.runway = runway;
        this.lowerRunway = runway.getLowerRunway();
        this.higherRunway = runway.getHigherRunway();
    }

    public void redeclareRunway(Obstacle obstacle) {
        int distanceLowerThresh = obstacle.getDistLowerThreshold(); // left thresh
        int distanceHigherThresh = obstacle.getDistHigherThreshold(); // right thresh
        if (distanceLowerThresh > distanceHigherThresh) {
            // Closer to left threshold (ie 09L)
            // Would be going away/over for higher runway ( TORA TODA ASDA ) , and towards obstacle for lower runway ( LDA )

            takingOffTowards(lowerRunway, obstacle, distanceLowerThresh);
            calculateLDALandingTowards(lowerRunway, obstacle,distanceLowerThresh); // check this later

            takingOffAway(higherRunway, obstacle,distanceHigherThresh);
            calculateLDALandingOver(higherRunway, obstacle);
        }
        else {
            // Closer to right threshold (ie 27R)
            // Would be going towards obstacle for higher runway ( LDA )  and away/over from obstacle for lower runway ( TORA TODA ASDA )

            takingOffTowards(higherRunway, obstacle, distanceHigherThresh);
            calculateLDALandingTowards(higherRunway, obstacle, distanceHigherThresh); // check these4

            takingOffAway(lowerRunway, obstacle, distanceLowerThresh);
            calculateLDALandingOver(lowerRunway, obstacle);
        }

    }

    public int calculateLDALandingOver(LogicalRunway sideRunway, Obstacle obstacle) {
        int originalLDA = sideRunway.getLda();
        int slopeRequirmentHeight = obstacle.getHeight() * SLOPE_RATIO;
        int slopeRequiremntOrRESA = Math.max(slopeRequirmentHeight, RESA);
        int newLDA = originalLDA - slopeRequiremntOrRESA - STRIP_END;
        // For Logging/Steps
        // Formula = newLDA = OriginalLDA - (Possibly ObstacleDistance)  - max(Height*50, RESA) - Strip End
        return newLDA;
    }

    public int calculateLDALandingTowards(LogicalRunway sideRunway, Obstacle obstacle, int distanceFromThreshold) {
        int newLDA = distanceFromThreshold - RESA - STRIP_END;

        //For Logging/ Steps
        // Formula = newLDA = distance from displaced threshold - RESA - Strip End
        return newLDA;
    }

    public int takingOffTowards(LogicalRunway sideRunway, Obstacle obstacle,int distanceFromThreshold) {
        int slopeRequirementHeight = obstacle.getHeight() * SLOPE_RATIO;
        int slopeRequirementOrRESA = Math.max(slopeRequirementHeight, RESA);
        int newTora = distanceFromThreshold + sideRunway.getDispThreshold() - slopeRequirementOrRESA - STRIP_END;
        int newAsda = newTora;
        int newToda = newTora;

        sideRunway.setCurrTora(newTora);
        sideRunway.setCurrAsda(newAsda);
        sideRunway.setCurrToda(newToda);

        return newTora;
        // For Logging/Steps
        // Formula TORA = Obstacle Distance + Displace Threshold - MAX(RESA, Obstacle height*50) - Stripend
        // TORA = TODA = ASDA
    }

    public int takingOffAway(LogicalRunway sideRunway, Obstacle obstacle,int distanceFromThreshold) {
        //For Logging/Steps
        //Formula:
        // newTora = originalTora - ObstacleDistance - BlastProtection
        // newAsda = originalAsda - ObstacleDistance - BlastProtection
        // newToda = OriginalToda - ObstacleDistance - BlastProtection
        int newTora = sideRunway.getTora() - distanceFromThreshold - BLAST_PROTECTION;
        int newAsda = sideRunway.getAsda() - distanceFromThreshold - BLAST_PROTECTION;
        int newToda = sideRunway.getToda() - distanceFromThreshold - BLAST_PROTECTION;

        sideRunway.setCurrTora(newTora);
        sideRunway.setCurrAsda(newAsda);
        sideRunway.setCurrToda(newToda);

        return newTora;
    }

}

