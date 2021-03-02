package com.dashboard;

import com.fazecast.jSerialComm.SerialPort;

public interface Nano33Service {

  void startPolling();

  void stopPolling();

  void onReadHandler(final Nano33ServiceSerial.OnReadHandler<byte[]> handler);

  void close();

  void connect(final SerialPort selectedPort);
}
