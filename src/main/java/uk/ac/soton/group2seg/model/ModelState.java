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
  }

  public void getRunwayDetails() {
    currentRunway = currentAirport.getCurrentRunway();
    LogicalRunway lowerRunway = currentRunway.getLowerRunway();
    LogicalRunway higherRunway = currentRunway.getHigherRunway();
    // Lower Runway Original Values
    int lowerRunwayOrgASDA = lowerRunway.getAsda();
    int lowerRunwayOrgTODA = lowerRunway.getToda();
    int lowerRunwayOrgTORA = lowerRunway.getTora();
    int lowerRunwayOrgLDA = lowerRunway.getLda();
    // Lower Runway Current Values
    int lowerRunwayCurASDA = lowerRunway.getCurrAsda();
    int lowerRunwayCurTODA = lowerRunway.getToda();
    int lowerRunwayCurTORA = lowerRunway.getTora();
    int lowerRunwayCurLDA = lowerRunway.getLda();

    //Higher Runway Original Values
    int higherRunwayOrgASDA = higherRunway.getAsda();
    int higherRunwayOrgTODA = higherRunway.getToda();
    int higherRunwayOrgTORA = higherRunway.getTora();
    int higherRunwayOrgLDA = higherRunway.getLda();
    //Higher Runway Current Values
    int higherRunwayCurASDA = higherRunway.getCurrAsda();
    int higherRunwayCurTODA = higherRunway.getToda();
    int higherRunwayCurTORA = higherRunway.getTora();

  }

}
