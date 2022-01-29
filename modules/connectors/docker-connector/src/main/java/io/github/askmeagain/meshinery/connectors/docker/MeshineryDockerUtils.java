package io.github.askmeagain.meshinery.connectors.docker;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.async.ResultCallback;
import com.github.dockerjava.api.model.Frame;
import com.github.dockerjava.core.DefaultDockerClientConfig;
import com.github.dockerjava.core.DockerClientImpl;
import com.github.dockerjava.httpclient5.ApacheDockerHttpClient;
import java.io.PipedInputStream;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class MeshineryDockerUtils {

  private static DockerClient getInstance() {
    var config = DefaultDockerClientConfig.createDefaultConfigBuilder().build();
    var httpClient = new ApacheDockerHttpClient.Builder()
        .dockerHost(config.getDockerHost())
        .sslConfig(config.getSSLConfig())
        .build();
    return DockerClientImpl.getInstance(config, httpClient);
  }

  public static String createContainer(String container) {
    return "asdasdasd";
  }

  public static void createTty(
      String containerId,
      String[] startCommand,
      DockerConnector.DataContainer dataContainer
  ) throws InterruptedException {
    var dockerClient = getInstance();
    var execCreateCmdResponse = dockerClient.execCreateCmd(containerId)
        .withCmd(startCommand)
        .withAttachStdout(true)
        .withAttachStderr(true)
        .withTty(true)
        .withAttachStdin(true)
        .exec();

    dockerClient.execStartCmd(execCreateCmdResponse.getId())
        .withStdIn(dataContainer.getStdin())
        .withTty(false)
        .exec(new ResultCallback.Adapter<>() {
          @Override
          public void onNext(Frame frame) {
            dataContainer.getLogs().add(new String(frame.getPayload(), StandardCharsets.UTF_8));
          }

          @Override
          public void onComplete() {
            log.info("Completed docker container process");
          }
        })
        .awaitStarted();
  }
}
