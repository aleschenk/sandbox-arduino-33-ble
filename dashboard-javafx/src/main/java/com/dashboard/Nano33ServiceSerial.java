package com.dashboard;

import com.fazecast.jSerialComm.SerialPort;

public class Nano33ServiceSerial implements Nano33Service {

  public interface OnReadHandler<T> {
    void readEvent(final T data);
  }

  private OnReadHandler handler;

  private Thread pollingWorker;

  private SerialPort serialPort;

  public void open(final SerialPort serialPort) {
    this.serialPort = serialPort;
    this.serialPort.openPort();
    this.serialPort.setBaudRate(9600);
    pollingWorker = new Thread(new SerialWorker(this.serialPort));
  }

  private class SerialWorker implements Runnable {
    private final SerialPort serialPort;

    private SerialWorker(final SerialPort serialPort) {
      this.serialPort = serialPort;
    }

    @Override
    public void run() {
      while (true) {
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

  public synchronized void startPolling() {
    pollingWorker.start();
  }

  public synchronized void stopPolling() {

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

}
