package com.dashboard;

import eu.hansolo.tilesfx.Tile;
import eu.hansolo.tilesfx.TileBuilder;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.scene.layout.GridPane;

import java.net.URL;
import java.util.Date;
import java.util.ResourceBundle;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

public class Dashboard implements Initializable {

  final int WINDOW_SIZE = 50;

  private static final double TILE_WIDTH = 150;
  private static final double TILE_HEIGHT = 150;

  @FXML
  private GridPane gridPane;

  @FXML
  private LineChart<String, Number> imuChart;

//  @FXML
//  private ObservableList<XYChart.Series<Number, Number>> imuSeries;
//
//  @FXML
//  private ObservableList<XYChart.Data<Number, Number>> imuData;

  public Dashboard() {
//    imuData = FXCollections.observableArrayList();
//    imuSeries = FXCollections.observableArrayList();
  }

  public void x() {
    // LineChart Data
    XYChart.Series<String, Number> series2 = new XYChart.Series();
    series2.setName("Inside");
    series2.getData().add(new XYChart.Data("MO", 8));
    series2.getData().add(new XYChart.Data("TU", 5));
    series2.getData().add(new XYChart.Data("WE", 0));
    series2.getData().add(new XYChart.Data("TH", 2));
    series2.getData().add(new XYChart.Data("FR", 4));
    series2.getData().add(new XYChart.Data("SA", 3));
    series2.getData().add(new XYChart.Data("SU", 5));

    XYChart.Series<String, Number> series3 = new XYChart.Series();
    series3.setName("Outside");
    series3.getData().add(new XYChart.Data("MO", 8));
    series3.getData().add(new XYChart.Data("TU", 5));
    series3.getData().add(new XYChart.Data("WE", 0));
    series3.getData().add(new XYChart.Data("TH", 2));
    series3.getData().add(new XYChart.Data("FR", 4));
    series3.getData().add(new XYChart.Data("SA", 3));
    series3.getData().add(new XYChart.Data("SU", 5));

    Tile tile = TileBuilder.create()
      .skinType(Tile.SkinType.SMOOTHED_CHART)
      .prefSize(TILE_WIDTH, TILE_HEIGHT)
      .title("SmoothedChart Tile")
      //.animated(true)
      .smoothing(false)
      .series(series2, series3)
      .build();

    gridPane.add(tile, 1, 1);
  }

  @Override
  public void initialize(final URL location, final ResourceBundle resources) {
    x();
    XYChart.Series<String, Number> series = new XYChart.Series();
    series.getData().add(new XYChart.Data<String, Number>("1", 2));
    imuChart.getData().add(series);
    // put dummy data onto graph per second
    var scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();

    var ref = new Object() {
      int time = 0;
    };

    scheduledExecutorService.scheduleAtFixedRate(() -> {
      // get a random integer between 0-10
      Integer random = ThreadLocalRandom.current().nextInt(10);

      // Update the chart
      Platform.runLater(() -> {
        // get current time
        Date now = new Date();
        // put random number with current time
        ref.time++;
        series.getData().add(new XYChart.Data<>(Integer.toString(ref.time), random));

        if (series.getData().size() > WINDOW_SIZE)
          series.getData().remove(0);
      });

    }, 0, 100, TimeUnit.MILLISECONDS);
  }

}
