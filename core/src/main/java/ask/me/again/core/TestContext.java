package ask.me.again.core;

import lombok.Builder;
import lombok.Value;

@Value
@Builder(toBuilder = true)
public class TestContext implements Context{

  int testvalue1;

}
