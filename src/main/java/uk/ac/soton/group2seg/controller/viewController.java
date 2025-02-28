package uk.ac.soton.group2seg.controller;


import javafx.fxml.FXML;
import javafx.scene.control.*;

public class viewController
{
    @FXML
    private ComboBox airportDropdown;

    @FXML
    private Button createButton;

    @FXML
    private Label resultLabel;

    @FXML
    private void handleCreateButtonAction()
    {
        MenuItem selectedAirport = (MenuItem) airportDropdown.getSelectionModel().getSelectedItem();
        if (selectedAirport != null) {
            // Get the text of the selected airport and display it in the label
            String airportName = selectedAirport.getText();
            resultLabel.setText("Selected Airport: " + airportName);
        } else {
            // If no selection, set a default message
            resultLabel.setText("No airport selected.");
        }
    }
    }

