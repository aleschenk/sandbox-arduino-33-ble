package com.dashboard;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

public class Main extends Application {

  private final ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();

  @Override
  public void start(final Stage stage) throws Exception {
    var resource = getClass().getResource("dashboard.fxml");
    FXMLLoader loader = new FXMLLoader(resource);
    loader.setControllerFactory(param -> {
      if (param.getCanonicalName() == Dashboard.class.getCanonicalName()) {
        return new Dashboard(executorService);
      } else {
        try {
          return param.newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
          e.printStackTrace();
          return null;
        }
      }
    });
    Parent root = loader.load();

    Scene scene = new Scene(root);
    stage.setScene(scene);
    stage.centerOnScreen();
    stage.setOnCloseRequest(event -> executorService.shutdown());
    stage.show();
  }
}
