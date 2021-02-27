package com.dashboard;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {

  @Override
  public void start(final Stage stage) throws Exception {
    var resource = getClass().getResource("dashboard.fxml");
    FXMLLoader loader = new FXMLLoader(resource);
    Parent root = loader.load();

    Scene scene = new Scene(root);
    stage.setScene(scene);
    stage.centerOnScreen();
    stage.show();
  }
}
