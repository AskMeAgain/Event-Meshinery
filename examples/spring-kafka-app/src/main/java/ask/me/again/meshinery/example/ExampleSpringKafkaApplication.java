package ask.me.again.meshinery.example;

import ask.me.again.meshinery.example.config.ApplicationConfiguration;
import ask.me.again.meshinery.example.config.ExampleController;
import ask.me.again.meshinery.example.config.ExampleTaskConfiguration;
import io.github.askmeagain.meshinery.core.EnableMeshinery;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;

@SpringBootApplication
@EnableMeshinery
@Import({
  ExampleController.class,
  ExampleTaskConfiguration.class,
  ApplicationConfiguration.class
})
@SuppressWarnings("checkstyle:MissingJavadocType")
public class ExampleSpringKafkaApplication {

  public static void main(String[] args) {
    SpringApplication.run(ExampleSpringKafkaApplication.class, args);
  }
}