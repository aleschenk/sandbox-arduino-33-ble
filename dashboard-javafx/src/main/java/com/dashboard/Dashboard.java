package com.dashboard;

import com.fazecast.jSerialComm.SerialPort;
import eu.hansolo.tilesfx.Tile;
import eu.hansolo.tilesfx.TileBuilder;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListCell;
import javafx.scene.layout.GridPane;

import java.net.URL;
import java.util.ResourceBundle;
import java.util.concurrent.ScheduledExecutorService;

public class Dashboard implements Initializable {

  final int WINDOW_SIZE = 25;

  private static final double TILE_WIDTH = 150;

  private static final double TILE_HEIGHT = 150;

  @FXML
  private GridPane gridPane;

  @FXML
  private ComboBox<SerialPort> portsComboBox;

  @FXML
  private Button connectButton;

  @FXML
  private Button disconnectButton;

  @FXML
  private Button startButton;

  @FXML
  private Button stopButton;

  private final ScheduledExecutorService scheduledExecutorService;

  private final Nano33Service nano33Service;

  public Dashboard(final Nano33Service nano33Service, final ScheduledExecutorService scheduledExecutorService) {
    this.nano33Service = nano33Service;
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

  private void setButtonState(final boolean connect, final boolean disconnect) {
    connectButton.setDisable(connect);
    disconnectButton.setDisable(disconnect);
  }

  @Override
  public void initialize(final URL location, final ResourceBundle resources) {
    Tile tempGauge = gauge("Temperature", "ºC");
    tempGauge.setMaxValue(140);

    Tile humidityGauge = gauge("Humidity", "%");
    humidityGauge.setMaxValue(100);

    Tile pressureGauge = gauge("Pressure", "hPa");
    pressureGauge.setMaxValue(200);

    XYChart.Series<String, Number> seriesX = new XYChart.Series();
    XYChart.Series<String, Number> seriesY = new XYChart.Series();
    XYChart.Series<String, Number> seriesZ = new XYChart.Series();

    var ref = new Object() {
      int time = 0;
    };

    disconnectButton.setOnAction(event -> {
      nano33Service.close();
      setButtonState(false, true);
      startButton.setDisable(true);
      stopButton.setDisable(true);
    });

    connectButton.setOnAction(event -> {
      var selectedPort = portsComboBox.getSelectionModel().getSelectedItem();
      nano33Service.connect(selectedPort);
      setButtonState(true, false);
      startButton.setDisable(false);
    });

    startButton.setOnAction(event -> {
      nano33Service.startPolling();
      startButton.setDisable(true);
      stopButton.setDisable(false);
    });

    stopButton.setOnAction(event -> {
      nano33Service.stopPolling();
      stopButton.setDisable(true);
      startButton.setDisable(false);
    });

    nano33Service.onReadHandler(data -> {
      ref.time++;
      if (data.startsWith("[")) {
//      sprintf(str, "%.2f,%.2f,%2.f,%2.f,%2.f,%2.f,%2.f,%2.f,%2.f,%2.f,%2.f,%2.f,%d,%d,%d,%d,%d\n", temperature, humidity, pressure, ax, ay, az, gx, gy, gz, mx, my, mz, r, g, b, proximity, gesture);
        data = data
          .replace("[", "")
          .replace("\n", "");
        System.out.println(data);
        String[] value = data.split(",");

        float temperature = Float.valueOf(value[0]);
        float humidity = Float.valueOf(value[1]);
        float pressure = Float.valueOf(value[2]);
        float ax = Float.valueOf(value[3]);
        float ay = Float.valueOf(value[4]);
        float az = Float.valueOf(value[5]);
        float gx = Float.valueOf(value[6]);
        float gy = Float.valueOf(value[7]);
        float gz = Float.valueOf(value[8]);
        float mx = Float.valueOf(value[9]);
        float my = Float.valueOf(value[10]);
        float mz = Float.valueOf(value[11]);
        int r = Integer.parseInt(value[12]);
        int g = Integer.parseInt(value[13]);
        int b = Integer.parseInt(value[14]);
        int proximity = Integer.parseInt(value[15]);
        int gesture = Integer.parseInt(value[16]);

        tempGauge.setValue(temperature);
        humidityGauge.setValue(humidity);
        pressureGauge.setValue(pressure);

        seriesX.getData().add(new XYChart.Data<>(Integer.toString(ref.time), ax));
        seriesY.getData().add(new XYChart.Data<>(Integer.toString(ref.time), ay));
        seriesZ.getData().add(new XYChart.Data<>(Integer.toString(ref.time), az));

        if (seriesX.getData().size() > WINDOW_SIZE) {
          seriesX.getData().remove(0);
        }
        if (seriesY.getData().size() > WINDOW_SIZE) {
          seriesY.getData().remove(0);
        }
        if (seriesZ.getData().size() > WINDOW_SIZE) {
          seriesZ.getData().remove(0);
        }
//        if (seriesR.getData().size() > WINDOW_SIZE) {
//          seriesR.getData().remove(0);
//        }

//      29.76,48.77,101, 0,-0, 1,10,6,5,0,-1
//      Platform.runLater();
      }
    });

//    Stream.of(commPorts).forEach(port -> {
//      System.out.println(
//        port.getDescriptivePortName()
//          + " -> " + port.getPortDescription()
//          + " -> " + port.getSystemPortName());
//    });

    portsComboBox.setItems(FXCollections.observableArrayList(SerialPort.getCommPorts()));
    portsComboBox.setCellFactory(lv -> new ListCell<>() {
      @Override
      protected void updateItem(final SerialPort item, final boolean empty) {
        super.updateItem(item, empty);
        setText(empty ? "" : item.getDescriptivePortName() + "->" + item.getPortDescription());
      }
    });


    XYChart.Series<String, Number> seriesR = new XYChart.Series();

    Tile imuChart = lineChart("IMU", seriesX, seriesY, seriesZ);
    Tile gestureChart = lineChart("Gesture Color", seriesR);

    gridPane.add(tempGauge, 0, 0);
    gridPane.add(humidityGauge, 1, 0);
    gridPane.add(pressureGauge, 2, 0);
    gridPane.add(imuChart, 0, 1, 3, 1);
    gridPane.add(gestureChart, 0, 2, 3, 1);


//    scheduledExecutorService.scheduleAtFixedRate(() -> {
//      Integer randomX = ThreadLocalRandom.current().nextInt(10);
//      Integer randomY = ThreadLocalRandom.current().nextInt(10);
//      Integer randomZ = ThreadLocalRandom.current().nextInt(10);
//      Integer randomR = ThreadLocalRandom.current().nextInt(10);
//
//      Platform.runLater(() -> {
////        Date now = new Date();
//        ref.time++;
//        seriesX.getData().add(new XYChart.Data<>(Integer.toString(ref.time), randomX));
//        seriesY.getData().add(new XYChart.Data<>(Integer.toString(ref.time), randomY));
//        seriesZ.getData().add(new XYChart.Data<>(Integer.toString(ref.time), randomZ));
//        seriesR.getData().add(new XYChart.Data<>(Integer.toString(ref.time), randomR));
//
//        if (seriesX.getData().size() > WINDOW_SIZE) {
//          seriesX.getData().remove(0);
//        }
//        if (seriesY.getData().size() > WINDOW_SIZE) {
//          seriesY.getData().remove(0);
//        }
//        if (seriesZ.getData().size() > WINDOW_SIZE) {
//          seriesZ.getData().remove(0);
//        }
//        if (seriesR.getData().size() > WINDOW_SIZE) {
//          seriesR.getData().remove(0);
//        }
//      });
//    }, 0, 100, TimeUnit.MILLISECONDS);
  }

}
