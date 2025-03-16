package uk.ac.soton.group2seg.controller;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.ac.soton.group2seg.model.LogicalRunway;
import uk.ac.soton.group2seg.model.Obstacle;
import uk.ac.soton.group2seg.model.Runway;

/**
 * @author louistownsend
 */
public class Calculator {
    private final Logger logger = LogManager.getLogger(this.getClass());
    private final Logger calculationLogger = LogManager.getLogger("CalculationLogger");
    // Runway Calculation is done on
    private Runway runway;
    private LogicalRunway lowerRunway;
    private LogicalRunway higherRunway;

    // Constants
    private final int RESA = 240; // in meters, Runway End Safety Area
    private final int STRIP_END = 60;
    private final int BLAST_PROTECTION = 300;
    private final int SLOPE_RATIO = 50; // 1:50

    private StringProperty asdaBreakdown = new SimpleStringProperty("");
    private StringProperty toraBreakdown = new SimpleStringProperty("");
    private StringProperty todaBreakdown = new SimpleStringProperty("");
    private StringProperty ldaBreakdown = new SimpleStringProperty("");



    public Calculator(Runway runway) {
        logger.debug("Initializing Calculator with runway: {}", runway);
        this.runway = runway;
        this.lowerRunway = runway.getLowerRunway();
        this.higherRunway = runway.getHigherRunway();
    }

    public void redeclareRunway(Obstacle obstacle) {
        logger.info("New obstacle added: {}", obstacle);
        int distanceLowerThresh = obstacle.getDistLowerThreshold(); // left thresh
        int distanceHigherThresh = obstacle.getDistHigherThreshold(); // right thresh
        ldaBreakdown.set("");
        asdaBreakdown.set("");
        toraBreakdown.set("");
        todaBreakdown.set("");
        if (distanceLowerThresh > distanceHigherThresh) {
            // Further from lower threshold (ie 09L)
            // Taking off away/landing over for higher runway ( TORA TODA ASDA )
            // Taking off/landing towards obstacle for lower runway ( LDA )
            logger.debug("Obstacle closer to lower runway ({}). Processing lower for takeoff towards and higher for landing over.", lowerRunway.getName());
            takingOffTowards(lowerRunway, obstacle, distanceLowerThresh);
            calculateLDALandingTowards(lowerRunway, obstacle,distanceLowerThresh);
            takingOffAway(higherRunway, obstacle,distanceHigherThresh);
            calculateLDALandingOver(higherRunway, obstacle, distanceHigherThresh);

            //Flag for graphics calculations
            obstacle.setCloserLower(true);
        }
        else {
            // Closer to lower threshold (ie 09L)
            // Would be going towards obstacle for higher runway ( LDA )  and away/over from obstacle for lower runway ( TORA TODA ASDA )
            logger.debug("Obstacle closer to higher runway ({}). Processing higher for takeoff towards and lower for landing over.", higherRunway.getName());
            takingOffTowards(higherRunway, obstacle, distanceHigherThresh);
            calculateLDALandingTowards(higherRunway, obstacle, distanceHigherThresh);
            takingOffAway(lowerRunway, obstacle, distanceLowerThresh);
            calculateLDALandingOver(lowerRunway, obstacle, distanceLowerThresh);

            //Flag for graphics calculations
            obstacle.setCloserLower(false);
        }
    }

    public int calculateLDALandingOver(LogicalRunway sideRunway, Obstacle obstacle, int distanceFromThresh) {
        int originalLDA = sideRunway.getLda();
        int slopeRequirementHeight = obstacle.getHeight() * SLOPE_RATIO;
        int slopeRequirementOrRESA = Math.max(slopeRequirementHeight, RESA);
        int newLDA = originalLDA - distanceFromThresh - slopeRequirementOrRESA - STRIP_END;

        String calculationBreakdown = "Runway: " + sideRunway.getName() + "\n" +
                "Calculating For Landing Over Obstacle\n" +
                "NewLDA = Original LDA - Distance From Threshold - MAX(Slope * Height, RESA) - Strip End\n" +
                newLDA + " = " + originalLDA + " - " + distanceFromThresh + " - " + slopeRequirementOrRESA + " - " + STRIP_END + "\n";

        ldaBreakdown.set(calculationBreakdown);
        calculationLogger.info(calculationBreakdown);

        sideRunway.setCurrLda(newLDA);
        return newLDA;
    }


    public int calculateLDALandingTowards(LogicalRunway sideRunway, Obstacle obstacle, int distanceFromThreshold) {
        logger.info("Calculating LDA towards obstacle for runway: {}", sideRunway.getName());
        int newLDA = distanceFromThreshold - RESA - STRIP_END;

        //For Logging/ Steps
        // Formula = newLDA = distance from displaced threshold - RESA - Strip End
        String calculationBreakdown = "Runway: " + sideRunway.getName() + "\n" +
                "Calculating For Landing Towards Obstacle\n" +
                "NewLDA = Distance From Threshold - RESA - Strip End\n" +
                newLDA + " = " + distanceFromThreshold + " - " + RESA + " - " + STRIP_END + "\n";
        ldaBreakdown.set(calculationBreakdown);
        calculationLogger.info(calculationBreakdown);

        sideRunway.setCurrLda(newLDA);
        return newLDA;
    }

