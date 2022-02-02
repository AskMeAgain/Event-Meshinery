package io.github.askmeagain.meshinery.connectors.docker;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.async.ResultCallback;
import com.github.dockerjava.api.command.CreateContainerCmd;
import com.github.dockerjava.api.model.Bind;
import com.github.dockerjava.api.model.Frame;
import com.github.dockerjava.api.model.HostConfig;
import com.github.dockerjava.api.model.Volume;
import com.github.dockerjava.core.DefaultDockerClientConfig;
import com.github.dockerjava.core.DockerClientImpl;
import com.github.dockerjava.httpclient5.ApacheDockerHttpClient;
import io.github.askmeagain.meshinery.core.task.TaskData;
import java.io.Closeable;
import java.util.ArrayList;
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
  public static InternalDockerDataContainer runContainer(String imageName, String[] startCommand, TaskData taskData) {

    var dockerClient = getInstance();
    var dataContainer = new InternalDockerDataContainer();
    var createContainerCmd = dockerClient.createContainerCmd(imageName)
        .withCmd(startCommand)
        .withEnv(getEnviVars(taskData))
        .withTty(true)
        .withStdinOpen(true)
        .withAttachStdout(true)
        .withAttachStderr(true)
        .withAttachStdin(true);

    var createdContainer = addVolumes(createContainerCmd, taskData).exec();

    dockerClient.attachContainerCmd(createdContainer.getId())
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
                dockerClient.removeContainerCmd(createdContainer.getId())
                    .withForce(true)
                    .exec();
              }
            }
          }
        }))
        .awaitStarted();

    dockerClient.startContainerCmd(createdContainer.getId())
        .exec();

    return dataContainer;

  }

  private static CreateContainerCmd addVolumes(CreateContainerCmd containerBuilder, TaskData taskData) {

    var volumes = taskData.getAllWithPrefix(MeshineryDockerProperties.VOLUME_PREFIX);

    if (volumes.isEmpty()) {
      return containerBuilder;
    }

    var binds = new ArrayList<Bind>();

    for (var kv : volumes.entrySet()) {
      var source = getValue(kv);
      var destination = kv.getKey().replace(MeshineryDockerProperties.VOLUME_PREFIX, "");

      var volume = new Volume(destination);
      containerBuilder = containerBuilder.withVolumes(volume);
      binds.add(new Bind(source, volume));
    }

    return containerBuilder.withHostConfig(HostConfig.newHostConfig().withBinds(binds));
  }

  private static List<String> getEnviVars(TaskData taskData) {
    var enviVars = taskData.getAllWithPrefix(MeshineryDockerProperties.ENVIRONMENT_PREFIX);

    return enviVars.entrySet().stream()
        .map(kv -> kv.getKey().replace(MeshineryDockerProperties.ENVIRONMENT_PREFIX, "") + "=" + getValue(kv))
        .toList();
  }

  private static String getValue(Map.Entry<String, Object> kv) {
    if (kv.getValue() instanceof List list) {
      return list.get(0).toString();
    }

    return (String) kv.getValue();
  }
}
