package io.github.askmeagain.meshinery.core;

import java.util.ArrayList;
import java.util.List;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@NoArgsConstructor
@ConfigurationProperties("meshinery.core")
public class MeshineryConfigProperties {

  private final List<String> inject = new ArrayList<>();

}
