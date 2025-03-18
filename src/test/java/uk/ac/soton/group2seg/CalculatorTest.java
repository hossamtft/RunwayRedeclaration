package uk.ac.soton.group2seg;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.ac.soton.group2seg.controller.Calculator;
import uk.ac.soton.group2seg.model.LogicalRunway;
import uk.ac.soton.group2seg.model.Obstacle;
import uk.ac.soton.group2seg.model.Runway;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CalculatorTest {
    private Runway runway09L27R;
    private Calculator calculator;

    @BeforeEach
    void setUp() {
        LogicalRunway lowerRunway09L = new LogicalRunway("09L", 3902, 3902, 3902, 3595);
        LogicalRunway higherRunway27R = new LogicalRunway("27R", 3884, 3962, 3884, 3884);

        runway09L27R = new Runway(lowerRunway09L, higherRunway27R);
        calculator = new Calculator(runway09L27R);
    }

    @Test
    void testLandingOverObstacle() {
        Obstacle obstacle = new Obstacle(25, 3384, 500, 0);
        calculator.redeclareRunway(obstacle);
        int newLDA = runway09L27R.getHigherRunway().getCurrLda();
        assertEquals(2074, newLDA);
    }

    @Test
    void testLandingTowardsObstacle() {
        Obstacle obstacle = new Obstacle(0, 2600, 1302, 0);
        calculator.redeclareRunway(obstacle);
        int newLDA = runway09L27R.getLowerRunway().getCurrLda();
        assertEquals(2300, newLDA);

    }

    @Test
    void testTakingOffTowardsObstacle() {
        Obstacle obstacle = new Obstacle(25, 2500, 1402, 0);
        calculator.redeclareRunway(obstacle);
        int newTORA = runway09L27R.getLowerRunway().getCurrTora();
        // Test fails due to displaced threshold being calculated differently to what's in the project definition
        assertEquals(1497, newTORA);
        assertEquals(1497, runway09L27R.getLowerRunway().getCurrAsda());
        assertEquals(1497, runway09L27R.getLowerRunway().getCurrToda());

    }

    @Test
    void testTakingOffAwayFromObstacle() {
        Obstacle obstacle = new Obstacle(0, 3384, 500, 0); // Distance from 27R threshold
        calculator.redeclareRunway(obstacle);
        int newTORA = runway09L27R.getHigherRunway().getCurrTora();
        assertEquals(3084, newTORA);
        assertEquals(3162, runway09L27R.getHigherRunway().getCurrToda()); // TODA = 3962 - 500 - 300 = 3162m
        assertEquals(3084, runway09L27R.getHigherRunway().getCurrAsda()); // ASDA = 3884 - 500 - 300 = 3084m

    }
}