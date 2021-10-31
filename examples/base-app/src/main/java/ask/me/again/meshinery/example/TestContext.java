package ask.me.again.meshinery.example;

import ask.me.again.meshinery.core.common.Context;
import lombok.Builder;
import lombok.Value;

@Value
@Builder(toBuilder = true)
@SuppressWarnings("checkstyle:MissingJavadocType")
public class TestContext implements Context {

  int testValue1;

  String id;

}
