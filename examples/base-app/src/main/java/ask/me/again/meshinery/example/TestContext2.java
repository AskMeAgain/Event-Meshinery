package ask.me.again.meshinery.example;

import ask.me.again.meshinery.core.common.DataContext;
import lombok.Builder;
import lombok.Value;

@Value
@Builder(toBuilder = true)
@SuppressWarnings("checkstyle:MissingJavadocType")
public class TestContext2 implements DataContext {

  int testValue1;

  String id;

}
