package ask.me.again.meshinery.core.utils.context;

import ask.me.again.meshinery.core.common.DataContext;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;
import lombok.With;

@Value
@Builder
@AllArgsConstructor
public class TestContext2 implements DataContext {

  @With
  String id;
  int index;

  public TestContext2(int index) {
    this.id = String.valueOf(index);
    this.index = index;
  }
}
