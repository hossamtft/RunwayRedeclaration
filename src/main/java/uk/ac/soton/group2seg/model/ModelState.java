package uk.ac.soton.group2seg.model;

import java.util.ArrayList;
import uk.ac.soton.group2seg.model.utility.JaxbUtility;

public class ModelState {
  AirportList airportList;
  Airport currentAirport;

  public ModelState (){
    airportList = JaxbUtility.parseAirports();

  }

  public AirportList getAirportList() {
    return airportList;
  }

  public void loadAirport(String airportId) {
    currentAirport = JaxbUtility.loadAirport(airportId + ".xml");
    currentAirport.initialise();
  }

  /*public ArrayList<Runway>*/

}
