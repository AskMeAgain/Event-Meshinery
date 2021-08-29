package ask.me.again.example;

import ask.me.again.example.config.ExampleShutdownController;
import ask.me.again.example.config.ExampleTaskConfiguration;
import ask.me.again.example.config.InputConfiguration;
import ask.me.again.springconfig.SpringConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;

@SpringBootApplication
@Import({
  SpringConfiguration.class,
  ExampleShutdownController.class,
  ExampleTaskConfiguration.class,
  InputConfiguration.class
})
public class ExampleSpringKafkaAppApplication {

  public static void main(String[] args) {
    SpringApplication.run(ExampleSpringKafkaAppApplication.class, args);
  }
}