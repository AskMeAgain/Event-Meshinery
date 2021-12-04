package io.github.askmeagain.meshinery.core;

import java.util.ArrayList;
import java.util.List;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties("meshinery.core")
public class MeshineryConfigProperties {

  private final List<String> inject = new ArrayList<>();

}
