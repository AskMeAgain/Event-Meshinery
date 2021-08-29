package ask.me.again.example;

import ask.me.again.core.common.Context;
import lombok.Builder;
import lombok.Value;

@Value
@Builder(toBuilder = true)
public class TestContext implements Context {

  int testValue1;

  String id;

}
