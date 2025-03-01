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

      airport.initialise();

      airport.selectRunway("08R/26L");

      airport.selectRunway("08L/26R");

//      System.out.println();


    }catch (Exception e){
      e.printStackTrace();
      System.out.println(e.getMessage());
    }

  }

}
