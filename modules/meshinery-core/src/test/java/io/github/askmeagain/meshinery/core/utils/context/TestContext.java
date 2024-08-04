package io.github.askmeagain.meshinery.core.utils.context;

import io.github.askmeagain.meshinery.core.common.MeshineryDataContext;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Value;
import lombok.With;
import lombok.extern.jackson.Jacksonized;

@Value
@Builder(toBuilder = true)
@AllArgsConstructor
@Jacksonized
public class TestContext implements MeshineryDataContext {

  @With
  String id;
  @With
  int index;

  @Getter(AccessLevel.PRIVATE)
  Map<String, String> metadata = new ConcurrentHashMap<>();

  public TestContext(int index) {
    this.id = String.valueOf(index);
    this.index = index;
  }

  @Override
  public String getMetadata(String key) {
    return metadata.get(key);
  }

  @Override
  public TestContext setMetadata(String key, String value) {
    metadata.put(key, value);
    return this;
  }
}
