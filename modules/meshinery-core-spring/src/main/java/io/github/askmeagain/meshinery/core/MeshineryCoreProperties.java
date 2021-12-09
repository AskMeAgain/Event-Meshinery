package io.github.askmeagain.meshinery.core;

import java.util.ArrayList;
import java.util.List;
import javax.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties("meshinery.core")
public class MeshineryCoreProperties {

  private final List<@NotNull String> inject = new ArrayList<>();

  private boolean batchJob = false;

  private boolean shutdownOnError = true;

}
