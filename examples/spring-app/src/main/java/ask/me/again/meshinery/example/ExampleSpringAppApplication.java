package ask.me.again.meshinery.example;

import ask.me.again.meshinery.example.config.ExampleShutdownController;
import ask.me.again.meshinery.example.config.ExampleTaskConfiguration;
import ask.me.again.meshinery.example.config.InputConfiguration;
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
public class ExampleSpringAppApplication {

  public static void main(String[] args) {
    SpringApplication.run(ExampleSpringAppApplication.class, args);
  }
}