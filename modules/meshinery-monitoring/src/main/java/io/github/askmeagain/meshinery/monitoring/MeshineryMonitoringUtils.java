package io.github.askmeagain.meshinery.monitoring;

import java.util.regex.Pattern;
import lombok.experimental.UtilityClass;

@UtilityClass
public class MeshineryMonitoringUtils {

  public static String convertLambdaProcessorName(Class<?> processorName) {
    var simpleName = processorName.getSimpleName();

    if (simpleName.contains("$$Lambda$")) {
      return "lambda-" + processorName.hashCode();
    }

    if (simpleName.contains("$EnhancerBySpringCGLIB$$")) {
      var pattern = Pattern.compile("\\$(.*)\\$\\$EnhancerBySpringCGLIB\\$");
      var matcher = pattern.matcher(simpleName);

      if (matcher.find()) {
        return matcher.group(1);
      }
    }

    return simpleName;
  }
}
