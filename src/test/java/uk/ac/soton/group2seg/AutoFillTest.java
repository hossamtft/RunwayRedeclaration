package uk.ac.soton.group2seg;

import javafx.application.Platform;
import javafx.scene.control.TextField;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import uk.ac.soton.group2seg.controller.ObstacleFormController;
import uk.ac.soton.group2seg.model.LogicalRunway;
import uk.ac.soton.group2seg.model.Runway;

import java.lang.reflect.Field;
import java.util.concurrent.CountDownLatch;

import static org.junit.jupiter.api.Assertions.*;

public class AutoFillTest {

    private ObstacleFormController controller;
    private TextField sourceDistance;
    private TextField finalDistance;
    private LogicalRunway lowerRunway;
    private LogicalRunway higherRunway;

    @BeforeAll
    static void initToolkit() throws Exception {
        // To allow JAVAFX elements ( TextFields ) to be initialized
        CountDownLatch latch = new CountDownLatch(1);
        Platform.startup(latch::countDown);
        latch.await();
    }

    @BeforeEach
    void setUp() throws Exception {
        controller = new ObstacleFormController();
        sourceDistance = new TextField();
        finalDistance = new TextField();
        Field autoFillEnabledField = ObstacleFormController.class.getDeclaredField("autoFillEnabled");
        autoFillEnabledField.setAccessible(true);
        autoFillEnabledField.set(controller, true);
        lowerRunway = new LogicalRunway("09R", 3660, 3660, 3660, 3353);
        higherRunway = new LogicalRunway("27L", 3660, 3660, 3660, 3660);
        controller.setRunways(new Runway(lowerRunway, higherRunway));
    }

    @Test
    @DisplayName("Test with runway 09R/27L - obstacle 50m before 09R threshold")
    void testObstacle50mBefore09RThreshold() {
        sourceDistance.setText("-50");
        Integer result = controller.updateOppositeThreshold(sourceDistance, finalDistance);
        assertEquals(3403, result);
        assertEquals("3403", finalDistance.getText());
    }

    @Test
    @DisplayName("Test with runway 09R/27L - obstacle 150m from 09R threshold")
    void testObstacle150mFrom09RThreshold() {
        sourceDistance.setText("150");
        Integer result = controller.updateOppositeThreshold(sourceDistance, finalDistance);
        assertEquals(3203, result);
        assertEquals("3203", finalDistance.getText());
    }

    @Test
    @DisplayName("Test with runway 09R/27L - obstacle 500m from 27L threshold")
    void testObstacle500mFrom27LThreshold() {
        sourceDistance.setText("500");
        Integer result = controller.updateOppositeThreshold(sourceDistance, finalDistance);
        assertEquals(2853, result);
        assertEquals("2853", finalDistance.getText());
    }

    @Test
    @DisplayName("Test with runway 09L/27R - obstacle 50m before 09L threshold")
    void testObstacle50mBefore09LThreshold() {
        lowerRunway = new LogicalRunway("09L", 3902, 3902, 3902, 3595);
        higherRunway = new LogicalRunway("27R", 3884, 3962, 3884, 3884);
        controller.setRunways(new Runway(lowerRunway, higherRunway));
        sourceDistance.setText("-50");
        Integer result = controller.updateOppositeThreshold(sourceDistance, finalDistance);
        assertEquals(3645, result);
        assertEquals("3645", finalDistance.getText());
    }

    @Test
    @DisplayName("Test with runway 09L/27R - obstacle 50m from 27R threshold")
    void testObstacle50mFrom27RThreshold() {
        lowerRunway = new LogicalRunway("09L", 3902, 3902, 3902, 3595);
        higherRunway = new LogicalRunway("27R", 3884, 3962, 3884, 3884);
        controller.setRunways(new Runway(lowerRunway, higherRunway));
        sourceDistance.setText("50");
        Integer result = controller.updateOppositeThreshold(sourceDistance, finalDistance);
        assertEquals(3545, result);
        assertEquals("3545", finalDistance.getText());
    }

    @Test
    @DisplayName("Test distance calculation with null input")
    void testNullInput() {
        sourceDistance.setText("");
        Integer result = controller.updateOppositeThreshold(sourceDistance, finalDistance);
        assertNull(result);
    }

    @Test
    @DisplayName("Test distance calculation with non-numeric input")
    void testNonNumericInput() {
        sourceDistance.setText("invalid");
        Integer result = controller.updateOppositeThreshold(sourceDistance, finalDistance);
        assertNull(result);
    }

    @Test
    @DisplayName("Test when auto-fill is disabled")
    void testAutoFillDisabled() throws Exception {
        Field autoFillEnabledField = ObstacleFormController.class.getDeclaredField("autoFillEnabled");
        autoFillEnabledField.setAccessible(true);
        autoFillEnabledField.set(controller, false);
        sourceDistance.setText("150");
        Integer result = controller.updateOppositeThreshold(sourceDistance, finalDistance);
        assertNull(result);
        assertEquals("", finalDistance.getText());
    }
}
