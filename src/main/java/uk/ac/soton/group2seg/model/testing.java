package uk.ac.soton.group2seg.model;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import uk.ac.soton.group2seg.model.utility.JaxbUtility;

public class testing {

  public static void main(String[] args){
   ModelState modelState = new ModelState();

   System.out.println(modelState.getAirportList());

   modelState.loadAirport("London Heathrow");

   //modelState.



  }

}
