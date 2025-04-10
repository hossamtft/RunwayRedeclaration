package uk.ac.soton.group2seg;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import uk.ac.soton.group2seg.model.ModelState;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit test for simple App.
 */
public class AppTest {

    private ModelState modelState;
    private final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
    private final PrintStream originalOut = System.out;

    @BeforeEach
    void setUp() {
        System.setOut(new PrintStream(outContent));
        modelState = new ModelState();
    }

    @Test
    @DisplayName("Added airport in startup")
    public void testAirportAddedOnStartup()
    {
        HashMap<String, String> airportList = modelState.getAirportList();

        assertNotNull(airportList, "Airport list should not be null on startup");
        assertFalse(airportList.isEmpty(), "Airport list should not be empty on startup");

        String selectedAirport = "London Heathrow";
        assertTrue(airportList.containsKey(selectedAirport), "Selected airport should exist in the list");

        String airportId = airportList.get(selectedAirport);
        assertNotNull(airportId, "Airport ID should not be null");
        assertFalse(airportId.isEmpty(), "Airport ID should not be empty");

        modelState.loadAirport(selectedAirport);

        assertTrue(outContent.toString().contains("Loading airport: " + selectedAirport),
                "Terminal should log: Loading airport: " + selectedAirport);
    }
    @Test
    @DisplayName("Airport defined")
    public void airportDef()
    {
        // requires gui testing
    }

    @Test
    @DisplayName("Runway defined")
    public void runwayDef()
    {

    }

}
