package ask.me.again.meshinery.core.task;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.With;


@NoArgsConstructor
@SuppressWarnings("checkstyle:MissingJavadocType")
@AllArgsConstructor
public class TaskData {

  @With(AccessLevel.PRIVATE)
  private Properties properties = new Properties();

  public TaskData put(String key, String value) {
    var newProperties = new Properties();
    newProperties.putAll(properties);
    var list = (List<String>) newProperties.get(key);
    if (list == null) {
      list = new ArrayList<>();
    }
    list.add(value);
    newProperties.put(key, list);
    return this.withProperties(newProperties);
  }

  public List<String> get(String key) {
    return (List<String>) properties.get(key);
  }

  public String getSingle(String key) {
    var list = (List<String>) properties.get(key);
    return list.get(0);
  }
}
