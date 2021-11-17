package ask.me.again.meshinery.core.common;

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

  public TaskData put(String key, Object value) {
    var newProperties = new Properties();
    newProperties.putAll(properties);
    newProperties.put(key, value);
    return this.withProperties(newProperties);
  }

  public TaskData appendToList(String key, Object value) {
    var newProperties = new Properties();
    newProperties.putAll(properties);
    var list = (List<Object>) newProperties.get(key);
    if (list == null) {
      list = new ArrayList<>();
    }
    list.add(value);
    newProperties.put(key, list);
    return this.withProperties(newProperties);
  }

  public String get(String key) {
    return properties.getProperty(key);
  }

  public List<String> getList(String key) {
    return (List<String>) properties.get(key);
  }
}
