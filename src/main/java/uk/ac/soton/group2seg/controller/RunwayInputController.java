package uk.ac.soton.group2seg.controller;

import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import uk.ac.soton.group2seg.model.Runway;
import uk.ac.soton.group2seg.model.LogicalRunway;

import java.util.function.Consumer;

public class RunwayInputController {

    @FXML private VBox root;

    @FXML private TextField designation1Field;
    @FXML private TextField tora1Field;
    @FXML private TextField toda1Field;
    @FXML private TextField asda1Field;
    @FXML private TextField lda1Field;

    @FXML private TextField designation2Field;
    @FXML private TextField tora2Field;
    @FXML private TextField toda2Field;
    @FXML private TextField asda2Field;
    @FXML private TextField lda2Field;

    @FXML private Button removeButton;

    private Consumer<RunwayInputController> onRemoveCallback;

    @FXML
    public void initialize() {
        // Store this controller in the root node's properties
        root.getProperties().put("controller", this);
    }

    @FXML
    public void handleRemove() {
        if (onRemoveCallback != null) {
            onRemoveCallback.accept(this);
        }
    }

    public void enableRemoveButton(Consumer<RunwayInputController> callback) {
        removeButton.setVisible(true);
        this.onRemoveCallback = callback;
    }

    public Node getRoot() {
        return root;
    }

    public boolean isValid() {
        if (designation1Field.getText().trim().isEmpty()) {
            return false;
        }

        if (designation2Field.getText().trim().isEmpty()) {
            return false;
        }
        try {
            if (!tora1Field.getText().trim().isEmpty()) {
                Integer.parseInt(tora1Field.getText().trim());
            } else {
                return false;
            }

            if (!toda1Field.getText().trim().isEmpty()) {
                Integer.parseInt(toda1Field.getText().trim());
            } else {
                return false;
            }

            if (!asda1Field.getText().trim().isEmpty()) {
                Integer.parseInt(asda1Field.getText().trim());
            } else {
                return false;
            }

            if (!lda1Field.getText().trim().isEmpty()) {
                Integer.parseInt(lda1Field.getText().trim());
            } else {
                return false;
            }

            if (!tora2Field.getText().trim().isEmpty()) {
                Integer.parseInt(tora2Field.getText().trim());
            } else {
                return false;
            }

            if (!toda2Field.getText().trim().isEmpty()) {
                Integer.parseInt(toda2Field.getText().trim());
            } else {
                return false;
            }

            if (!asda2Field.getText().trim().isEmpty()) {
                Integer.parseInt(asda2Field.getText().trim());
            } else {
                return false;
            }

            if (!lda2Field.getText().trim().isEmpty()) {
                Integer.parseInt(lda2Field.getText().trim());
            } else {
                return false;
            }

            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public Runway getRunway() {
        if (!isValid()) {
            return null;
        }

        try {
            String desig1 = designation1Field.getText().trim();
            int tora1 = Integer.parseInt(tora1Field.getText().trim());
            int toda1 = Integer.parseInt(toda1Field.getText().trim());
            int asda1 = Integer.parseInt(asda1Field.getText().trim());
            int lda1 = Integer.parseInt(lda1Field.getText().trim());

            String desig2 = designation2Field.getText().trim();
            int tora2 = Integer.parseInt(tora2Field.getText().trim());
            int toda2 = Integer.parseInt(toda2Field.getText().trim());
            int asda2 = Integer.parseInt(asda2Field.getText().trim());
            int lda2 = Integer.parseInt(lda2Field.getText().trim());

            LogicalRunway logicalRunway1 = new LogicalRunway(desig1, asda1, toda1, tora1, lda1);
            LogicalRunway logicalRunway2 = new LogicalRunway(desig2, asda2, toda2, tora2, lda2);

            int bearing1 = extractBearing(desig1);
            int bearing2 = extractBearing(desig2);

            if (bearing1 < bearing2) {
                return new Runway(logicalRunway1, logicalRunway2);
            } else {
                return new Runway(logicalRunway2, logicalRunway1);
            }

        } catch (Exception e) {
            return null;
        }
    }
    private int extractBearing(String designation) {
        // Remove any suffix (L, R)
        String numericPart = designation.replaceAll("[LR]", "");
        try {
            return Integer.parseInt(numericPart);
        } catch (NumberFormatException e) {
            return -1;
        }
    }

}