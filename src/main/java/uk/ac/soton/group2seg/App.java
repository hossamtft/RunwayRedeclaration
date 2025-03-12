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
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/LoginView.fxml"));
            Parent root = loader.load();

            primaryStage.setTitle("Runway Redeclaration Tool");
            primaryStage.setScene(new Scene(root, 600, 450));
            primaryStage.show();
        }

        public static void main(String[] args) {
            launch(args);
        }
    }

