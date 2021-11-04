package ask.me.again.meshinery.example;

import ask.me.again.meshinery.draw.MeshineryDrawerConfiguration;
import ask.me.again.meshinery.example.config.ExampleController;
import ask.me.again.meshinery.example.config.ExampleTaskConfiguration;
import ask.me.again.meshinery.example.config.InputConfiguration;
import ask.me.again.springconfig.MeshineryAutoConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;

@SuppressWarnings("checkstyle:MissingJavadocType")
@SpringBootApplication
@Import({
    MeshineryAutoConfiguration.class,
    MeshineryDrawerConfiguration.class,
    ExampleController.class,
    ExampleTaskConfiguration.class,
    InputConfiguration.class
})
public class ExampleSpringAppApplication {

  public static void main(String[] args) {
    SpringApplication.run(ExampleSpringAppApplication.class, args);
  }
}