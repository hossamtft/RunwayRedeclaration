package uk.ac.soton.group2seg.model;

import uk.ac.soton.group2seg.model.utility.JaxbUtility;

public class ModelState {
  AirportList airportList;
  Airport currentAirport;

  public ModelState (){
    airportList = JaxbUtility.parseAirports();
    currentAirport = JaxbUtility.loadAirport("EGLL.xml");
  }

  public AirportList getAirportList() {
    return airportList;
  }
}
