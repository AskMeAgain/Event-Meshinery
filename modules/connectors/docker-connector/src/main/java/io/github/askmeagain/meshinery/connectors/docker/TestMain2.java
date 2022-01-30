package io.github.askmeagain.meshinery.connectors.docker;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.async.ResultCallback;
import com.github.dockerjava.api.model.Frame;
import com.github.dockerjava.core.DefaultDockerClientConfig;
import com.github.dockerjava.core.DockerClientImpl;
import com.github.dockerjava.httpclient5.ApacheDockerHttpClient;
import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.nio.charset.StandardCharsets;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class TestMain2 {
  public static void main(String[] args) throws InterruptedException, IOException {

    var dockerClient = getInstance();
    var container = dockerClient.createContainerCmd("alpine")
        .withCmd("tail", "-f","/dev/null")
        .exec();

    dockerClient.startContainerCmd(container.getId()).exec();

    var exec = dockerClient.execCreateCmd(container.getId())
        .withCmd("sh")
        .withTty(true)
        .withAttachStdout(true)
        .withAttachStderr(true)
        .withAttachStdin(true)
        .exec();

    var stdin = new PipedInputStream();
    var writeTo = new PipedOutputStream(stdin);

    dockerClient.execStartCmd(exec.getId())
        .withStdIn(stdin)
        .withTty(false)
        .exec((new ResultCallback.Adapter<>() {
          @Override
          public void onNext(Frame item) {
            System.out.println("Received: " + item.toString());
          }

          @Override
          public void onComplete() {
            System.out.println("Finished!");
          }
        }))
        .awaitStarted();

    Thread.sleep(3000);

    System.out.println("Echoing now!");
    writeTo.write("echo test\n".getBytes(StandardCharsets.UTF_8));
    writeTo.write("exit\n".getBytes(StandardCharsets.UTF_8));
    Thread.sleep(3000);
  }


  private static DockerClient getInstance() {
    var config = DefaultDockerClientConfig.createDefaultConfigBuilder().build();
    var httpClient = new ApacheDockerHttpClient.Builder()
        .dockerHost(config.getDockerHost())
        .sslConfig(config.getSSLConfig())
        .build();
    return DockerClientImpl.getInstance(config, httpClient);
  }
}
