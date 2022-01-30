package io.github.askmeagain.meshinery.connectors.docker;

import java.io.InputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import lombok.SneakyThrows;
import lombok.Value;

@Value
public class DataContainer {
  LinkedBlockingQueue<String> logs = new LinkedBlockingQueue<>();
  PipedInputStream stdin = new PipedInputStream();
  PipedOutputStream stdin2;
  AtomicBoolean isFinished = new AtomicBoolean();

  @SneakyThrows
  public DataContainer() {
    stdin2 = new PipedOutputStream(stdin);
  }
}
