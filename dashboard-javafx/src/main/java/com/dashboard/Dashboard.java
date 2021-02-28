package com.dashboard;

import com.fazecast.jSerialComm.SerialPort;
import eu.hansolo.tilesfx.Tile;
import eu.hansolo.tilesfx.TileBuilder;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.XYChart;
import javafx.scene.control.ComboBox;
import javafx.scene.layout.GridPane;

import java.net.URL;
import java.util.ResourceBundle;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

public class Dashboard implements Initializable {

  final int WINDOW_SIZE = 25;

  private static final double TILE_WIDTH = 150;

  private static final double TILE_HEIGHT = 150;

  @FXML
  private GridPane gridPane;

  @FXML
  private ComboBox<SerialPort> portsComboBox;

  private final ScheduledExecutorService scheduledExecutorService;

  public Dashboard(final ScheduledExecutorService scheduledExecutorService) {
    this.scheduledExecutorService = scheduledExecutorService;
  }

  public Tile gauge(final String title, final String unit) {
    return TileBuilder.create()
      .skinType(Tile.SkinType.GAUGE)
      .prefSize(TILE_WIDTH, TILE_HEIGHT)
      .title(title)
      .unit(unit)
      .threshold(75)
      .build();
  }

  //  final Series<String, Number>... SERIES
  public Tile lineChart(final String title, final XYChart.Series<String, Number>... series) {
    return TileBuilder.create()
      .skinType(Tile.SkinType.SMOOTHED_CHART)
      .prefSize(TILE_WIDTH, TILE_HEIGHT)
      .title(title)
      //.animated(true)
      .smoothing(true)
      .series(series)
      .build();
  }

  @Override
  public void initialize(final URL location, final ResourceBundle resources) {
    SerialPort[] commPorts = SerialPort.getCommPorts();
    portsComboBox.setItems(FXCollections.observableArrayList(commPorts));

    Tile tempGauge = gauge("Temperature", "ºC");
    Tile humidityGauge = gauge("Humidity", "%");
    Tile pressureGauge = gauge("Pressure", "hPa");

    XYChart.Series<String, Number> seriesX = new XYChart.Series();
    XYChart.Series<String, Number> seriesY = new XYChart.Series();
    XYChart.Series<String, Number> seriesZ = new XYChart.Series();

    XYChart.Series<String, Number> seriesR = new XYChart.Series();

    Tile imuChart = lineChart("IMU", seriesX, seriesY, seriesZ);
    Tile gestureChart = lineChart("Gesture Color", seriesR);

    gridPane.add(tempGauge, 0, 0);
    gridPane.add(humidityGauge, 1, 0);
    gridPane.add(pressureGauge, 2, 0);
    gridPane.add(imuChart, 0, 1, 3, 1);
    gridPane.add(gestureChart, 0, 2, 3, 1);

    var ref = new Object() {
      int time = 0;
    };

    scheduledExecutorService.scheduleAtFixedRate(() -> {
      Integer randomX = ThreadLocalRandom.current().nextInt(10);
      Integer randomY = ThreadLocalRandom.current().nextInt(10);
      Integer randomZ = ThreadLocalRandom.current().nextInt(10);
      Integer randomR = ThreadLocalRandom.current().nextInt(10);

      Platform.runLater(() -> {
//        Date now = new Date();
        ref.time++;
        seriesX.getData().add(new XYChart.Data<>(Integer.toString(ref.time), randomX));
        seriesY.getData().add(new XYChart.Data<>(Integer.toString(ref.time), randomY));
        seriesZ.getData().add(new XYChart.Data<>(Integer.toString(ref.time), randomZ));
        seriesR.getData().add(new XYChart.Data<>(Integer.toString(ref.time), randomR));

        if (seriesX.getData().size() > WINDOW_SIZE) {
          seriesX.getData().remove(0);
        }
        if (seriesY.getData().size() > WINDOW_SIZE) {
          seriesY.getData().remove(0);
        }
        if (seriesZ.getData().size() > WINDOW_SIZE) {
          seriesZ.getData().remove(0);
        }
        if (seriesR.getData().size() > WINDOW_SIZE) {
          seriesR.getData().remove(0);
        }
      });
    }, 0, 100, TimeUnit.MILLISECONDS);
  }

}
