package io.github.askmeagain.meshinery.core.utils.context;

import io.github.askmeagain.meshinery.core.common.DataContext;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;
import lombok.With;
import lombok.extern.jackson.Jacksonized;

@Value
@Builder(toBuilder = true)
@AllArgsConstructor
@Jacksonized
public class TestContext implements DataContext {

  @With
  String id;
  @With
  int index;

  public TestContext(int index) {
    this.id = String.valueOf(index);
    this.index = index;
  }
}
