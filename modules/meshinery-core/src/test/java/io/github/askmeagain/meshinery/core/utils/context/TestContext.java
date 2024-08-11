package io.github.askmeagain.meshinery.core.utils.context;

import io.github.askmeagain.meshinery.core.common.MeshineryDataContext;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.Value;
import lombok.With;
import lombok.extern.jackson.Jacksonized;

@Value
@Builder(toBuilder = true)
@Jacksonized
public class TestContext implements MeshineryDataContext {

  @With String id;
  @With int index;

  @Getter(AccessLevel.PRIVATE) Map<String, String> metadata;

  public TestContext(int index) {
    this.id = String.valueOf(index);
    this.index = index;
    this.metadata = new ConcurrentHashMap<>();
  }

  public TestContext(String id, int index, Map<String, String> map) {
    this.id = id;
    this.index = index;
    if (map == null) {
      map = new ConcurrentHashMap<>();
    }
    this.metadata = new ConcurrentHashMap<>(map);
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
