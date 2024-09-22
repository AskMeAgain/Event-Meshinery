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
  @With boolean step1;
  @With boolean step2;
  @With boolean step3;


  public TestContext(int index) {
    this.id = String.valueOf(index);
    this.index = index;
    this.step1 = false;
    this.step2 = false;
    this.step3 = false;
    this.metadata = new ConcurrentHashMap<>();
  }

  public TestContext(String id, int index, Map<String, String> map, boolean step1, boolean step2, boolean step3) {
    this.id = id;
    this.index = index;
    this.step1 = step1;
    this.step2 = step2;
    this.step3 = step3;
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
