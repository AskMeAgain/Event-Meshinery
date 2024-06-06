package io.github.askmeagain.meshinery.core;

import io.github.askmeagain.meshinery.core.task.MeshineryTask;
import io.github.askmeagain.meshinery.core.task.MeshineryTaskVerifier;
import jakarta.annotation.PostConstruct;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;

@Configuration
@SuppressWarnings("checkstyle:MissingJavadocType")
@RequiredArgsConstructor
public class MeshineryVerificationConfiguration {

  private final List<MeshineryTask<?, ?>> tasks;

  @PostConstruct
  void setup() {
    MeshineryTaskVerifier.verifyTasks(tasks);
  }

}