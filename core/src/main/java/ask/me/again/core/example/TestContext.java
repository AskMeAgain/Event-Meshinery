package ask.me.again.core.example;

import ask.me.again.core.common.Context;
import ask.me.again.core.common.IdAware;
import lombok.Builder;
import lombok.Value;

@Value
@Builder(toBuilder = true)
public class TestContext implements Context, IdAware {

  int testValue1;

  String id;

}
