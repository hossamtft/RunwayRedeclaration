package uk.ac.soton.group2seg.model;

import java.util.ArrayList;
import java.util.HashMap;
import uk.ac.soton.group2seg.model.utility.JaxbUtility;

public class ModelState {
  private HashMap<String,String> airportList;
  private Airport currentAirport;

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

}
