package io.github.askmeagain.meshinery.core.utils.context;

import io.github.askmeagain.meshinery.core.common.MeshineryDataContext;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;
import lombok.With;

@Value
@Builder
@AllArgsConstructor
public class TestContext2 implements MeshineryDataContext {

  @With
  String id;
  int index;

  Map<String, String> metadata = new ConcurrentHashMap<>();

  public TestContext2(int index) {
    this.id = String.valueOf(index);
    this.index = index;
  }

  @Override
  public String getMetadata(String key) {
    return metadata.get(key);
  }

  @Override
  public TestContext2 setMetadata(String key, String value) {
    metadata.put(key, value);
    return this;
  }
}
