package com.dashboard;

import com.fazecast.jSerialComm.SerialPort;

public class Nano33Service {

  public SerialPort[] getPorts() {
    return SerialPort.getCommPorts();
  }

  public void open() {
//    SerialPort serialPort = SerialPort.getCommPort(serialPortName);
  }


}
