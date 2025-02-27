package uk.ac.soton.group2seg.model;

import java.util.HashMap;

public class AirportList {

  private HashMap<String,String> airportList;

  public AirportList() {
    this.airportList = new HashMap<>();

  }

  public void addAirport(Airport airport){
    airportList.put(airport.getId(), airport.getName());
  }

  public void addAirportByString(String id, String name) {
    airportList.put(id, name);
  }

  public HashMap<String, String> getList() {
    return airportList;
  }

}
