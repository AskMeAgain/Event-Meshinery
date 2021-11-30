package ask.me.again.meshinery.example;

import io.github.askmeagain.meshinery.connectors.mysql.MeshineryMysqlConfiguration;
import io.github.askmeagain.meshinery.draw.EnableMeshineryDrawing;
import ask.me.again.meshinery.example.config.ExampleController;
import ask.me.again.meshinery.example.config.ExampleVoteConfiguration;
import ask.me.again.meshinery.example.config.InputConfiguration;
import io.github.askmeagain.meshinery.monitoring.EnableMeshineryMonitoring;
import io.github.askmeagain.meshinery.core.EnableMeshinery;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;

@SuppressWarnings("checkstyle:MissingJavadocType")
@SpringBootApplication
@EnableMeshinery
@EnableMeshineryMonitoring
@EnableMeshineryDrawing
@Import({
    ExampleVoteConfiguration.class,
    ExampleController.class,
    InputConfiguration.class,
    MeshineryMysqlConfiguration.class
})
public class ExampleVotingApplication {

  public static void main(String[] args) {
    SpringApplication.run(ExampleVotingApplication.class, args);
  }
}