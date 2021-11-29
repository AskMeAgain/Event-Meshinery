package ask.me.again.meshinery.core.utils.context;

import ask.me.again.meshinery.core.common.DataContext;
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
  int index;

  public TestContext(int index) {
    this.id = String.valueOf(index);
    this.index = index;
  }
}
