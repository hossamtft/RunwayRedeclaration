package uk.ac.soton.group2seg.model;

import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlElementWrapper;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlTransient;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Represents an airport, containing an identifier (eg, EGLL), name (e.g. London Heathrow) and a
 * list of runways. This class is annotated for marshalling and unmarshalling using JAXB
 *
 * @author louistownsend
 */
@XmlRootElement(name = "airport")
@XmlAccessorType(XmlAccessType.FIELD)
public class Airport {

    @XmlElement(name = "id")
    private String id;
    @XmlElement(name = "name")
    private String name;

    @XmlElementWrapper(name = "runways")
    @XmlElement(name = "runway")
    private ArrayList<Runway> runways;

    @XmlTransient
    private HashMap<String, Runway> runwayMap = new HashMap<>();

    @XmlTransient
    private Runway currentRunway;

    /**
     * Default empty constructor required for JAXB
     */
    public Airport() {
        System.out.println("Airport constructor");
    }

    /**
     * Constructs an Airport with the given identifier and name.
     *
     * @param id   The unique identifier for the airport.
     * @param name The name of the airport.
     */
    public Airport(String id, String name) {
        this.id = id;
        this.name = name;
        this.runways = new ArrayList<>();
        this.runwayMap = new HashMap<>();
    }

    /**
     * Initialises runways at the airport
     */
    public void initialise() {
        for (Runway runway : runways) {
            runwayMap.put(runway.getName(), runway);
        }
    }

    /**
     * Adds a runway to the airport
     *
     * @param runway The runway to add
     */
    public void addRunway(Runway runway) {
        runways.add(runway);
    }

    /**
     * Select the current runway using its bidirectional designator (ie "8R/26L")
     *
     * @param runwayName The name of the runway
     */
    public void selectRunway(String runwayName) {
        currentRunway = runwayMap.get(runwayName);
        currentRunway.getHigherRunway().initialise();
        currentRunway.getLowerRunway().initialise();
        System.out.println("Setting current runway to: " + runwayName);
    }


    /**
     * Retrieves the list of runways at this airport. This method is used by JAXB during XML
     * marshalling.
     *
     * @return A hashmap - Key= Runway name, Value = Runway object
     */

    public HashMap<String, Runway> getRunways() {
        return runwayMap;
    }

    /***
     * @return The unique ID
     */
    public String getId() {
        return id;
    }

    /**
     * @return The airport name
     */
    public String getName() {
        return name;
    }

    /**
     * @param name The airport name
     */
    protected void setName(String name) {
        System.out.println("Setting name");
        this.name = name;
    }

    /**
     * @param id The unique ID
     */
    protected void setId(String id) {
        this.id = id;
    }

    /**
     * Sets the list of runways for the airport Used by JAXB during XML unmarshalling
     *
     * @param runways The list of runways to set
     */
    protected void setRunways(ArrayList<Runway> runways) {
        this.runways = runways;
    }

    public ArrayList<Runway> getRunwayList() {
        return runways;
    }
    public Runway getCurrentRunway() {
        return currentRunway;
    }
}
