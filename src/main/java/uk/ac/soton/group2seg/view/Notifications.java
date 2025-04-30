package uk.ac.soton.group2seg.view;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.media.AudioClip;
import javafx.stage.Popup;
import javafx.stage.Stage;
import javafx.util.Duration;

public class Notifications {

  public static void showSuccessNotification(Stage primaryStage, String message) {
    Popup popup = new Popup();

    Label checkIcon = new Label("\u2714"); // ✔
    checkIcon.setStyle("-fx-text-fill: white; -fx-font-size: 16px; -fx-font-weight: bold;");

    Label messageLabel = new Label(message);
    messageLabel.setStyle("-fx-text-fill: white; -fx-font-size: 14px; -fx-padding: 0 0 0 10px;");

    HBox content = new HBox(10, checkIcon, messageLabel);
    content.setStyle("-fx-background-color: #4CAF50; -fx-background-radius: 5; -fx-padding: 10;");
    content.setAlignment(Pos.CENTER_LEFT);

    VBox wrapper = new VBox(content);
    wrapper.setStyle("-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.25), 8, 0, 2, 2);");

    popup.getContent().add(wrapper);
    popup.setAutoFix(true);
    popup.setAutoHide(true);
    popup.setHideOnEscape(true);

    double x = primaryStage.getX() + 30;
    double y = primaryStage.getY() + primaryStage.getHeight() - 100;
    popup.setX(x);
    popup.setY(y);

    try {
      AudioClip sound = new AudioClip(Notifications.class.getResource("/sounds/success.mp3").toString());
      sound.setVolume(0.1);
      sound.play();
    } catch (Exception e) {
      System.err.println("Notification sound failed to play: " + e.getMessage());
    }

    popup.show(primaryStage);
    Timeline hideTimeline = new Timeline(new KeyFrame(Duration.seconds(6), e -> popup.hide()));
    hideTimeline.setCycleCount(1);
    hideTimeline.play();
  }
}



