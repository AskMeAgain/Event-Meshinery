package io.github.askmeagain.meshinery.connectors.docker;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.async.ResultCallback;
import com.github.dockerjava.api.model.Frame;
import com.github.dockerjava.core.DefaultDockerClientConfig;
import com.github.dockerjava.core.DockerClientImpl;
import com.github.dockerjava.httpclient5.ApacheDockerHttpClient;
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

  public static DataContainer runContainer(
      String imageName,
      String[] startCommand
  ) throws InterruptedException {

    var dataContainer = new DataContainer();
    var dockerClient = getInstance();
    var container = dockerClient.createContainerCmd(imageName)
        .withCmd(startCommand)
        .withTty(true)
        .withStdinOpen(true)
        .withAttachStdout(true)
        .withAttachStderr(true)
        .withAttachStdin(true)
        .exec();

    dockerClient.attachContainerCmd(container.getId())
        .withStdIn(dataContainer.getStdin())
        .withStdErr(true)
        .withStdOut(true)
        .withFollowStream(true)
        .exec((new ResultCallback.Adapter<>() {
          @Override
          public void onNext(Frame item) {
            dataContainer.getLogs().add(new String(item.getPayload()));
          }

          @Override
          public void onComplete() {
            dataContainer.getIsFinished().set(true);
            dockerClient.removeContainerCmd(container.getId())
                .withForce(true)
                .exec();
          }
        }))
        .awaitStarted();

    dockerClient.startContainerCmd(container.getId())
        .exec();

    return dataContainer;

  }
}
