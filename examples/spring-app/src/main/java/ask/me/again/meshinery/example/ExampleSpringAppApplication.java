package ask.me.again.meshinery.example;

import io.github.askmeagain.meshinery.draw.EnableMeshineryDrawing;
import ask.me.again.meshinery.example.config.ExampleController;
import ask.me.again.meshinery.example.config.ExampleHeartbeatSplitJoinConfiguration;
import ask.me.again.meshinery.example.config.ExampleVoteConfiguration;
import ask.me.again.meshinery.example.config.InputConfiguration;
import io.github.askmeagain.meshinery.core.EnableMeshinery;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;

@SuppressWarnings("checkstyle:MissingJavadocType")
@SpringBootApplication
@EnableMeshinery
@EnableMeshineryDrawing
@Import({
    ExampleVoteConfiguration.class,
    ExampleController.class,
    ExampleHeartbeatSplitJoinConfiguration.class,
    InputConfiguration.class
})
public class ExampleSpringAppApplication {

  public static void main(String[] args) {
    SpringApplication.run(ExampleSpringAppApplication.class, args);
  }
}