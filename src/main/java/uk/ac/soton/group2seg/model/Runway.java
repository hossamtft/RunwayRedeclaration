package uk.ac.soton.group2seg.model;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a physical runway, which consists of two logical runways. This class is annotated for
 * JAXB to allow marshalling and unmarshalling to and from XML.
 *
 * @author louistownsend
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class Runway {

    @XmlElement(name = "runwayName")
    private String name;

    @XmlElement(name = "logicalRunway")
    private ArrayList<LogicalRunway> logicalRunways;

    /**
     * Default constructor for JAXB and general use. Initialises the list of logical runways.
     */
    public Runway() {
        //this.logicalRunways = new ArrayList<>();
    }

    /**
     * Constructs a runway made up of two logical runways. The name of the runway is derived from
     * the names of the provided logical runways.
     *
     * @param lowerRunway  The logical runway with the lower heading.
     * @param higherRunway The logical runway with the higher heading.
     */
    public Runway(LogicalRunway lowerRunway, LogicalRunway higherRunway) {
        this.logicalRunways = new ArrayList<>();
        this.name = lowerRunway.getName() + "/" + higherRunway.getName();
        this.logicalRunways.add(lowerRunway);
        this.logicalRunways.add(higherRunway);
    }

    /**
     * Sets the name of the runway. This method is used by JAXB during unmarshalling.
     *
     * @param name The name to set.
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Retrieves the list of logical runways associated with this runway.
     *
     * @return The list of logical runways.
     */
    public List<LogicalRunway> getLogicalRunways() {
        return logicalRunways;
    }

    /**
     * Sets the list of logical runways. This method is used by JAXB during unmarshalling.
     *
     * @param logicalRunways The list of logical runways to set.
     */
    protected void setLogicalRunways(ArrayList<LogicalRunway> logicalRunways) {
        this.logicalRunways = logicalRunways;
    }

    /**
     * Retrieves the logical runway designated as the lower runway. This is the runway with a
     * heading between 01 and 18.
     *
     * @return The lower logical runway, or null if no suitable runway is found.
     */
    public LogicalRunway getLowerRunway() {
        for (LogicalRunway lr : logicalRunways) {
            int heading = parseHeading(lr.getName());
            if (heading >= 1 && heading <= 18) {
                return lr;
            }
        }
        return null;
    }

    /**
     * Retrieves the logical runway designated as the upper runway. This is the runway with a
     * heading between 19 and 36.
     *
     * @return The upper logical runway, or null if no suitable runway is found.
     */
    public LogicalRunway getHigherRunway() {
        for (LogicalRunway lr : logicalRunways) {
            int heading = parseHeading(lr.getName());
            if (heading >= 19 && heading <= 36) {
                return lr;
            }
        }
        return null;
    }

    /**
     * Retrieves the name of the runway.
     *
     * @return The name of the runway.
     */
    public String getName() {
        return name;
    }

    /**
     * Parses the numeric heading from a runway name. Runway names are expected to contain numeric
     * headings (e.g., "09L", "27R").
     *
     * @param runwayName The name of the runway to parse.
     * @return The numeric heading, or -1 if parsing fails.
     */
    private int parseHeading(String runwayName) {
        try {
            return Integer.parseInt(runwayName.replaceAll("[^0-9]", ""));
        } catch (NumberFormatException e) {
            System.err.println("Could not parse heading from runway: " + runwayName);
            return -1;
        }
    }

    public int getRunwayLength() {
        int maxLength = 0;
        for(LogicalRunway runway : logicalRunways) {
            if(runway.getTora() > maxLength) {
                maxLength = runway.getTora();
            }
        }
        return maxLength;
    }

    public int getStripLength() {
        int maxLength = 0;
        for(LogicalRunway runway : logicalRunways) {
            if(runway.getTora() > maxLength) {
                maxLength = runway.getTora();
            }
        }
        return maxLength;
    }


    public int getThresholdDistance() {
        int minLength = Integer.MAX_VALUE;
        for(LogicalRunway runway : logicalRunways) {
            if (runway.getLda() < minLength) {
                minLength = runway.getLda();
            }
        }

        return minLength;
    }


}
