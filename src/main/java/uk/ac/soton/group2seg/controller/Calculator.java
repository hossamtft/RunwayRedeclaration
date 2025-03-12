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
            // Closer to left threshold (ie 09L)
            // Would be going away/over for higher runway ( TORA TODA ASDA ) , and towards obstacle for lower runway ( LDA )
            logger.debug("Obstacle closer to lower runway ({}). Processing lower for takeoff towards and higher for landing over.", lowerRunway.getName());
            takingOffTowards(lowerRunway, obstacle, distanceLowerThresh);
            calculateLDALandingTowards(lowerRunway, obstacle,distanceLowerThresh);
            takingOffAway(higherRunway, obstacle,distanceHigherThresh);
            calculateLDALandingOver(higherRunway, obstacle, distanceHigherThresh);
        }
        else {
            // Closer to right threshold (ie 27R)
            // Would be going towards obstacle for higher runway ( LDA )  and away/over from obstacle for lower runway ( TORA TODA ASDA )
            logger.debug("Obstacle closer to higher runway ({}). Processing higher for takeoff towards and lower for landing over.", higherRunway.getName());
            takingOffTowards(higherRunway, obstacle, distanceHigherThresh);
            calculateLDALandingTowards(higherRunway, obstacle, distanceHigherThresh);
            takingOffAway(lowerRunway, obstacle, distanceLowerThresh);
            calculateLDALandingOver(lowerRunway, obstacle, distanceLowerThresh);
        }
    }

    public int calculateLDALandingOver(LogicalRunway sideRunway, Obstacle obstacle, int distanceFromThresh) {
        logger.info("Calculating LDA over obstacle for runway: {}", sideRunway.getName());
        int originalLDA = sideRunway.getLda();
        int slopeRequirementHeight = obstacle.getHeight() * SLOPE_RATIO;
        int slopeRequirementOrRESA = Math.max(slopeRequirementHeight, RESA);
        int newLDA = originalLDA - distanceFromThresh-  slopeRequirementOrRESA - STRIP_END;
        // For Logging/Steps
        // Formula = newLDA = OriginalLDA - (Possibly ObstacleDistance)  - max(Height*50, RESA) - Strip End
        ldaBreakdown.set(ldaBreakdown.get() + "\n" + "Runway: " + sideRunway.getName() + "\n" +
                "Calculating For Landing Over Obstacle\n" +
                "NewLDA = Original LDA - Distance From Threshold - MAX(Slope*Height, RESA) - Strip End\n" +
                newLDA + " = " + originalLDA + " - " + distanceFromThresh + " - " + slopeRequirementOrRESA + " - " + STRIP_END + "\n");


        logger.debug("LDA Calculation Details - Original: {}, Distance: {}, Slope/RESA: {}, Strip End: {}, Result: {}",
                originalLDA, distanceFromThresh, slopeRequirementOrRESA, STRIP_END, newLDA);
        sideRunway.setCurrLda(newLDA);
        return newLDA;
    }

    public int calculateLDALandingTowards(LogicalRunway sideRunway, Obstacle obstacle, int distanceFromThreshold) {
        logger.info("Calculating LDA towards obstacle for runway: {}", sideRunway.getName());
        int newLDA = distanceFromThreshold - RESA - STRIP_END;

        //For Logging/ Steps
        // Formula = newLDA = distance from displaced threshold - RESA - Strip End
        ldaBreakdown.set("Runway: " + sideRunway.getName() + "\n" +
                "Calculating For Landing Towards Obstacle\n" +
                "NewLDA = Distance From Threshold - RESA - Strip End\n" +
                newLDA + " = " + distanceFromThreshold + " - " + RESA + " - " + STRIP_END + "\n");

        logger.debug("LDA Calculation Details - Distance: {}, RESA: {}, Strip End: {}, Result: {}",
                distanceFromThreshold, RESA, STRIP_END, newLDA);

        sideRunway.setCurrLda(newLDA);
        return newLDA;
    }

    public int takingOffTowards(LogicalRunway sideRunway, Obstacle obstacle, int distanceFromThreshold) {
        logger.info("Calculating TORA towards obstacle for runway: {}", sideRunway.getName());
        int slopeRequirementHeight = obstacle.getHeight() * SLOPE_RATIO;
        int slopeRequirementOrRESA = Math.max(slopeRequirementHeight, RESA);
        int newTora = distanceFromThreshold + sideRunway.getDispThreshold() - slopeRequirementOrRESA - STRIP_END;

        toraBreakdown.set("Runway: " + sideRunway.getName() + "\n" + "Calculating for Take Off Towards Obstalce\n" +"NewTORA = Distance From Threshold + Displaced Threshold - MAX(Slope*Height, RESA) - Strip End\n" +
                newTora + " = " + distanceFromThreshold + " + " + sideRunway.getDispThreshold() + " - " + slopeRequirementOrRESA + " - " + STRIP_END + "\n");
        asdaBreakdown.set("Runway: " + sideRunway.getName() + "\n" +  "Calculating for Take Off Towards Obstalce\n" +
                "ASDA is equal to TORA: " + newTora + "\n");
        todaBreakdown.set("Runway: " + sideRunway.getName() + "\n" + "Calculating for Take Off Towards Obstalce\n" +
                "TODA is equal to TORA: " + newTora + "\n");

        logger.debug("TORA Calculation Details - Distance: {}, Displaced Threshold: {}, Slope/RESA: {}, Strip End: {}, Result: {}",
                distanceFromThreshold, sideRunway.getDispThreshold(), slopeRequirementOrRESA, STRIP_END, newTora);

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
        //For Logging/Steps
        //Formula:
        // newTora = originalTora - ObstacleDistance - BlastProtection
        // newAsda = originalAsda - ObstacleDistance - BlastProtection
        // newToda = OriginalToda - ObstacleDistance - BlastProtection
        int newTora = sideRunway.getTora() - distanceFromThreshold - BLAST_PROTECTION;
        int newAsda = sideRunway.getAsda() - distanceFromThreshold - BLAST_PROTECTION;
        int newToda = sideRunway.getToda() - distanceFromThreshold - BLAST_PROTECTION;

        toraBreakdown.set(toraBreakdown.get() + "\n" + "Runway: " + sideRunway.getName() + "\n" +
                "Calculating For Takeoff Away From Obstacle\n" +
                "NewTORA = Original TORA - Distance From Threshold - Blast Protection\n" +
                newTora + " = " + sideRunway.getTora() + " - " + distanceFromThreshold + " - " + BLAST_PROTECTION + "\n");

        asdaBreakdown.set(asdaBreakdown.get() + "\n" + "Runway: " + sideRunway.getName() + "\n" + "Calculating For Takeoff Away From Obstacle\n" +
                "NewASDA = Original ASDA - Distance From Threshold - Blast Protection\n" +
                newAsda + " = " + sideRunway.getAsda() + " - " + distanceFromThreshold + " - " + BLAST_PROTECTION + "\n");

        todaBreakdown.set(todaBreakdown.get() + "\n"+ "Runway: " + sideRunway.getName() + "\n" + "Calculating For Takeoff Away From Obstacle\n" +
                "NewTODA = Original TODA - Distance From Threshold - Blast Protection\n" +
                newToda + " = " + sideRunway.getToda() + " - " + distanceFromThreshold + " - " + BLAST_PROTECTION + "\n" );


        logger.debug("TORA Calculation Details - Original TORA: {}, Distance: {}, Blast: {}, New TORA: {}, ASDA: {}, TODA: {}",
                sideRunway.getTora(), distanceFromThreshold, BLAST_PROTECTION, newTora, newAsda, newToda);

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