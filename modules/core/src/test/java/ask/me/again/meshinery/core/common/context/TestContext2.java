package ask.me.again.meshinery.core.common.context;

import ask.me.again.meshinery.core.common.Context;
import lombok.RequiredArgsConstructor;
import lombok.Value;

@Value
@RequiredArgsConstructor
public class TestContext2 implements Context {

  int index;

  @Override
  public String getId() {
    return null;
  }
}
