package io.github.askmeagain.meshinery.connectors.docker;

import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.concurrent.LinkedBlockingQueue;
import lombok.Data;
import lombok.SneakyThrows;

@Data
public class InternalDockerDataContainer {
  LinkedBlockingQueue<String> logs = new LinkedBlockingQueue<>();
  PipedInputStream stdin = new PipedInputStream();
  PipedOutputStream userIn;

  Runnable shutdownContainer;

  @SneakyThrows
  public InternalDockerDataContainer() {
    userIn = new PipedOutputStream(stdin);
  }
}
