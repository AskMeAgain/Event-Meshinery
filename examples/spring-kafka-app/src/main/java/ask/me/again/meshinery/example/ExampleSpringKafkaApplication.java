package ask.me.again.meshinery.example;

import ask.me.again.meshinery.example.config.ApplicationConfiguration;
import ask.me.again.meshinery.example.config.ExampleController;
import ask.me.again.meshinery.example.config.ExampleTaskConfiguration;
import ask.me.again.springconfig.MeshineryAutoConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;

@SpringBootApplication
@Import({
  MeshineryAutoConfiguration.class,
  ExampleController.class,
  ExampleTaskConfiguration.class,
  ApplicationConfiguration.class
})
public class ExampleSpringKafkaApplication {

  public static void main(String[] args) {
    SpringApplication.run(ExampleSpringKafkaApplication.class, args);
  }
}