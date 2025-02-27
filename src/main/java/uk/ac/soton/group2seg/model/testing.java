package uk.ac.soton.group2seg.model;

public class testing {

  public static void main(String[] args){
    AirportList airportList = new AirportList();

    LogicalRunway logicalRunway = new LogicalRunway("09L", 3901, 3901, 3901, 3592);
    LogicalRunway logicalRunway1 = new LogicalRunway("27R", 3882, 3960, 3882, 3882);

    LogicalRunway logicalRunway2 = new LogicalRunway("09R", 3658, 3658, 3658, 3350);
    LogicalRunway logicalRunway3 = new LogicalRunway("27L", 3658, 3658, 3658, 3658);

    Runway runway = new Runway(logicalRunway, logicalRunway1);

    Runway runway1 = new Runway(logicalRunway2, logicalRunway3);

    Airport airport = new Airport("EGLL", "London-Heathrow");
    airport.addRunway(runway);
    airport.addRunway(runway1);

    airportList.addAirport(airport);

    System.out.println(airportList.getList());

    for (Runway runway2 : airport.getRunways()) {
      LogicalRunway leftRunway = runway2.getLeftRunway();
      LogicalRunway rightRunway = runway2.getRightRunway();

      System.out.println(runway2.getName());
      System.out.println(leftRunway.getName() + "\n" + rightRunway.getName());
    }
  }
}
