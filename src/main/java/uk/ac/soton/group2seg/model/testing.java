package uk.ac.soton.group2seg.model;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import uk.ac.soton.group2seg.model.utility.JaxbUtility;

public class testing {

  public static void main(String[] args){
    AirportList airportList;
    Airport airport;

    try{
      airportList = JaxbUtility.parseAirports();
      airport = JaxbUtility.loadAirport("EGKK.xml");

      System.out.println(airportList.getList());

      System.out.println(airport.getName());

      ArrayList<Runway> runways = airport.getRunways();

      for(Runway runway : runways) {
        LogicalRunway lowerRunway = runway.getLowerRunway();
        lowerRunway.initialise();
        LogicalRunway higherRunway = runway.getHigherRunway();
        higherRunway.initialise();

        System.out.println(runway.getName());
        System.out.println(lowerRunway.getDistances());
        System.out.println(higherRunway.getDistances());
      }

    }catch (Exception e){
      e.printStackTrace();
      System.out.println(e.getMessage());
    }

  }

}
