package uk.ac.soton.group2seg.model;

import java.util.Collections;
import java.util.HashMap;
import java.util.Set;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.ac.soton.group2seg.model.utility.JaxbUtility;

public class ModelState {
  private final Logger logger = LogManager.getLogger(this.getClass());

  private HashMap<String,String> airportList;
  private final ObjectProperty<Airport> currentAirport = new SimpleObjectProperty<>();
  private final ObjectProperty<Runway> currentRunway = new SimpleObjectProperty<>();
  private Obstacle obstacle;

  /**
   * Initialise the model
   */
  public ModelState() {
    airportList = JaxbUtility.parseAirports();
    this.obstacle = null;
  }

  /**
   * Returns the hashmap of the airport list
   * @return "Hashmap<AirportName, AirportId>"
   */
  public HashMap<String,String> getAirportList() {
    return airportList;
  }

  /**
   * Load an airport by its name (e.g. London Heathrow)
   * @param airportName The name of the airport to load
   */
  public void loadAirport(String airportName) {
    String airportId = airportList.get(airportName);
    Airport loadedAirport = JaxbUtility.loadAirport(airportId + ".xml");

    assert loadedAirport != null;
    loadedAirport.initialise();

    // Set the new airport and trigger listeners
    currentAirport.set(loadedAirport);
    currentRunway.set(null);
  }

  public void updateAirportList() {
    airportList = JaxbUtility.parseAirports();
  }

  /**
   * Get runway names
   */
  public Set<String> getRunways() {
    if (currentAirport.get() != null) {
      return currentAirport.get().getRunways().keySet();
    }
    return Collections.emptySet();
  }

  /**
   * Select which runway to view
   * @param runwayName The name of the runway to select
   */
  public void selectRunway(String runwayName) {
    if (currentAirport.get() != null) {
      currentAirport.get().selectRunway(runwayName);
      currentRunway.set(currentAirport.get().getCurrentRunway());
    }
  }

  public Runway getCurrentRunway() {
    return currentRunway.get();
  }

  public Airport getCurrentAirport() {
    return currentAirport.get();
  }

  public int getRunwayLength() {
    return currentRunway.get() != null ? currentRunway.get().getRunwayLength() : 0;
  }

  public void setObstacle(Obstacle obstacle) {
    this.obstacle = obstacle;
  }

  public Obstacle getObstacle() {
    return obstacle;
  }

  public ObjectProperty<Airport> currentAirportProperty() {
    return currentAirport;
  }

  public ObjectProperty<Runway> currentRunwayProperty() {
    return currentRunway;
  }
}
