package com.dashboard;

public interface Nano33Service {

  void onReadHandler(final Nano33ServiceSerial.OnReadHandler<byte[]> handler);
  
  void close();

}
