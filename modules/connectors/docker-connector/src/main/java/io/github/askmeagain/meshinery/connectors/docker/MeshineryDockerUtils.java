package io.github.askmeagain.meshinery.connectors.docker;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.async.ResultCallback;
import com.github.dockerjava.api.model.Frame;
import com.github.dockerjava.core.DefaultDockerClientConfig;
import com.github.dockerjava.core.DockerClientImpl;
import com.github.dockerjava.httpclient5.ApacheDockerHttpClient;
import java.io.Closeable;
import java.util.List;
import java.util.Map;
import lombok.SneakyThrows;
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

  @SneakyThrows
  public static DataContainer runContainer(String imageName, String[] startCommand, Map<String, Object> enviVars) {
    var transformedEnviVars = enviVars.entrySet().stream()
        .map(kv -> kv.getKey().replace("MESHINERY_CONNECTORS_DOCKER_", "") + "=" + getValue(kv))
        .toList();

    var dockerClient = getInstance();
    var container = dockerClient.createContainerCmd(imageName)
        .withCmd(startCommand)
        .withEnv(transformedEnviVars)
        .withTty(true)
        .withStdinOpen(true)
        .withAttachStdout(true)
        .withAttachStderr(true)
        .withAttachStdin(true)
        .exec();

    var dataContainer = new DataContainer();

    dockerClient.attachContainerCmd(container.getId())
        .withStdIn(dataContainer.getStdin())
        .withStdErr(true)
        .withStdOut(true)
        .withFollowStream(true)
        .exec((new ResultCallback.Adapter<>() {

          private boolean alreadyShutdown;

          @Override
          public void onStart(Closeable closeable) {
            super.onStart(closeable);
            dataContainer.setShutdownContainer(this::onComplete);
          }

          @Override
          public void onNext(Frame item) {
            dataContainer.getLogs().add(new String(item.getPayload()));
          }

          @Override
          @SneakyThrows
          public void onComplete() {
            if (!alreadyShutdown) {
              alreadyShutdown = true;
              dataContainer.getUserIn().close();
              dataContainer.getStdin().close();
              try (dockerClient) {
                dockerClient.removeContainerCmd(container.getId())
                    .withForce(true)
                    .exec();
              }
            }
          }
        }))
        .awaitStarted();

    dockerClient.startContainerCmd(container.getId())
        .exec();

    return dataContainer;

  }

  private static String getValue(Map.Entry<String, Object> kv) {
    if (kv.getValue() instanceof List list) {
      return list.get(0).toString();
    }

    return (String) kv.getValue();
  }
}
