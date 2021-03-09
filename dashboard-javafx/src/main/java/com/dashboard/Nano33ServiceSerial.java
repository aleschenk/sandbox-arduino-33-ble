package com.dashboard;

import com.fazecast.jSerialComm.SerialPort;

import static java.nio.charset.StandardCharsets.UTF_8;

public class Nano33ServiceSerial implements Nano33Service {
  private static final String START_POLLING_COMMAND = "START";

  private static final String STOP_POLLING_COMMAND = "STOP";

  public interface OnReadHandler<T> {
    void readEvent(final T data);
  }

  private OnReadHandler handler;

  private Thread pollingWorker;

  private SerialPort serialPort;

  private boolean pollingActive = false;

  private class SerialWorker implements Runnable {
    private final SerialPort serialPort;

    private SerialWorker(final SerialPort serialPort) {
      this.serialPort = serialPort;
    }

    @Override
    public void run() {
      while (pollingActive) {
        while (serialPort.bytesAvailable() == 0) {
          try {
            Thread.sleep(20);
          } catch (InterruptedException e) {
            e.printStackTrace();
          }
        }
        byte[] readBuffer = new byte[serialPort.bytesAvailable()];
        int numRead = serialPort.readBytes(readBuffer, readBuffer.length);
//        System.out.println("Read " + numRead + " bytes.");
        String data = new String(readBuffer, UTF_8);
//        System.out.print(numRead + ": " + data);
//        if (data.endsWith("\n")) {
        handler.readEvent(data);
//        }
      }
    }

  }

  @Override
  public synchronized void startPolling() {
    pollingActive = true;
    serialPort.writeBytes(START_POLLING_COMMAND.getBytes(UTF_8), START_POLLING_COMMAND.length());
    new Thread(new SerialWorker(this.serialPort)).start();
  }

  @Override
  public synchronized void stopPolling() {
    pollingActive = false;
    serialPort.writeBytes(STOP_POLLING_COMMAND.getBytes(UTF_8), STOP_POLLING_COMMAND.length());
  }

  public void onReadHandler(final OnReadHandler<String> handler) {
    this.handler = handler;
  }

  @Override
  public void close() {
    if (serialPort == null) {
      return;
    }

    if (serialPort.isOpen()) {
      stopPolling();
      serialPort.closePort();
    }
  }

  @Override
  public void connect(final SerialPort selectedPort) {
    this.serialPort = selectedPort;
    this.serialPort.setBaudRate(9600);
    this.serialPort.openPort();
  }

}
