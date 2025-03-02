package uk.ac.soton.group2seg.model;

import java.util.ArrayList;
import java.util.HashMap;
import uk.ac.soton.group2seg.model.utility.JaxbUtility;

public class ModelState {
  private HashMap<String,String> airportList;
  private Airport currentAirport;

  public ModelState (){
    airportList = JaxbUtility.parseAirports();

  }

  public HashMap<String,String> getAirportList() {
    return airportList;
  }

  public void loadAirport(String airportName) {
    String airportId = airportList.get(airportName);
    currentAirport = JaxbUtility.loadAirport(airportId + ".xml");
    currentAirport.initialise();
  }

  public HashMap<String, Runway> getRunways() {
    return currentAirport.getRunways();
  }

}
