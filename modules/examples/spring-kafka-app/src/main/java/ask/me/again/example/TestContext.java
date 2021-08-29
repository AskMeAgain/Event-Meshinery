package ask.me.again.example;

import ask.me.again.core.common.Context;
import lombok.*;
import lombok.extern.jackson.Jacksonized;


@Value
@Builder(toBuilder = true)
@Jacksonized
public class TestContext implements Context {

  int testValue1;

  String id;

}
