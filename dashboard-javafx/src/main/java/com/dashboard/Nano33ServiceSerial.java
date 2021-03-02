package com.dashboard;

import com.fazecast.jSerialComm.SerialPort;

public class Nano33ServiceSerial implements Nano33Service {

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
        System.out.println("Read " + numRead + " bytes.");
        handler.readEvent(readBuffer);
      }
    }

  }

  @Override
  public synchronized void startPolling() {
    pollingActive = true;
    pollingWorker.start();
  }

  @Override
  public synchronized void stopPolling() {
    pollingActive = false;
  }

  public void onReadHandler(final OnReadHandler<byte[]> handler) {
    this.handler = handler;
  }

  @Override
  public void close() {
    if (serialPort == null) {
      return;
    }

    if (serialPort.isOpen()) {
      serialPort.closePort();
    }
  }

  @Override
  public void connect(final SerialPort selectedPort) {
    this.serialPort = selectedPort;
    this.serialPort.setBaudRate(9600);
    this.serialPort.openPort();
    pollingWorker = new Thread(new SerialWorker(this.serialPort));
  }

}
