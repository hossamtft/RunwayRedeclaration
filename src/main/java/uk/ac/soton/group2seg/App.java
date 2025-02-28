package uk.ac.soton.group2seg;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;

public class App extends Application {
        @Override
        public void start(Stage primaryStage) throws Exception {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/RunwayView.fxml"));
            Parent root = loader.load();

            primaryStage.setTitle("JavaFX Scene Builder Example");
            primaryStage.setScene(new Scene(root, 750, 750));
            primaryStage.show();
        }

        public static void main(String[] args) {
            launch(args);
        }
    }

