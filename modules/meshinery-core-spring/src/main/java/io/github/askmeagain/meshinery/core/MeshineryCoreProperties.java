package io.github.askmeagain.meshinery.core;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Data
@Validated
@ConfigurationProperties("meshinery.core")
public class MeshineryCoreProperties {

  private final List<@NotBlank String> inject = new ArrayList<>();

  private boolean batchJob = false;

  private int backpressureLimit = 200;

  private boolean shutdownOnError = false;

  private boolean shutdownOnFinished = true;

  private int gracePeriodMilliseconds = 2000;

  private final Map<String, Map<String, @NotBlank String>> taskProperties = new HashMap<>();

}
