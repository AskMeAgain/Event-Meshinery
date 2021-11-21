package ask.me.again.meshinery.example;

import ask.me.again.meshinery.connectors.mysql.MeshineryMysqlConfiguration;
import ask.me.again.meshinery.draw.EnableMeshineryDrawing;
import ask.me.again.meshinery.example.config.ExampleController;
import ask.me.again.meshinery.example.config.ExampleVoteConfiguration;
import ask.me.again.meshinery.example.config.InputConfiguration;
import ask.me.again.meshinery.monitoring.EnableMeshineryMonitoring;
import ask.me.again.springconfig.EnableMeshinery;
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