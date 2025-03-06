package uk.ac.soton.group2seg.model;



public class testing {

    public static void main(String[] args) {
        ModelState modelState = new ModelState();

        System.out.println(modelState.getAirportList());

        modelState.loadAirport("London Heathrow");

        System.out.println(System.getProperty("javafx.version"));

    }

}
