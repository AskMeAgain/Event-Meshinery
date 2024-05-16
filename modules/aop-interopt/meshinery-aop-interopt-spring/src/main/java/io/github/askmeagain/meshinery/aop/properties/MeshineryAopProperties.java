package io.github.askmeagain.meshinery.aop.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "meshinery.aop")
public class MeshineryAopProperties {

  private Boolean enabled;

}