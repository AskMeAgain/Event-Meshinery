package ask.me.again.meshinery.example;

import io.github.askmeagain.meshinery.core.common.DataContext;
import lombok.Builder;
import lombok.Value;

@Value
@Builder(toBuilder = true)
@SuppressWarnings("checkstyle:MissingJavadocType")
public class TestContext implements DataContext {

  int testValue1;

  String id;

}