    public int takingOffTowards(LogicalRunway sideRunway, Obstacle obstacle, int distanceFromThreshold) {
        logger.info("Calculating TORA towards obstacle for runway: {}", sideRunway.getName());
        int slopeRequirementHeight = obstacle.getHeight() * SLOPE_RATIO;
        int slopeRequirementOrRESA = Math.max(slopeRequirementHeight, RESA);
        int newTora = distanceFromThreshold + sideRunway.getDispThreshold() - slopeRequirementOrRESA - STRIP_END;

        String toraBreakdownString = "Runway: " + sideRunway.getName() + "\n" + "Calculating for Take Off Towards Obstalce\n" +"NewTORA = Distance From Threshold + Displaced Threshold - MAX(Slope*Height, RESA) - Strip End\n" +
                newTora + " = " + distanceFromThreshold + " + " + sideRunway.getDispThreshold() + " - " + slopeRequirementOrRESA + " - " + STRIP_END + "\n";
        toraBreakdown.set(toraBreakdownString);

        String asdaBreakdownString = "Runway: " + sideRunway.getName() + "\n" +  "Calculating for Take Off Towards Obstalce\n" +
                "ASDA is equal to TORA: " + newTora + "\n";
        asdaBreakdown.set(asdaBreakdownString);

        String todaBreakdownString = "Runway: " + sideRunway.getName() + "\n" + "Calculating for Take Off Towards Obstalce\n" +
                "TODA is equal to TORA: " + newTora + "\n";
        todaBreakdown.set(todaBreakdownString);

        calculationLogger.info(toraBreakdownString);
        calculationLogger.info(asdaBreakdownString);
        calculationLogger.info(todaBreakdownString);

        sideRunway.setCurrTora(newTora);
        sideRunway.setCurrAsda(newTora);
        sideRunway.setCurrToda(newTora);
        return newTora;
        // For Logging/Steps
        // Formula TORA = Obstacle Distance + Displace Threshold - MAX(RESA, Obstacle height*50) - Stripend
        // TORA = TODA = ASDA
    }

    public int takingOffAway(LogicalRunway sideRunway, Obstacle obstacle, int distanceFromThreshold) {
        logger.info("Calculating TORA away from obstacle for runway: {}", sideRunway.getName());
        //TODO factor in displaced thresholds to the calculations.
        //Distance from threshold + displaced threshold is the distance from the actual runway end

        // Calculation formulas:
        // newTora = originalTora - distanceFromThreshold - BLAST_PROTECTION
        // newAsda = originalAsda - distanceFromThreshold - BLAST_PROTECTION
        // newToda = originalToda - distanceFromThreshold - BLAST_PROTECTION
        int newTora = sideRunway.getTora() - distanceFromThreshold - BLAST_PROTECTION;
        int newAsda = sideRunway.getAsda() - distanceFromThreshold - BLAST_PROTECTION;
        int newToda = sideRunway.getToda() - distanceFromThreshold - BLAST_PROTECTION;

        // Logging breakdown for clarity
        String toraBreakdownString = "Runway: " + sideRunway.getName() + "\n" +
                "Calculating For Takeoff Away From Obstacle\n" +
                "NewTORA = Original TORA - Distance From Threshold - Blast Protection\n" +
                newTora + " = " + sideRunway.getTora() + " - " + distanceFromThreshold + " - " + BLAST_PROTECTION + "\n";
        toraBreakdown.set(toraBreakdownString);

        String asdaBreakdownString = "Runway: " + sideRunway.getName() + "\n" +
                "Calculating For Takeoff Away From Obstacle\n" +
                "NewASDA = Original ASDA - Distance From Threshold - Blast Protection\n" +
                newAsda + " = " + sideRunway.getAsda() + " - " + distanceFromThreshold + " - " + BLAST_PROTECTION + "\n";
        asdaBreakdown.set(asdaBreakdownString);

        String todaBreakdownString = "Runway: " + sideRunway.getName() + "\n" +
                "Calculating For Takeoff Away From Obstacle\n" +
                "NewTODA = Original TODA - Distance From Threshold - Blast Protection\n" +
                newToda + " = " + sideRunway.getToda() + " - " + distanceFromThreshold + " - " + BLAST_PROTECTION + "\n";
        todaBreakdown.set(todaBreakdownString);

        // Log the breakdown of calculations
        calculationLogger.info(toraBreakdownString);
        calculationLogger.info(asdaBreakdownString);
        calculationLogger.info(todaBreakdownString);

        // Set updated runway values
        sideRunway.setCurrTora(newTora);
        sideRunway.setCurrAsda(newAsda);
        sideRunway.setCurrToda(newToda);

        return newTora;
    }

    public StringProperty getAsdaBreakdown() {
        return asdaBreakdown;
    }

    public StringProperty getToraBreakdown() {
        return toraBreakdown;
    }

    public StringProperty getTodaBreakdown() {
        return todaBreakdown;
    }

    public StringProperty getLdaBreakdown() {
        return ldaBreakdown;
    }
}