package uk.ac.soton.group2seg.model;

import java.util.HashMap;
import java.util.Set;

import uk.ac.soton.group2seg.model.utility.JaxbUtility;

public class ModelState {
  private HashMap<String,String> airportList;
  private Airport currentAirport;
  private Runway currentRunway;
/**
 * Initialise the model
 */
  public ModelState (){
    airportList = JaxbUtility.parseAirports();

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
   * */
  public void loadAirport(String airportName) {
    String airportId = airportList.get(airportName);
    currentAirport = JaxbUtility.loadAirport(airportId + ".xml");

    assert currentAirport != null;
    currentAirport.initialise();
  }

  /**
   * Get runway names
   */
  public Set<String> getRunways() {
    return currentAirport.getRunways().keySet();
  }

  /**
   * Select which runway to view
   * @param runwayName The name of the runway to select
   * */
  public void selectRunway(String runwayName) {
    currentAirport.selectRunway(runwayName);
    currentRunway = currentAirport.getCurrentRunway();
  }

  public Runway getCurrentRunway() {
    return currentRunway;
  }


  public int getRunwayLength() {
    return 3500;
  }

}
