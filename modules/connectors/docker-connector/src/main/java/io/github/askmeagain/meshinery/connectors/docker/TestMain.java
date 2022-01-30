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
public class TestMain {
  public static void main(String[] args) throws InterruptedException, IOException {

    var dockerClient = getInstance();
    var container = dockerClient.createContainerCmd("alpine")
        .withCmd("sh")
        .withTty(true)
        .withStdinOpen(true)
        .withAttachStdout(true)
        .withAttachStderr(true)
        .withAttachStdin(true)
        .exec();

    var stdin = new PipedInputStream();
    var writeTo = new PipedOutputStream(stdin);

    dockerClient.attachContainerCmd(container.getId())
        .withStdIn(stdin)
        .withStdErr(true)
        .withStdOut(true)
        .withFollowStream(true)
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

    dockerClient.startContainerCmd(container.getId())
        .exec();

    Thread.sleep(3000);

    System.out.println("Echoing now!");
    writeTo.write("echo test; exit\n".getBytes(StandardCharsets.UTF_8));
    writeTo.flush();
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
