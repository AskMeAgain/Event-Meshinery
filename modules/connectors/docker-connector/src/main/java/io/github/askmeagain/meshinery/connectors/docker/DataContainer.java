package io.github.askmeagain.meshinery.connectors.docker;

import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.concurrent.LinkedBlockingQueue;
import lombok.Data;
import lombok.SneakyThrows;

@Data
public class DataContainer {
  LinkedBlockingQueue<String> logs = new LinkedBlockingQueue<>();
  PipedInputStream stdin = new PipedInputStream();
  PipedOutputStream userIn;

  Runnable shutdownContainer;

  @SneakyThrows
  public DataContainer() {
    userIn = new PipedOutputStream(stdin);
  }
}
