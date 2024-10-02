package io.github.askmeagain.meshinery.core.other;

import jakarta.validation.constraints.NotBlank;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.Data;

@SuppressWarnings("checkstyle:MissingJavadocType")
@Data
public class MeshineryCoreProperties {

  private final List<@NotBlank String> inject = new ArrayList<>();

  private boolean batchJob = false;

  private int backpressureLimit = 200;

  private boolean shutdownOnError = false;

  /**
   * This is used in a hook, io.github.askmeagain.meshinery.core.MeshineryAutoConfiguration#shutdownHook
   */
  private boolean shutdownOnFinished = true;

  private boolean startImmediately = true;

  private boolean shutdownApi = true;

  private int gracePeriodMilliseconds = 2000;

  private final Map<String, Map<String, @NotBlank String>> taskProperties = new HashMap<>();

}
